package com.finalproject.smartspeechseparator.transform;

/*********** STFT CLASS *************
 * METHODS : 
 * 1- stft_single   calculate the short time Fourier transformation of x 
 * 		INPUT: x the signal 1D-array of the sound wave, wlen the window length
 * 		the function using sin window function to calculate the transformation 
 * 		
 * 		OUTPUT: X 2D-matrix of size F x T where F is the number of frequency bins
 * 					and T the number of time frames
 * 		
 * 2- istft_single  calculate the inverse of the STFT of spectrum X
 * 		INPUT: X 2D-matrix  F x T as the output of stft_single method
 * 		Output: x 1D-array containing the original signals  
 * 		
 * */

import org.jblas.ComplexDouble;
import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;
import org.jblas.ranges.IndicesRange;
import org.jblas.ranges.Range;
import org.jtransforms.fft.DoubleFFT_1D;

import com.finalproject.smartspeechseparator.debug.DebugLogger;

public class STFT {

	public org.jblas.ComplexDoubleMatrix stft_single(double[] x) {
		return stft_single(x,
				1024);/**
						 * wlen: window length (default: 1024 samples or 64ms at
						 * 16 kHz, which is optimal for speech source separation
						 * via binary time-frequency masking)
						 **/
	}

	public ComplexDoubleMatrix stft_single(double[] x_, int wlen) {
		DebugLogger Debug = new DebugLogger();
		double nsampl = x_.length;
		ComplexDoubleMatrix X;/**
								 * X: nbin x nfram matrix containing the STFT
								 * coefficients with nbin frequency bins and
								 * nfram time frames
								 **/
		if (wlen != 4 * Math.floor(wlen / 4)) {
			System.err.println("ERROR: The window length must be a multiple of 4."
					+ "\nMETHOD INVOCK: stft_single with wlen = " + wlen);
			return null;
		}
		///// Computing STFT coefficients /////
		// Defining sine window
		MatrixTools mt = new MatrixTools();

		double[][] win_v = mt.makeVector(0.5, 1, wlen - 0.5);
		for (int i = 0; i < win_v[0].length; i++) {
			win_v[0][i] = Math.sin(win_v[0][i] / wlen * Math.PI);
		}
		DoubleMatrix win = new DoubleMatrix(win_v);
		DoubleMatrix x = new DoubleMatrix(x_);
		win = win.transpose();
		x = x.transpose();
		// V = F x T ----.> T is nfram
		int nfram = (int) Math.ceil(nsampl / wlen * 2);
		// % Zero-padding
		x = DoubleMatrix.concatHorizontally(x, DoubleMatrix.zeros(1, (int) (nfram * wlen / 2 - nsampl)));
		// % Pre-processing for edges
		x = DoubleMatrix.concatHorizontally(DoubleMatrix.zeros(1, wlen / 4), x);
		x = DoubleMatrix.concatHorizontally(x, DoubleMatrix.zeros(1, wlen / 4));

		DoubleMatrix swin = DoubleMatrix.zeros((nfram + 1) * wlen / 2, 1);
		Range cs = new IndicesRange(mt.makeIndecesRange(0, 0));
		for (int t = 0; t < nfram; t++) {
			Range rs = new IndicesRange(mt.makeIndecesRange(t * wlen / 2, t * wlen / 2 + (wlen - 1)));
			swin.put(rs, cs, swin.get(rs, cs).add(mt.pow(win, 2)));
		}

		swin = mt.sqrt(swin.mul(wlen));

		int nbin = wlen / 2 + 1;

		X = ComplexDoubleMatrix.zeros(nbin, nfram); // X = F x T

		for (int t = 0; t < nfram; t++) {
			// Framing
			Range rs = new IndicesRange(mt.makeIndecesRange(t * wlen / 2, t * wlen / 2 + wlen - 1));
			DoubleMatrix den = swin.get(rs, 0);
			DoubleMatrix numi = x.get(0, rs);

			numi = numi.transpose();
			numi = numi.mul(win);

			DoubleMatrix frame_ = numi.div(den);
			double[] frame = frame_.toArray();
			// FFT
			double[] fft = new double[frame.length * 2];
			for (int i = 0; i < frame.length; i++) {
				fft[i] = frame[i];
			}
			DoubleFFT_1D d_fft_1d = new DoubleFFT_1D(frame.length);

			d_fft_1d.realForwardFull(fft);

			ComplexDouble val = new ComplexDouble(0, 0);
			for (int i = 0; i < nbin; i++) {
				double re = fft[2 * i], im = fft[2 * i + 1];
				val.set(re, im);
				X.put(i, t, val);
			}

		}
		return X;
	}

