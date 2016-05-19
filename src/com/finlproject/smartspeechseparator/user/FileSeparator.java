package com.finlproject.smartspeechseparator.user;

import java.io.File;

import javax.imageio.ImageIO;

import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;

import com.finalproject.smartspeechseparator.audioprocessing.Mask;
import com.finalproject.smartspeechseparator.audioprocessing.WavFile;
import com.finalproject.smartspeechseparator.general.Cell;
import com.finalproject.smartspeechseparator.general.Separate_output_struct;
import com.finalproject.smartspeechseparator.transform.Analyzer;
import com.finalproject.smartspeechseparator.transform.MatrixTools;
import com.finalproject.smartspeechseparator.transform.Preprocessor;
import com.finalproject.smartspeechseparator.transform.STFT;
import com.finalproject.smartspeechseparator.transform.SpectrumType;
import com.finlproject.smartspeechseparator.learn.Initializer;
import com.finlproject.smartspeechseparator.learn.NMF_KL;
import com.finlproject.smartspeechseparator.learn.STATE;

public class FileSeparator {

	private Analyzer mixAnalyzer, speechAnalyzer, noiseAnalizer;
	private double[] cost;

	public Analyzer getMixAnalyzer() {
		return mixAnalyzer;
	}

	public Analyzer getSpeechAnalyzer() {
		return speechAnalyzer;
	}

	public Analyzer getNoiseAnalizer() {
		return noiseAnalizer;
	}

	public double[] getCost() {
		return cost;
	}

	SpectrumType analyzeType = SpectrumType.log;

	public SpectrumType getAnalyzeType() {
		return analyzeType;
	}

	public void setAnalyzeType(SpectrumType analyzeType) {
		this.analyzeType = analyzeType;
	}
	
