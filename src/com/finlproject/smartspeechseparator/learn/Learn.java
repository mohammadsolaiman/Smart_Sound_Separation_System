package com.finlproject.smartspeechseparator.learn;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;

import com.finalproject.smartspeechseparator.audioprocessing.WavFileException;
import com.finalproject.smartspeechseparator.database.Basis_row;
import com.finalproject.smartspeechseparator.database.DB_handler;
import com.finalproject.smartspeechseparator.database.SOUNDTYPE;
import com.finalproject.smartspeechseparator.database.Sound_row;
import com.finalproject.smartspeechseparator.debug.DebugLogger;
import com.finalproject.smartspeechseparator.general.Cell;
import com.finalproject.smartspeechseparator.general.SystemSettings;
import com.finalproject.smartspeechseparator.transform.MatrixTools;
import com.finalproject.smartspeechseparator.transform.Preprocessor;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class Learn {

	DebugLogger debug;
	
	@FXML
	private TextArea consol;

	private int sampleLength = SystemSettings.sampleLemgth;
	
	private double mu = 0.001;
	
	public int getSampleLength() {
		return sampleLength;
	}

	public void setSampleLength(int sampleLength) {
		this.sampleLength = sampleLength;
	}

	public DebugLogger getDebug() {
		return debug;
	}

	public void setDebug(DebugLogger debug) {
		this.debug = debug;
	}

	
	public double getMu() {
		return mu;
	}

	public void setMu(double mu) {
		this.mu = mu;
	}

	public TextArea getConsol() {
		return consol;
	}

	public void setConsol(TextArea consol) {
		this.consol = consol;
		this.debug.setConsole(consol);
	}

	public Learn() {
		debug = new DebugLogger();
	}

	public boolean InsertAndLearn(String wav_path, SOUNDTYPE type, int stft_wlen, int K, int niter,
			boolean tackSample) {

		DB_handler db = new DB_handler();

		Cell comp_inds = new Cell();

		MatrixTools tool = new MatrixTools();
		Preprocessor pp = new Preprocessor();
		Initializer init = new Initializer();
		comp_inds.add(tool.makeIndecesRange(0, K - 1));

		pp.setSampleLength(this.sampleLength);
		pp.setStft_wlen(stft_wlen);
		
		Basis_row br = db.getLastBasie(type, stft_wlen, K);
		DoubleMatrix B_init, V_abs;
		int F;

		try {
			db.switchAutoCommit(false);

			Sound_row record = new Sound_row();
			db.InsertSound(wav_path, type, record);

			String path = record.getDir() + record.getName();
			if (br == null) {
				pp.preprocessing(path, tackSample);
				V_abs = tool.abs(pp.getV());
				F = V_abs.getRows();

				B_init = init.initialize_B(V_abs, F, K);

			} else {
				B_init = tool.String2DoubleMatrix(br.getData().toString());

			}

			StringBuilder B = getBasisFromSoundSource(wav_path, B_init, K, stft_wlen, tackSample, niter);
			
			db.UpdateSoundRowByID(record.getId(), true);
			db.InsertBasis(record, B, stft_wlen, K);

			db.commit();

		} catch (Exception e) {
			debug.log("Learn.InsertAndLearn error  learning failed!");
			e.printStackTrace();
		}

		db.close();
		return true;
	}

	public void learnDBSoundType(SOUNDTYPE type, int stft_wlen, int K, int niter, boolean tackSample) {
		DB_handler db = new DB_handler();
		Initializer init = new Initializer();
		Preprocessor pp = new Preprocessor();
		MatrixTools tool = new MatrixTools();
		Cell comp_inds = new Cell();

		comp_inds.add(tool.makeIndecesRange(0, K - 1));

		List<Sound_row> sounds_can_be_used = db.getSoundsWithType_NotK_wlen(type, stft_wlen, K);

		if (sounds_can_be_used == null || sounds_can_be_used.size() == 0) {
			System.out.println("WARNINH: learnDBSoundType    0 data processed!");
			return;
		}

		Basis_row br = db.getLastBasie(type, stft_wlen, K);
		DoubleMatrix B_init, V_abs;
		int F;
		if (br == null) {
			try {
				pp.preprocessing(sounds_can_be_used.get(0).getDir() + sounds_can_be_used.get(0).getName(), tackSample);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				debug.log("ERROR: Learn.learnDBSoundType  IOException " + e.getMessage());
				return;
			} catch (WavFileException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				debug.log("ERROR: Learn.learnDBSoundType  WavFileException " + e.getMessage());
				return;
			}
			V_abs = tool.abs(pp.getV());
			F = V_abs.getRows();

			B_init = init.initialize_B(V_abs, F, K);

		} else {
			B_init = tool.String2DoubleMatrix(br.getData().toString());

		}

		try {
			db.switchAutoCommit(false);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (Sound_row sr : sounds_can_be_used) {

			String path = sr.getDir() + sr.getName();
			StringBuilder B = getBasisFromSoundSource(path, B_init, K, stft_wlen, tackSample, niter, B_init);

			try {
				db.UpdateSoundRowByID(sr.getId(), true);
				db.InsertBasis(sr, B, stft_wlen, K);

				db.commit();
			} catch (Exception e) {
				e.printStackTrace();
				debug.log("##. " + path + "\tlearn failed!");
				continue;
			}
			debug.log("#. " + path + "\tlearn success!");

		}

		db.close();
		debug.log("\nDBlearn DONE!\n");
	}

	public List<StringBuilder> getBasisFromSoundSources(List<String> wav_pathes, DoubleMatrix B_init, int K,
			int stft_wlen, boolean takeSample, int niter) {

		List<StringBuilder> out = new ArrayList<StringBuilder>();

		DoubleMatrix newB = B_init;
		for (String path : wav_pathes) {
			StringBuilder sb = getBasisFromSoundSource(path, newB, K, stft_wlen, takeSample, niter, newB);
			out.add(sb);
		}

		return out;
	}

	public StringBuilder getBasisFromSoundSource(String wav_path, DoubleMatrix B_init, int K, int stft_wlen,
			boolean takeSample, int niter, DoubleMatrix B_out) {
		MatrixTools tool = new MatrixTools();
		StringBuilder out = new StringBuilder();
		try {
			Preprocessor pp = new Preprocessor();
			pp.setStft_wlen(stft_wlen);
			ComplexDoubleMatrix V = pp.preprocessing(wav_path, takeSample);
			int T = V.getColumns();
			DoubleMatrix V_abs = tool.abs(V);
			Initializer init = new Initializer();
			DoubleMatrix W_init = init.initialize_W(K, T);
			Cell comp_ind = new Cell();
			comp_ind.add(tool.makeIndecesRange(0, K - 1));

			//// RUN NMF_KL
			NMF_KL NMF = new NMF_KL(V_abs, B_init, W_init, comp_ind);
			NMF.setNiter(niter);
			NMF.setMu(this.mu);
			NMF.setState(STATE.unsupervised);
			new Thread(NMF).start();

			out = tool.DoubleMatrex2StringBuilder(NMF.B);
			B_out = NMF.B;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return out;
	}

	public StringBuilder getBasisFromSoundSource(String wav_path, DoubleMatrix B_init, int K, int stft_wlen,
			boolean takeSample, int niter) {
		MatrixTools tool = new MatrixTools();
		StringBuilder out = new StringBuilder();
		try {
			Preprocessor pp = new Preprocessor();
			pp.setStft_wlen(stft_wlen);
			pp.setSampleLength(this.sampleLength);
			ComplexDoubleMatrix V = pp.preprocessing(wav_path, takeSample);
			int F = V.getRows(), T = V.getColumns();
			DoubleMatrix V_abs = tool.abs(V);
			Initializer init = new Initializer();
			DoubleMatrix W_init = init.initialize_W(K, T);
			Cell comp_ind = new Cell();
			comp_ind.add(tool.makeIndecesRange(0, K - 1));

			//// RUN NMF_KL
			NMF_KL NMF = new NMF_KL(V_abs, B_init, W_init, comp_ind);
			NMF.setNiter(niter);
			NMF.RunUnupervised_NMF();

			out = tool.DoubleMatrex2StringBuilder(NMF.B);

		} catch (Exception e) {
			e.printStackTrace();
			debug.log("##. " + wav_path + "\tlearn failed!");
			return null;
		}

		return out;
	}

	public void LearnBasisDictionary(String dec_path, List<String> soundPaths, int K, boolean takeSample, int niter) {
		for (String path : soundPaths) {
			addToBasisDictionary(dec_path, path, K, takeSample, niter);
		}
	}

	public boolean addToBasisDictionary(String dec_path, String soundPath, int K, boolean takeSample, int niter) {
		try {
			MatrixTools tool = new MatrixTools();

			DoubleMatrix dec_B = tool.File2DoubleMatrix(dec_path);

			// pre-processing ...
			Preprocessor pp = new Preprocessor();
			ComplexDoubleMatrix V = pp.preprocessing(soundPath, takeSample);
			int F = V.getRows(), T = V.getColumns();

			///// calculate | V |
			DoubleMatrix V_abs = tool.abs(V);

			// INITIALIZATION
			Initializer init = new Initializer();
			DoubleMatrix B_init = dec_B.getColumns() != 0
					? dec_B.getColumns(tool.makeIndecesRange(dec_B.getColumns() - K, dec_B.getColumns() - 1))
					: init.initialize_B(V_abs, F, K);
			DoubleMatrix W_init = init.initialize_W(K, T);
			Cell comp_ind = new Cell();
			comp_ind.add(tool.makeIndecesRange(0, K - 1));

			//// RUN NMF_KL
			NMF_KL NMF = new NMF_KL(V_abs, B_init, W_init, comp_ind);
			NMF.setNiter(niter);
			NMF.RunUnupervised_NMF();

			dec_B = dec_B.getRows() == 0 ? NMF.B : DoubleMatrix.concatHorizontally(dec_B, NMF.B);

			tool.DoubleMatrex2File(dec_B, dec_path);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public void deleteFromDictionary(String dic_path, int[] inds, int K) {
		MatrixTools tool = new MatrixTools();

		try {
			DoubleMatrix dec_B = tool.File2DoubleMatrix(dic_path);

			List<Integer> li = new ArrayList<Integer>();
			for (int i = 0; i < dec_B.getColumns(); i++) {
				boolean valid = true;
				for (int ind : inds) {
					int rangeStart = ind * K, rangeEnd = rangeStart + K - 1;
					if (i >= rangeStart && i <= rangeEnd) {
						valid = false;
						break;
					}
				}
				if (valid) {
					li.add(i);
				}
			}

			int[] ind_li = new int[li.size()];
			for (int i = 0; i < ind_li.length; i++) {
				ind_li[i] = li.get(i);
			}

			DoubleMatrix newB = dec_B.getColumns(ind_li);
			tool.DoubleMatrex2File(newB, dic_path);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