	public double[] istft_single(ComplexDoubleMatrix X, int nsampl) {
		com.finalproject.smartspeechseparator.debug.DebugLogger Debug = new DebugLogger();

		int nbin = X.getRows(), nfram = X.getColumns();
		if (nbin == 2 * Math.floor((double) nbin / 2)) {
			System.err.println("The number of frequency bins must be odd.");
			return null;
		}

		int wlen = 2 * (nbin - 1);

		// %%% Computing inverse STFT signal %%%
		// Defining sine window
		MatrixTools mt = new MatrixTools();

		double[][] win_v = mt.makeVector(0.5, 1, wlen - 0.5);
		for (int i = 0; i < win_v[0].length; i++) {
			win_v[0][i] = Math.sin(win_v[0][i] / wlen * Math.PI);
		}
		DoubleMatrix win = new DoubleMatrix(win_v);

		// % Pre-processing for edges
		DoubleMatrix swin = DoubleMatrix.zeros(1, (nfram + 1) * wlen / 2);
		Range cs = new IndicesRange(mt.makeIndecesRange(0, 0));

		for (int t = 0; t < nfram; t++) {
			Range rs = new IndicesRange(mt.makeIndecesRange(t * wlen / 2, t * wlen / 2 + wlen - 1));
			swin.put(rs, cs, swin.get(rs, cs).add(mt.pow(win, 2)));
		}
		swin = mt.sqrt(swin.div(wlen));
		DoubleMatrix x = DoubleMatrix.zeros((nfram + 1) * wlen / 2, 1);

		for (int t = 0; t < nfram; t++) {
			// IFFT
			ComplexDoubleMatrix fframe = X.getColumn(t);
			fframe = ComplexDoubleMatrix.concatVertically(fframe,
					X.get(mt.makeIndecesRange(wlen / 2 - 1, 1, -1), t).conj());
			double[] fframe_ = new double[fframe.length * 2];

			DoubleFFT_1D d_fft_1d = new DoubleFFT_1D(fframe.length);
			int ctr = 0;
			for (int i = 0; i < fframe.getRows(); i++) {
				fframe_[ctr] = fframe.getReal(i);
				fframe_[ctr + 1] = fframe.getImag(i);
				ctr += 2;
			}

			d_fft_1d.complexInverse(fframe_, true);
			double[] frame = new double[fframe_.length / 2];
			ctr = 0;
			// getting the real values
			for (int i = 0; i < fframe_.length; i += 2) {
				frame[ctr] = fframe_[i];
				ctr++;
			}
			// Overlap-add
			DoubleMatrix factorToAdd = new DoubleMatrix(frame).transpose().mul(win)
					.div(swin.get(mt.makeIndecesRange(t * wlen / 2, t * wlen / 2 + wlen - 1)));
			x.put(mt.makeIndecesRange(t * wlen / 2, t * wlen / 2 + wlen - 1),
					x.get(mt.makeIndecesRange(t * wlen / 2, t * wlen / 2 + wlen - 1), 0).add(factorToAdd));
		}
		// Truncation
		double[] x_ = new double[(wlen / 4 + nsampl) - (wlen / 4 + 1) + 1];
		int ctr = 0;
		for (int i = wlen / 4; i < wlen / 4 + nsampl; i++) {
			x_[ctr] = x.get(i);
			ctr++;
		}
		return x_;
	}

}
