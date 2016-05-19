package com.finlproject.smartspeechseparator.learn;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;

import com.finalproject.smartspeechseparator.audioprocessing.ColorMap;
import com.finalproject.smartspeechseparator.audioprocessing.Mask;
import com.finalproject.smartspeechseparator.audioprocessing.WavFile;
import com.finalproject.smartspeechseparator.audioprocessing.WavFileException;
import com.finalproject.smartspeechseparator.database.Basis_row;
import com.finalproject.smartspeechseparator.database.DB_handler;
import com.finalproject.smartspeechseparator.database.SOUNDTYPE;
import com.finalproject.smartspeechseparator.database.Sound_row;
import com.finalproject.smartspeechseparator.general.Cell;
import com.finalproject.smartspeechseparator.general.Separate_output_struct;
import com.finalproject.smartspeechseparator.transform.MatrixTools;
import com.finalproject.smartspeechseparator.transform.Preprocessor;
import com.finalproject.smartspeechseparator.transform.STFT;


public class TestNMF {

	public static int K = 6;
	public static String speech_dec_path = "speechB_Dic.txt", noise_dec_path = "noiseB_Dic.txt";

	public static void main(String[] args) {

		try {

			//System.out.println("Current Dir : "+ System.getProperty("user.dir")+File.separator+"data");
			MatrixTools tool = new MatrixTools();

			// Preprocessing...
			Preprocessor pp = new Preprocessor();
			ComplexDoubleMatrix V = pp.preprocessing("input.wav", false);
			WavFile wavfile = pp.getWavfile();
			int nsampl = pp.getNsample();
			int F = V.getRows(), T = V.getColumns();

			 System.out.println("\nF = " + F + "\tT = " + T + "\n" + V.get(50,
			 50));
			///////////////////////////////////////////////////////////////////////////

			///// calculate | V |
			DoubleMatrix V_abs = tool.abs(V);
			///////////////////////////////////////////////////////////////////////////

			drowLogSpectrum(V_abs, "mix");

			// INITIALIZATION
			Initializer init = new Initializer();
			DoubleMatrix B_init = init.initialize_B(V_abs, F, K);
			DoubleMatrix W_init = init.initialize_W(K, T);
			Cell comp_ind = new Cell();
			comp_ind.add(new int[] { 0, 1, 2 });
			comp_ind.add(new int[] { 3, 4, 5 });
			//////////////////////////////////////////////////////////////////////////

			//// RUN NMF_KL
			NMF_KL NMF = new NMF_KL(V_abs, B_init, W_init, comp_ind);
			 NMF.setNiter(100);
			NMF.RunUnupervised_NMF();
			////////////////////////////////////////////////////////////////////////

			
			///////////////////////////////DB //////////////////////////////////
			
			DB_handler db = new DB_handler();
			
			try {
				db.clearDB();
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			try{
				try{
					db.InsertSound("input.wav", SOUNDTYPE.mix);
				}catch(Exception e1){
					e1.printStackTrace();
				}
			List<Sound_row> li1 = db.selectSounds("select * from smartsoundseparationdb.sound;");
			for(Sound_row sr : li1){
				System.out.println(sr.getId()+",\t"+sr.getDir()+",\t"+sr.getName()+",\t"+sr.getType());
			}
			Sound_row row = db.getRecordByName("input.wav");
			
			db.InsertBasis(row,tool.DoubleMatrex2StringBuilder( NMF.getB()), pp.getStft_wlen(), K);
			
			List<Basis_row> li = db.getBasisBySoundName("input.wav");
			tool.DoubleMatrex2File(NMF.getB(), "Orgenal_B.txt");
 
			FileWriter fw = new FileWriter(new File("B_from_DB.txt"));
			fw.write(li.get(li.size()-1).getData().toString());
			fw.close();
			
			List<Basis_row> basisLi = db.getAllBasisOfType(SOUNDTYPE.mix);
			for(Basis_row br : basisLi)
				System.out.println(br.getId()+",\t"+br.getStft_win_len()+",\t"+br.getSound_id());
			
			}catch(Exception e){
				e.printStackTrace();
			}
			
			
			
			
			
			// DoubleMatrix B_s = tool.File2DoubleMatrix(speech_dec_path);
			// DoubleMatrix B_n = tool.File2DoubleMatrix(noise_dec_path);
			//
			// K = B_s.getColumns() + B_n.getColumns();
			//
			// comp_ind.clear();
			// comp_ind.add(tool.makeIndecesRange(0,
			// (int)Math.floor((double)K/2)));
			// comp_ind.add(tool.makeIndecesRange((int)Math.floor((double)K/2)+1,K-1
			// ));
			//
			// W_init = init.initialize_W(K, T);
			//
			// B_init = DoubleMatrix.concatHorizontally(B_s, B_n);
			// NMF_KL NMF = new NMF_KL(V_abs, B_init, W_init, comp_ind);
			// NMF.RunSupervised_NMF();

			//// apply MASK and Separate
			Mask mask = new Mask();
			Separate_output_struct s_out = mask.Separate(NMF.B, NMF.W, comp_ind, V);

			// System.out.println("\nF = " + F + "\tT = " + T + "\n" + V.get(50,
			// 50));
			///////////////////////////////////////////////////////////////////////////

			///// calculate | V |
			V_abs = tool.abs(V);
			///////////////////////////////////////////////////////////////////////////

			////////////////////////////////////////////////////////////////////////

			// ////apply STFT inverse///////////////////////////////
			STFT stft = new STFT();

			double[] speech = stft.istft_single(s_out.S, nsampl);

			wavfile.close();
			WavFile speechWavFile = WavFile.newWavFile(new File("speech.wav"), 1, speech.length, wavfile.getValidBits(),
					wavfile.getSampleRate());

			speechWavFile.writeFrames(speech, speech.length);

			double[] noise = stft.istft_single(s_out.N, nsampl);
			WavFile noiseWavFile = WavFile.newWavFile(new File("noise.wav"), 1, noise.length, wavfile.getValidBits(),
					wavfile.getSampleRate());
			noiseWavFile.writeFrames(noise, noise.length);

			noiseWavFile.close();
			speechWavFile.close();

			V = pp.preprocessing("destroy_the_evidence.wav", false);
			wavfile = pp.getWavfile();
			nsampl = pp.getNsample();
			F = V.getRows();
			T = V.getColumns();
			 V_abs = tool.abs(V);

			drowLogSpectrum(V_abs, "orginalSpeech");

			V = pp.preprocessing("noise.wav", false);
			wavfile = pp.getWavfile();
			nsampl = pp.getNsample();
			F = V.getRows();
			T = V.getColumns();
			 V_abs = tool.abs(V);

			drowLogSpectrum(V_abs, "noise");

			V = pp.preprocessing("speech.wav", false);
			wavfile = pp.getWavfile();
			nsampl = pp.getNsample();
			F = V.getRows();
			T = V.getColumns();
			 V_abs = tool.abs(V);

			drowLogSpectrum(V_abs, "speech");
			//
			// double[] x_inv = stft.istft_single(V, nsampl);
			// WavFile newWavFile = WavFile.newWavFile(new File("out.wav"), 1,
			// wavfile.getNumFrames(),
			// wavfile.getValidBits(), wavfile.getSampleRate());
			// newWavFile.writeFrames(x_inv, x_inv.length);
			// /////////////////////////////////////////////////////////////////////////////

			// Learn learn = new Learn();
			// learn.addToBasisDictionary(speech_dec_path, "speech_2.wav",
			// SystemSettings.SpeechSourceColInDec, false,
			// 100);
			// learn.deleteFromDictionary(speech_dec_path, new int[]{1},
			// SystemSettings.SpeechSourceColInDec);

		} catch (IOException | WavFileException e) {
			e.printStackTrace();
		}
	}

	public static void drowLogSpectrum(DoubleMatrix data, String filename) {
		try {
			MatrixTools tool = new MatrixTools();
			double[][] arr = tool.log(data).toArray2();

			BufferedImage b = getSpectrogram(arr);
			File img = new File(filename + ".jpg");
			boolean state = ImageIO.write(b, "JPG", img);

			System.out.println("end,\t" + state);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static BufferedImage getSpectrogram(double[][] arr) {
		double min = 0, max = arr[0][0];
		ColorMap cmap = ColorMap.getJet();

		int width = arr[0].length, height = arr.length;
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[0].length; j++) {
				if (arr[i][j] < min) {
					min = arr[i][j];
				} else if (arr[i][j] > max) {
					max = arr[i][j];
				}
			}
		}

		double minIntensity = Math.abs(min);
		double maxIntensity = max + minIntensity;
		int maxYIndex = height - 1;

		BufferedImage spectrogram = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		double offsetFactor = 0;
		double scaleFactor = ((0x3f + offsetFactor) / maxIntensity);

		for (int i = 0; i < width; i++) {
			for (int j = maxYIndex; j >= 0; j--) {

				/*
				 * Adjust the grey value to make a value of 0 to mean white and
				 * a value of 0xff to mean black.
				 */
				int grey = (int) ((arr[j][i] + minIntensity) * scaleFactor - offsetFactor);
				// System.out.println(grey);

				// use grey as an index into the colormap;
				spectrogram.setRGB(i, maxYIndex - j, cmap.getColor(grey));
			}
		}

		return spectrogram;
	}
}
