package com.finlproject.smartspeechseparator.user.UI;

import java.awt.Checkbox;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;

import com.finalproject.smartspeechseparator.audioprocessing.FILTERNAME;
import com.finalproject.smartspeechseparator.audioprocessing.Mask;
import com.finalproject.smartspeechseparator.audioprocessing.WavFile;
import com.finalproject.smartspeechseparator.audioprocessing.WavFileException;
import com.finalproject.smartspeechseparator.database.Basis_row;
import com.finalproject.smartspeechseparator.database.DB_handler;
import com.finalproject.smartspeechseparator.database.SOUNDTYPE;
import com.finalproject.smartspeechseparator.debug.DebugLogger;
import com.finalproject.smartspeechseparator.general.Cell;
import com.finalproject.smartspeechseparator.general.Separate_output_struct;
import com.finalproject.smartspeechseparator.transform.Analyzer;
import com.finalproject.smartspeechseparator.transform.MatrixTools;
import com.finalproject.smartspeechseparator.transform.Preprocessor;
import com.finalproject.smartspeechseparator.transform.STFT;
import com.finalproject.smartspeechseparator.transform.SpectrumJPanel;
import com.finalproject.smartspeechseparator.transform.SpectrumType;
import com.finlproject.smartspeechseparator.learn.Initializer;
import com.finlproject.smartspeechseparator.learn.Learn;
import com.finlproject.smartspeechseparator.learn.NMF_KL;
import com.finlproject.smartspeechseparator.learn.STATE;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class UIController implements Initializable {

	private Stage stage;
	String DIR;
	@FXML
	private ListView<String> selectedFilesList;

	@FXML
	private TextField learnDirTF;

	@FXML
	private TextArea learn_logTA;

	@FXML
	private Label statusLable;

	@FXML
	private TextField learn_K;

	@FXML
	private TextField learn_num_iter;

	@FXML
	private TextField learn_MU_TF;

	@FXML
	private TextField learn_sampleLength_TF;

	@FXML
	private TextField learn_stft_wlen_TF;

	@FXML
	private CheckBox learn_takeSample_CB;

	@FXML
	private ChoiceBox<String> learn_soundType_chooesBox;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

	public void init(Stage stage) {
		this.stage = stage;

	}

	public void Open(String filePath) throws IllegalArgumentException {
		File f = new File(filePath);
		Desktop dt = Desktop.getDesktop();
		try {
			dt.open(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void PlaySound() {
		List<String> ui_selected = selectedFilesList.getSelectionModel().getSelectedItems();
		if (ui_selected == null || ui_selected.size() == 0) {
			statusLable.setText("No Selected Items!");
			return;
		}
		try {

			Open(DIR + File.separator + ui_selected.get(0));
			statusLable.setText("Playing Sound...");

		} catch (Exception e) {

			learn_logTA
					.setText(learn_logTA.getText() + "\nPLAY FILE ERROR: " + ui_selected.get(0) + " " + e.getMessage());
		}

	}

	public void onLearnDrowSpectrogram() {
		List<String> ui_selected = selectedFilesList.getSelectionModel().getSelectedItems();
		if (ui_selected == null || ui_selected.size() == 0) {
			statusLable.setText("No Selected Items!");
			return;
		}

		drowSpectrogram(ui_selected, DIR);
	}

	public void learn() {
		List<String> ui_selected = selectedFilesList.getSelectionModel().getSelectedItems();
		if (ui_selected == null || ui_selected.size() == 0) {
			statusLable.setText("No Selected Items!");
			return;
		}

		for (String filename : ui_selected) {
			statusLable.setText("Learning..." + filename);
			learn_logTA.setText(learn_logTA.getText() + "\n\n" + "Learning " + filename + "...");
			Learn learn = new Learn();
			SOUNDTYPE type;
			switch (learn_soundType_chooesBox.getSelectionModel().getSelectedItem()) {
			case "speech": {
				type = SOUNDTYPE.speech;
				break;
			}
			case "noise": {
				type = SOUNDTYPE.noise;
				break;
			}
			case "equelbrume": {
				type = SOUNDTYPE.eq;
				break;
			}
			default: {
				return;
			}
			}
			learn.setConsol(this.learn_logTA);
			learn.setSampleLength(Integer.parseInt(learn_sampleLength_TF.getText()));
			learn.InsertAndLearn(DIR + File.separator + filename, type, Integer.parseInt(learn_stft_wlen_TF.getText()),
					Integer.parseInt(learn_K.getText()), Integer.parseInt(learn_num_iter.getText()),
					learn_takeSample_CB.isSelected());
			learn_logTA.setText(learn_logTA.getText() + "\n\n" + filename + "...........................DONE!");

		}

		statusLable.setText("DONE!...");
	}

	public void drowSpectrogram(List<String> ui_selected, String dir) {
		for (String filename : ui_selected) {

			Analyzer analyzer = new Analyzer();
			JFrame frame = new JFrame(filename);

			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			SpectrumJPanel p = new SpectrumJPanel();
			p.setAnalyzer(analyzer);

			frame.add(p);
			frame.setMinimumSize(new Dimension(300, 300));

			frame.setResizable(true);
			frame.setVisible(true);
			try {
				// p.getAnalyzer().drowLogSpectrogramOnJPanel(p, "input.wav");
				p.getAnalyzer().drowSpectrogramOnJPanel(p, dir + File.separator + filename, true, SpectrumType.log);
			} catch (Exception e) {
				statusLable.setText("Spectrogram drowing failed!!\t" + filename);
				learn_logTA.setText(learn_logTA.getText() + "\nERROR: " + e.getMessage());
				e.printStackTrace();
			}

			p.addComponentListener(new ComponentListener() {

				@Override
				public void componentShown(ComponentEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void componentResized(ComponentEvent e) {

					try {
						// analyzer.drowLogSpectrogramOnJPanel((JPanel)e.getComponent(),
						// "input.wav");
						Analyzer analyzer = ((SpectrumJPanel) e.getComponent()).getAnalyzer();
						analyzer.JPanelResize((JPanel) e.getComponent(), analyzer.isShowCartesian());
					} catch (Exception ee) {
						ee.printStackTrace();
					}
				}

				@Override
				public void componentMoved(ComponentEvent e) {
					// TODO Auto-generated method stub
					try {
						// analyzer.drowLogSpectrogramOnJPanel((JPanel)e.getComponent(),
						// "input.wav");
						Analyzer analyzer = ((SpectrumJPanel) e.getComponent()).getAnalyzer();
						analyzer.JPanelResize((JPanel) e.getComponent(), analyzer.isShowCartesian());
					} catch (Exception ee) {
						ee.printStackTrace();
					}

				}

				@Override
				public void componentHidden(ComponentEvent e) {
					// TODO Auto-generated method stub

				}
			});
		}
	}

	@FXML
	private ImageView sep_Mix_spectroImgView;
	@FXML
	private ImageView sep_speech_spectroImgView;
	@FXML
	private ImageView sep_noise_spectroImgView;

	@FXML
	private TextField sep_K_sTF;
	@FXML
	private TextField sep_K_nTF;
	@FXML
	private TextField sep_niterTF;
	@FXML
	private TextField sep_unsupervizedstft_wlen;

	@FXML
	private TextField sep_MuTF;
	@FXML
	private TextField sep_sigmaFilterTF;

	private String mix_path, speech_outPath, noise_outPath;
	private boolean is_sepEND = false;
	public void separateAction() {
		is_sepEND = false;

		File mixedfile = new File(mixed_path_TF.getText());

		speech_outPath = mixedfile.getParent()+File.separator+mixedfile.getName().substring(0, mixedfile.getName().length() - 4)+"_speech.wav";
		noise_outPath = mixedfile.getParent()+File.separator+mixedfile.getName().substring(0, mixedfile.getName().length() - 4)+"_noise.wav";

		MatrixTools tool = new MatrixTools();

		Initializer _init = new Initializer();

		FILTERNAME filter;
		String filterName = sep_choose_filter.getSelectionModel().getSelectedItem();
		double sigma =Double.parseDouble(sep_sigmaFilterTF.getText());
		
		switch(filterName){
		case "winner":
			filter = FILTERNAME.winner;
			break;
		case "linear":
			filter = FILTERNAME.linear;
			break;
		case "hard":
			filter = FILTERNAME.hard;
			break;
		default:
			statusLable.setText("Pleas select the filter!");
				return;
		}
		
		String mode = sep_choose_mode.getSelectionModel().getSelectedItem();
		int K_speech = Integer.parseInt(sep_K_sTF.getText());
		int K_noise = Integer.parseInt(sep_K_nTF.getText());
		int niter = Integer.parseInt(sep_niterTF.getText());
		double Mu = Double.parseDouble(sep_MuTF.getText());
		int stft_wlen = Integer.parseInt(sep_choose_stft_wlen.getSelectionModel().getSelectedItem());
		List<Basis_row> Li_B_s = new ArrayList<>(), Li_B_n = new ArrayList<>(), Li_B_eq = new ArrayList<>();
		STATE state;
		DB_handler db = new DB_handler();

		Preprocessor pp = new Preprocessor();
		pp.setStft_wlen(stft_wlen);


		try {
			DoubleMatrix B = new DoubleMatrix(), W = new DoubleMatrix(), V_abs ;

			ComplexDoubleMatrix V = pp.preprocessing(mixedfile.getPath(), false);
			V_abs = tool.abs(V);
			//V_abs = tool.spectrumDenoising(V_abs, 0.002);

			switch (mode) {
			case ("supervised"): {
				state = STATE.supervised;
				statusLable.setText("Separation supervized PLEAS WAIT..!");

				try {
					Li_B_s = db.getAllBasisOf(SOUNDTYPE.speech, stft_wlen);
					Li_B_n = db.getAllBasisOf(SOUNDTYPE.noise, stft_wlen);
				} catch (Exception e) {
					statusLable.setText("ERROR: getting basis from database!!");
					e.printStackTrace();
					return;
				}
				if(Li_B_n.size() == 0 || Li_B_s.size() == 0)
				{
					statusLable.setText("ERROR: SEPARATION FAIL SINCE NO DATA RETREAVED FROM DATABASE!");
					return;
				}
				
				DoubleMatrix B_s = tool.String2DoubleMatrix(Li_B_s.get(0).getData().toString());
				DoubleMatrix B_n = tool.String2DoubleMatrix(Li_B_n.get(0).getData().toString());
				for(int i = 1 ;i<Li_B_s.size(); i++){
					B_s = DoubleMatrix.concatHorizontally(B_s, tool.String2DoubleMatrix(Li_B_s.get(i).getData().toString()));
				}
				
				for(int i = 1 ;i<Li_B_n.size(); i++){
					B_n = DoubleMatrix.concatHorizontally(B_n, tool.String2DoubleMatrix(Li_B_n.get(i).getData().toString()));
				}
				
				int F = B_s.getRows(), T = V_abs.getColumns(), K_s = B_s.getColumns(), K_n = B_n.getColumns() ;
				
				//Initialize W
				W =_init.initialize_W(K_s+K_n, T);
				Cell comp_inds = new Cell();
				comp_inds.add(tool.makeIndecesRange(0, K_s-1));
				comp_inds.add(tool.makeIndecesRange(K_s, K_s+K_n-1));
				
				B = DoubleMatrix.concatHorizontally(B_s, B_n);
				NMF_KL NMF = new NMF_KL(V_abs, B, W, comp_inds);
				NMF.setNiter(niter);
				NMF.setMu(Mu);
				NMF.setState(state);
				NMF.RunSupervised_NMF();
				
				Mask mask = new Mask();
				Separate_output_struct separated = mask.Separate(NMF.getB(), NMF.getW(), comp_inds, V, filter, sigma);
			
				STFT stft = new STFT();
				double[] speech_out_buffer = stft.istft_single(separated.S, pp.getNsample());
				double[] noise_out_buffer = stft.istft_single(separated.N, pp.getNsample());
				
				WavFile wavfile = pp.getWavfile();
				WavFile noiseWavFile = WavFile.newWavFile(new File(noise_outPath), 1, noise_out_buffer.length, wavfile.getValidBits(),
						wavfile.getSampleRate());
				noiseWavFile.writeFrames(noise_out_buffer, noise_out_buffer.length);
				
				WavFile speechWavFile = WavFile.newWavFile(new File(speech_outPath), 1, speech_out_buffer.length, wavfile.getValidBits(),
						wavfile.getSampleRate());
				speechWavFile.writeFrames(speech_out_buffer, speech_out_buffer.length);
				
				wavfile.close();
				speechWavFile.close();
				noiseWavFile.close();
				statusLable.setText("Separation DONE!");
				is_sepEND = true;
				
				break;
			}
			case ("simi_supervized"): {
				state = STATE.simisupervised;
				statusLable.setText("Separation simi_supervized PLEAS WAIT..!");
				
				try {
					Li_B_s = db.getAllBasisOf(SOUNDTYPE.speech, stft_wlen);
					Li_B_eq = db.getAllBasisOf(SOUNDTYPE.eq, stft_wlen);
				} catch (Exception e) {
					statusLable.setText("ERROR: getting basis from database!!");
					e.printStackTrace();
					return;
				}
				if(Li_B_s.size() == 0)
				{
					statusLable.setText("ERROR: SEPARATION FAIL SINCE NO DATA RETREAVED FROM DATABASE!");
					return;
				}
				
				DoubleMatrix B_s = tool.String2DoubleMatrix(Li_B_s.get(0).getData().toString());
				
				
				//DoubleMatrix B_n ;
				for(int i = 1 ;i<Li_B_s.size(); i++){
					B_s = DoubleMatrix.concatHorizontally(B_s, tool.String2DoubleMatrix(Li_B_s.get(i).getData().toString()));
				}
				
				int K_eq = 0;
				int F = B_s.getRows(), T = V_abs.getColumns(), K_s = B_s.getColumns(), K_n =Integer.parseInt(sep_K_nTF.getText()) ;

				if(Li_B_eq.size() > 0){
					DoubleMatrix B_eq = tool.String2DoubleMatrix(Li_B_eq.get(0).getData().toString());
					for(int i = 1 ;i<Li_B_s.size(); i++){
						B_eq = DoubleMatrix.concatHorizontally(B_eq, tool.String2DoubleMatrix(Li_B_eq.get(i).getData().toString()));
					}
					
					K_eq = B_eq.getColumns();
					B_s = DoubleMatrix.concatHorizontally(B_s,B_eq);
				}
				
				DoubleMatrix B_n = _init.initialize_B(V_abs, F, K_n);
				
				W = _init.initialize_W(K_s+K_n+K_eq, T);
				
				B = DoubleMatrix.concatHorizontally(B_s, B_n);
				
				Cell comp_inds_forNMF = new Cell();
				Cell comp_inds_forMask = new Cell();
				
				comp_inds_forNMF.add(tool.makeIndecesRange(0, K_s + K_eq-1));
				comp_inds_forNMF.add(tool.makeIndecesRange( K_s + K_eq , K_s + K_eq +K_n-1));

				comp_inds_forMask.add(tool.makeIndecesRange(0, K_s -1));
				comp_inds_forMask.add(tool.makeIndecesRange(K_s, K_s+K_eq+K_n-1));
				
				
				
				NMF_KL NMF = new NMF_KL(V_abs, B, W, comp_inds_forNMF);
				NMF.setNiter(niter);
				NMF.setMu(Mu);
				NMF.setState(state);
				NMF.RunSimiSupervised_NMF();
				
				Mask mask = new Mask();
				Separate_output_struct separated = mask.Separate(NMF.getB(), NMF.getW(), comp_inds_forMask, V, filter, sigma);
			
				STFT stft = new STFT();
				double[] speech_out_buffer = stft.istft_single(separated.S, pp.getNsample());
				double[] noise_out_buffer = stft.istft_single(separated.N, pp.getNsample());
				
				WavFile wavfile = pp.getWavfile();
				WavFile noiseWavFile = WavFile.newWavFile(new File(noise_outPath), 1, noise_out_buffer.length, wavfile.getValidBits(),
						wavfile.getSampleRate());
				noiseWavFile.writeFrames(noise_out_buffer, noise_out_buffer.length);
				
				WavFile speechWavFile = WavFile.newWavFile(new File(speech_outPath), 1, speech_out_buffer.length, wavfile.getValidBits(),
						wavfile.getSampleRate());
				speechWavFile.writeFrames(speech_out_buffer, speech_out_buffer.length);
				
				wavfile.close();
				speechWavFile.close();
				noiseWavFile.close();
				statusLable.setText("Separation DONE!");
				is_sepEND = true;

				break;
			}
			case ("unsupervized"): {
				state = STATE.unsupervised;
				
				statusLable.setText("Separation Unsupervized PLEAS WAIT..!");
				String stftWlenTFT = sep_unsupervizedstft_wlen.getText();
				
				if(!stftWlenTFT.equals("") && stftWlenTFT!=null){
					stft_wlen = Integer.parseInt(stftWlenTFT);
				}
				
				int F = V_abs.getRows(), T = V_abs.getColumns(), K_s =Integer.parseInt(sep_K_sTF.getText()), K_n =Integer.parseInt(sep_K_nTF.getText()) ;

				
				B = _init.initialize_B(V_abs, F, K_n+K_s);
				
				W = _init.initialize_W(K_s+K_n, T);
								
				Cell comp_inds_forNMF = new Cell();
				
				comp_inds_forNMF.add(tool.makeIndecesRange(0, K_s -1));
				comp_inds_forNMF.add(tool.makeIndecesRange( K_s  , K_s  +K_n-1));
				
				
				NMF_KL NMF = new NMF_KL(V_abs, B, W, comp_inds_forNMF);
				NMF.setNiter(niter);
				NMF.setMu(Mu);
				NMF.setState(state);
				NMF.RunUnupervised_NMF();
				
				Mask mask = new Mask();
				Separate_output_struct separated = mask.Separate(NMF.getB(), NMF.getW(), comp_inds_forNMF, V, filter, sigma);
			
				STFT stft = new STFT();
				double[] speech_out_buffer = stft.istft_single(separated.S, pp.getNsample());
				double[] noise_out_buffer = stft.istft_single(separated.N, pp.getNsample());
				
				WavFile wavfile = pp.getWavfile();
				WavFile noiseWavFile = WavFile.newWavFile(new File(noise_outPath), 1, noise_out_buffer.length, wavfile.getValidBits(),
						wavfile.getSampleRate());
				noiseWavFile.writeFrames(noise_out_buffer, noise_out_buffer.length);
				
				WavFile speechWavFile = WavFile.newWavFile(new File(speech_outPath), 1, speech_out_buffer.length, wavfile.getValidBits(),
						wavfile.getSampleRate());
				speechWavFile.writeFrames(speech_out_buffer, speech_out_buffer.length);
				
				wavfile.close();
				speechWavFile.close();
				noiseWavFile.close();
				statusLable.setText("Separation DONE!");
				is_sepEND = true;
				break;
			}
			default:
				return;

			}

			drowSpeechSpectrogram();
			drowNoiseSpectrogram();
		} catch (IOException e1) {
			statusLable.setText("Read file error!!");
			e1.printStackTrace();
			return;
		} catch (WavFileException e1) {
			statusLable.setText("WAVFile Exception!! " + e1.getMessage());

			e1.printStackTrace();
			return;
		}

	}

	public void drowMixedSpectrogram() {
		Analyzer analyzer = new Analyzer();

		try {
			BufferedImage BFimg = analyzer.getLogSpectrogram(mixed_path_TF.getText());
			Image img = SwingFXUtils.toFXImage(BFimg, null);

			sep_Mix_spectroImgView.setImage(img);
			sep_Mix_spectroImgView.setFitWidth(500);
			sep_Mix_spectroImgView.setFitHeight(100);
			sep_Mix_spectroImgView.setPreserveRatio(false);
			sep_Mix_spectroImgView.setSmooth(true);
			sep_Mix_spectroImgView.setCache(true);

		} catch (Exception e) {
			statusLable.setText("Spectrogram drowing failed!");
			e.printStackTrace();
		}
	}

	public void drowSpeechSpectrogram() {
		Analyzer analyzer = new Analyzer();

		try {
			BufferedImage BFimg = analyzer.getLogSpectrogram(speech_outPath);
			Image img = SwingFXUtils.toFXImage(BFimg, null);

			sep_speech_spectroImgView.setImage(img);
			sep_speech_spectroImgView.setFitWidth(245);
			sep_speech_spectroImgView.setFitHeight(100);
			sep_speech_spectroImgView.setPreserveRatio(false);
			sep_speech_spectroImgView.setSmooth(true);
			sep_speech_spectroImgView.setCache(true);

		} catch (Exception e) {
			statusLable.setText("speech Spectrogram drowing failed!");
			e.printStackTrace();
		}
	}
	
	public void drowNoiseSpectrogram() {
		Analyzer analyzer = new Analyzer();

		try {
			BufferedImage BFimg = analyzer.getLogSpectrogram(noise_outPath);
			Image img = SwingFXUtils.toFXImage(BFimg, null);

			sep_noise_spectroImgView.setImage(img);
			sep_noise_spectroImgView.setFitWidth(245);
			sep_noise_spectroImgView.setFitHeight(100);
			sep_noise_spectroImgView.setPreserveRatio(false);
			sep_noise_spectroImgView.setSmooth(true);
			sep_noise_spectroImgView.setCache(true);

		} catch (Exception e) {
			statusLable.setText("noise Spectrogram drowing failed!");
			e.printStackTrace();
		}
	}
	
	public void onImgClickEvent_Mix() {
		File file = new File(mixed_path_TF.getText());
		List<String> l = new ArrayList<>();
		l.add(file.getName());
		drowSpectrogram(l, file.getParent());
	}

	public void onImgClickEvent_Speech() {
		if(!is_sepEND)
			return;
		File file = new File(speech_outPath);
		List<String> l = new ArrayList<>();
		l.add(file.getName());
		drowSpectrogram(l, file.getParent());
	}
	public void onImgClickEvent_Noise() {
		if(!is_sepEND)
			return;
		File file = new File(noise_outPath);
		List<String> l = new ArrayList<>();
		l.add(file.getName());
		drowSpectrogram(l, file.getParent());
	}
	
	public void clearAllDB() {
		DB_handler db = new DB_handler();
		try {
			db.clearDB();
			statusLable.setText("Database Cleared!");
		} catch (Exception e) {
			statusLable.setText("ERROR : Clearing database failed!!");
			e.printStackTrace();
		}
	}

	public void deleteSelected() {
		try {
			List<String> ui_selected = selectedFilesList.getSelectionModel().getSelectedItems();
			if (ui_selected == null || ui_selected.size() == 0) {
				statusLable.setText("No Selected Items!");
				return;
			}
			selectedFilesList.getItems().removeAll(ui_selected);
			statusLable.setText("Items Removed!");
		} catch (Exception e) {
			learn_logTA.setText(learn_logTA.getText() + "\n" + e.toString());
			statusLable.setText("FAILE!");
		}
	}

	@FXML
	private ChoiceBox<String> sep_choose_mode;

	@FXML
	private ChoiceBox<String> sep_choose_stft_wlen;

	@FXML
	private ChoiceBox<String> sep_choose_filter;
	@FXML
	private TextField mixed_path_TF;

	public void sepPlayMixSound() {
		String path = mixed_path_TF.getText();
		if (path == null || path == "") {
			statusLable.setText("No Selected Items!");
			return;
		}
		try {

			Open(path);
			statusLable.setText("Playing Sound...");

		} catch (Exception e) {

			statusLable.setText("PLAY FILE ERROR: " + path + " " + e.getMessage());
			e.printStackTrace();
		}
	}
	public void sepPlaySpeechSound() {
		String path = speech_outPath;
		if (path == null || path == "") {
			statusLable.setText("No Selected Items!");
			return;
		}
		try {

			Open(path);
			statusLable.setText("Playing Sound...");

		} catch (Exception e) {

			statusLable.setText("PLAY FILE ERROR: " + path + " " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void sepPlayNoiseSound() {
		String path = noise_outPath;
		if (path == null || path == "") {
			statusLable.setText("No Selected Items!");
			return;
		}
		try {

			Open(path);
			statusLable.setText("Playing Sound...");

		} catch (Exception e) {

			statusLable.setText("PLAY FILE ERROR: " + path + " " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void BrowseForMixed() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose Files");
		fileChooser.setInitialDirectory(new File("C:\\Users\\Mohammad\\EclipsJEEWorkSpace"));
		fileChooser.getExtensionFilters().add(new ExtensionFilter("WAV", "*.wav"));
		File file = fileChooser.showOpenDialog(this.stage);
		if (file != null) {

			System.out.println(file.getPath());

			mixed_path_TF.setText(file.getPath());

			drowMixedSpectrogram();
		}

		sep_choose_stft_wlen.getItems().clear();
		sep_choose_filter.getItems().clear();

		sep_choose_filter.getItems().addAll("winner", "hard", "linear");
		sep_choose_filter.setValue("winner");

		DB_handler db = new DB_handler();
		List<Integer> stft_windows;
		try {
			stft_windows = db.getAllSTFT_Wlen();
			if (stft_windows != null && stft_windows.size() > 0) {
				for (Integer i : stft_windows) {
					sep_choose_stft_wlen.getItems().add(i.toString());
				}
				sep_choose_mode.getItems().clear();
				sep_choose_mode.getItems().addAll("supervised", "simi_supervized", "unsupervized");
				sep_choose_mode.setValue("unsupervized");
				sep_choose_stft_wlen.getSelectionModel().selectFirst();
			} else {
				sep_choose_mode.getItems().clear();
				sep_choose_mode.getItems().add("unsupervized");
				sep_choose_mode.setValue("unsupervized");

				sep_choose_stft_wlen.getItems().clear();
				sep_choose_stft_wlen.getItems().addAll("512", "1024", "2048", "4096");
			}
		} catch (Exception e) {
			sep_choose_mode.getItems().clear();
			sep_choose_mode.getItems().add("unsupervized");
			sep_choose_mode.setValue("unsupervized");

			sep_choose_stft_wlen.getItems().clear();
			sep_choose_stft_wlen.getItems().addAll("512", "1024", "2048", "4096");

			statusLable.setText("No STFT WINDOW LENGTH !!");
			e.printStackTrace();
		}

	}

	public void Browse() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose Files");
		fileChooser.setInitialDirectory(new File("C:\\Users\\Mohammad\\EclipsJEEWorkSpace"));
		fileChooser.getExtensionFilters().add(new ExtensionFilter("WAV", "*.wav"));
		List<File> files = fileChooser.showOpenMultipleDialog(this.stage);
		if (files != null) {
			ObservableList<String> items = FXCollections.observableArrayList();

			for (File file : files) {
				System.out.println(file.getPath());
				items.add(file.getName());
			}
			selectedFilesList.setItems(items);
			learnDirTF.setText(files.get(0).getParent());
			selectedFilesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

			DIR = files.get(0).getParent();
		}
		learn_logTA.setEditable(false);

		learn_soundType_chooesBox.getItems().clear();
		learn_soundType_chooesBox.getItems().addAll("speech", "noise", "equelbrume");
		learn_soundType_chooesBox.setValue("speech");
		learn_takeSample_CB.setSelected(true);
	}
}
