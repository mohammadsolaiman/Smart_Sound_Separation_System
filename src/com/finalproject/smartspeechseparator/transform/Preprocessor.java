package com.finalproject.smartspeechseparator.transform;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.jblas.ComplexDoubleMatrix;

import com.finalproject.smartspeechseparator.audioprocessing.WavFile;
import com.finalproject.smartspeechseparator.audioprocessing.WavFileException;
import com.finalproject.smartspeechseparator.general.SystemSettings;

public class Preprocessor {

	private WavFile wavfile;
	private int nsample;
	private double[] data;
	private int stft_wlen;
	private ComplexDoubleMatrix V;
	private int sampleLength;
	
	public Preprocessor(){
		this.stft_wlen = SystemSettings.stft_win_len;
	}
	
	public int getStft_wlen() {
		return stft_wlen;
	}


	public void setStft_wlen(int stft_wlen) {
		this.stft_wlen = stft_wlen;
	}


	public WavFile getWavfile() {
		return wavfile;
	}
	

	public int getSampleLength() {
		return sampleLength;
	}

	public void setSampleLength(int sampleLength) {
		this.sampleLength = sampleLength;
	}

	public int getNsample() {
		return nsample;
	}
	public double[] getData() {
		return data;
	}

	
	public ComplexDoubleMatrix getV() {
		return V;
	}

	public void setV(ComplexDoubleMatrix v) {
		V = v;
	}

	public ComplexDoubleMatrix preprocessing(String source_path, boolean take_sample)
			throws IOException, WavFileException {


		 wavfile = WavFile.openWavFile(new File(source_path));

		double[][] buffer = new double[wavfile.getNumChannels()][(int) wavfile.getNumFrames()];
		wavfile.readFrames(buffer, (int) wavfile.getNumFrames());
		double[] x;
		if (take_sample && buffer[0].length > this.sampleLength) {
			//take random part from the buffer
			
			//select random position
			Random rand = new Random();
			int r = (int) Math.floor(rand.nextFloat() * (buffer[0].length - this.sampleLength - 1) + 1);

			x = new double[this.sampleLength];
			System.out.println(" buffer[0].length = "+ buffer[0].length+"\t this.sampleLength = "+ this.sampleLength+"\t x.length = "+x.length);
			for (int i = r; i < r + this.sampleLength; i++) {
				x[i-r] = buffer[0][i];// + buffer[1][i]) / 2;
			}
		} else {
			x = new double[(int) wavfile.getNumFrames()];
			for (int i = 0; i < (int) wavfile.getNumFrames(); i++) {
				x[i] = buffer[0][i] ;//+ buffer[1][i]) / 2;
			}
		}
		data = x;
		nsample = x.length;
		STFT stft = new STFT();
		V = stft.stft_single(x, stft_wlen);

		return V;
	}


}