	public SeparatedOutoutStructure SeparateFile_SimiSupervised(String wavfilepath,
			int niter, double Mu, double segma, int stft_wlen,  int usK_n, DoubleMatrix B_s, DoubleMatrix B_eq) {
		cost = null;
		Analyzer mixAnalyzer_l = null;
		Analyzer speechAnalyzer_l = null;
		Analyzer noiseAnalizer_l = null;
		SeparatedOutoutStructure output = new SeparatedOutoutStructure();

		MatrixTools tool = new MatrixTools();
		int K_s = B_s.getColumns(), K_eq = B_eq.getColumns();
		
		Cell actual_comp_inds = new Cell();
		actual_comp_inds.add(tool.makeIndecesRange(0, K_s - 1));
		actual_comp_inds.add(tool.makeIndecesRange(K_s, K_s + K_eq+usK_n - 1));

		Cell toNMF_comp_inds = new Cell();
		toNMF_comp_inds.add(tool.makeIndecesRange(0, K_s+K_eq -1));
		toNMF_comp_inds.add(tool.makeIndecesRange(K_s+K_eq, K_s + K_eq+usK_n - 1));
		try {
			Preprocessor pp = new Preprocessor();
			pp.setStft_wlen(stft_wlen);
			ComplexDoubleMatrix V = pp.preprocessing(wavfilepath, false);
			WavFile wavfile = pp.getWavfile();

			DoubleMatrix V_abs = tool.abs(V);

			Initializer init = new Initializer();
			

			int F = V_abs.getRows(), T = V_abs.getColumns();
			DoubleMatrix B_n_init = init.initialize_B(V_abs, F, usK_n);
			DoubleMatrix B_init = DoubleMatrix.concatHorizontally(DoubleMatrix.concatHorizontally(B_s , B_eq),  B_n_init);

			DoubleMatrix W_init = init.initialize_W( K_s+K_eq+usK_n, T);

			NMF_KL NMF = new NMF_KL(V_abs, B_init, W_init, toNMF_comp_inds);
			NMF.setNiter(niter);
			NMF.RunSimiSupervised_NMF();
			cost = NMF.getCOST();

			Mask mask = new Mask();
			Separate_output_struct s_out = mask.Separate(NMF.getB(), NMF.getW(), actual_comp_inds, V);

			STFT stft = new STFT();
			int nsampl = pp.getNsample();
			double[] speech = stft.istft_single(s_out.S, nsampl);

			wavfile.close();

			String speechPath = wavfilepath.substring(0, wavfilepath.length() - 4) + "speech_SimiSU.wav",
					noisePath = wavfilepath.substring(0, wavfilepath.length() - 4) + "noise_SimiSU.wav";
			WavFile speechWavFile = WavFile.newWavFile(new File(speechPath), 1, speech.length, wavfile.getValidBits(),
					wavfile.getSampleRate());

			speechWavFile.writeFrames(speech, speech.length);

			double[] noise = stft.istft_single(s_out.N, nsampl);
			WavFile noiseWavFile = WavFile.newWavFile(new File(noisePath), 1, noise.length, wavfile.getValidBits(),
					wavfile.getSampleRate());
			noiseWavFile.writeFrames(noise, noise.length);

			noiseWavFile.close();
			speechWavFile.close();

			speechAnalyzer_l = new Analyzer();
			noiseAnalizer_l = new Analyzer();
			mixAnalyzer_l = new Analyzer();

			if (analyzeType == SpectrumType.log) {
				speechAnalyzer_l.getLogSpectrogram(speechPath);
				noiseAnalizer_l.getLogSpectrogram(noisePath);
				mixAnalyzer_l.getLogSpectrogram(wavfilepath);
			} else {
				speechAnalyzer_l.getSpectrogram(speechPath);
				noiseAnalizer_l.getSpectrogram(noisePath);
				mixAnalyzer_l.getSpectrogram(wavfilepath);
			}

			output.setMixAnalyzer(mixAnalyzer_l);
			output.setMixedPath(wavfilepath);
			output.setNoiseAnalizer(noiseAnalizer_l);
			output.setNoisePath(noisePath);
			output.setSpeechAnalyzer(speechAnalyzer_l);
			output.setSpeechPath(speechPath);

			speechAnalyzer = speechAnalyzer_l;
			noiseAnalizer = noiseAnalizer_l;
			mixAnalyzer = mixAnalyzer_l;
			return output;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public SeparatedOutoutStructure SeparateFile_UnSupervised(String wavfilepath,
			int niter, double Mu, double segma, int stft_wlen, int K_s, int K_n) {
		cost = null;
		Analyzer mixAnalyzer_l = null;
		Analyzer speechAnalyzer_l = null;
		Analyzer noiseAnalizer_l = null;
		SeparatedOutoutStructure output = new SeparatedOutoutStructure();

		MatrixTools tool = new MatrixTools();
		Cell comp_inds = new Cell();
		comp_inds.add(tool.makeIndecesRange(0, K_s - 1));
		comp_inds.add(tool.makeIndecesRange(K_s, K_s + K_n - 1));

		try {
			Preprocessor pp = new Preprocessor();
			pp.setStft_wlen(stft_wlen);
			ComplexDoubleMatrix V = pp.preprocessing(wavfilepath, false);
			WavFile wavfile = pp.getWavfile();

			DoubleMatrix V_abs = tool.abs(V);

			Initializer init = new Initializer();
			

			int F = V_abs.getRows(), T = V_abs.getColumns();
			DoubleMatrix B_init = init.initialize_B(V_abs, F, K_s+K_n);

			DoubleMatrix W_init = init.initialize_W(K_n + K_s, T);

			NMF_KL NMF = new NMF_KL(V_abs, B_init, W_init, comp_inds);
			NMF.setNiter(niter);
			NMF.RunUnupervised_NMF();
			cost = NMF.getCOST();

			Mask mask = new Mask();
			Separate_output_struct s_out = mask.Separate(NMF.getB(), NMF.getW(), comp_inds, V);

			STFT stft = new STFT();
			int nsampl = pp.getNsample();
			double[] speech = stft.istft_single(s_out.S, nsampl);

			wavfile.close();

			String speechPath = wavfilepath.substring(0, wavfilepath.length() - 4) + "speech_UNS.wav",
					noisePath = wavfilepath.substring(0, wavfilepath.length() - 4) + "noise_UNS.wav";
			WavFile speechWavFile = WavFile.newWavFile(new File(speechPath), 1, speech.length, wavfile.getValidBits(),
					wavfile.getSampleRate());

			speechWavFile.writeFrames(speech, speech.length);

			double[] noise = stft.istft_single(s_out.N, nsampl);
			WavFile noiseWavFile = WavFile.newWavFile(new File(noisePath), 1, noise.length, wavfile.getValidBits(),
					wavfile.getSampleRate());
			noiseWavFile.writeFrames(noise, noise.length);

			noiseWavFile.close();
			speechWavFile.close();

			speechAnalyzer_l = new Analyzer();
			noiseAnalizer_l = new Analyzer();
			mixAnalyzer_l = new Analyzer();

			if (analyzeType == SpectrumType.log) {
				speechAnalyzer_l.getLogSpectrogram(speechPath);
				noiseAnalizer_l.getLogSpectrogram(noisePath);
				mixAnalyzer_l.getLogSpectrogram(wavfilepath);
			} else {
				speechAnalyzer_l.getSpectrogram(speechPath);
				noiseAnalizer_l.getSpectrogram(noisePath);
				mixAnalyzer_l.getSpectrogram(wavfilepath);
			}

			output.setMixAnalyzer(mixAnalyzer_l);
			output.setMixedPath(wavfilepath);
			output.setNoiseAnalizer(noiseAnalizer_l);
			output.setNoisePath(noisePath);
			output.setSpeechAnalyzer(speechAnalyzer_l);
			output.setSpeechPath(speechPath);

			speechAnalyzer = speechAnalyzer_l;
			noiseAnalizer = noiseAnalizer_l;
			mixAnalyzer = mixAnalyzer_l;
			return output;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public SeparatedOutoutStructure SeparateFile_Supervised(String wavfilepath, DoubleMatrix B_s, DoubleMatrix B_n,
			int niter, double Mu, double segma, int stft_wlen) {
		cost = null;
		Analyzer mixAnalyzer_l = null;
		Analyzer speechAnalyzer_l = null;
		Analyzer noiseAnalizer_l = null;
		SeparatedOutoutStructure output = new SeparatedOutoutStructure();

		int K_s = B_s.getColumns(), K_n = B_n.getColumns();
		MatrixTools tool = new MatrixTools();
		Cell comp_inds = new Cell();
		comp_inds.add(tool.makeIndecesRange(0, K_s - 1));
		comp_inds.add(tool.makeIndecesRange(K_s, K_s + K_n - 1));

		try {
			Preprocessor pp = new Preprocessor();
			pp.setStft_wlen(stft_wlen);
			ComplexDoubleMatrix V = pp.preprocessing(wavfilepath, false);
			WavFile wavfile = pp.getWavfile();

			DoubleMatrix V_abs = tool.abs(V);

			DoubleMatrix B_init = DoubleMatrix.concatHorizontally(B_n, B_s);

			Initializer init = new Initializer();

			int F = V_abs.getRows(), T = V_abs.getColumns();
			DoubleMatrix W_init = init.initialize_W(K_n + K_s, T);

			NMF_KL NMF = new NMF_KL(V_abs, B_init, W_init, comp_inds);
			NMF.setNiter(niter);
			NMF.RunSupervised_NMF();
			cost = NMF.getCOST();

			Mask mask = new Mask();
			Separate_output_struct s_out = mask.Separate(NMF.getB(), NMF.getW(), comp_inds, V);

			STFT stft = new STFT();
			int nsampl = pp.getNsample();
			double[] speech = stft.istft_single(s_out.S, nsampl);

			wavfile.close();

			String speechPath = wavfilepath.substring(0, wavfilepath.length() - 4) + "speech_SU.wav",
					noisePath = wavfilepath.substring(0, wavfilepath.length() - 4) + "noise_SU.wav";
			WavFile speechWavFile = WavFile.newWavFile(new File(speechPath), 1, speech.length, wavfile.getValidBits(),
					wavfile.getSampleRate());

			speechWavFile.writeFrames(speech, speech.length);

			double[] noise = stft.istft_single(s_out.N, nsampl);
			WavFile noiseWavFile = WavFile.newWavFile(new File(noisePath), 1, noise.length, wavfile.getValidBits(),
					wavfile.getSampleRate());
			noiseWavFile.writeFrames(noise, noise.length);

			noiseWavFile.close();
			speechWavFile.close();

			speechAnalyzer_l = new Analyzer();
			noiseAnalizer_l = new Analyzer();
			mixAnalyzer_l = new Analyzer();

			if (analyzeType == SpectrumType.log) {
				speechAnalyzer_l.getLogSpectrogram(speechPath);
				noiseAnalizer_l.getLogSpectrogram(noisePath);
				mixAnalyzer_l.getLogSpectrogram(wavfilepath);
			} else {
				speechAnalyzer_l.getSpectrogram(speechPath);
				noiseAnalizer_l.getSpectrogram(noisePath);
				mixAnalyzer_l.getSpectrogram(wavfilepath);
			}

			output.setMixAnalyzer(mixAnalyzer_l);
			output.setMixedPath(wavfilepath);
			output.setNoiseAnalizer(noiseAnalizer_l);
			output.setNoisePath(noisePath);
			output.setSpeechAnalyzer(speechAnalyzer_l);
			output.setSpeechPath(speechPath);

			speechAnalyzer = speechAnalyzer_l;
			noiseAnalizer = noiseAnalizer_l;
			mixAnalyzer = mixAnalyzer_l;
			return output;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public void separateFile(String source_path, STATE state, int niter, double Mu, double segma, int K_s, int K_n)
			throws Exception {

		MatrixTools tool = new MatrixTools();

		// Preprocessing...
		Preprocessor pp = new Preprocessor();
		ComplexDoubleMatrix V = pp.preprocessing(source_path, false);
		WavFile wavfile = pp.getWavfile();
		int nsampl = pp.getNsample();
		int F = V.getRows(), T = V.getColumns();

		///// calculate | V |
		DoubleMatrix V_abs = tool.abs(V);
		///////////////////////////////////////////////////////////////////////////

		switch (state) {
		case supervised: {
			break;
		}
		case simisupervised: {
			break;
		}
		case unsupervised: {
			break;
		}
		}

	}

	public static void main(String[] args) {

	}

}
