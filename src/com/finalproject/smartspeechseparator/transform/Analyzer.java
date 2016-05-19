package com.finalproject.smartspeechseparator.transform;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;

import com.finalproject.smartspeechseparator.audioprocessing.ColorMap;
import com.finalproject.smartspeechseparator.general.SystemSettings;

public class Analyzer {
	
	
	/////////TEST////////////////////////////////////////////
	public static void main(String[] args) {
		Analyzer analyzer = new Analyzer();
		JFrame frame = new JFrame("Image");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SpectrumJPanel p = new SpectrumJPanel();
		p.setAnalyzer(analyzer);

		frame.add(p);
		frame.setMinimumSize(new Dimension(200, 200));

		frame.setResizable(true);
		frame.setVisible(true);
		try {
			//p.getAnalyzer().drowLogSpectrogramOnJPanel(p, "input.wav");
			p.getAnalyzer().drowSpectrogramOnJPanel(p,"input.wav",true,SpectrumType.log);
		} catch (Exception e) {
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
					analyzer.JPanelResize((JPanel) e.getComponent(), analyzer.showCartesian);
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


	final int startX = 30, startY = 30;
	private BufferedImage bufferdImg;
	private SpectrumType spcType = SpectrumType.log;
	// Cartesian settings
	private int ntime, nfrequency;
	private int ntimebins_for_1sec, nfreqbins_for_1unit = 50;

	private boolean showCartesian = true;
	public boolean isShowCartesian() {
		return showCartesian;
	}

	public void setShowCartesian(boolean showCartesian) {
		this.showCartesian = showCartesian;
	}

	public int getNfreqbins_for_1unit() {
		return nfreqbins_for_1unit;
	}

	public void setNfreqbins_for_1unit(int nfreqbins_for_1unit) {
		this.nfreqbins_for_1unit = nfreqbins_for_1unit;
	}

	public int getNtimebins_for_1sec() {
		return ntimebins_for_1sec;
	}

	public void setNtimebins_for_1sec(int ntimebins_for_1sec) {
		this.ntimebins_for_1sec = ntimebins_for_1sec;
	}

	public double getNtime() {
		return ntime;
	}

	public void setNtime(int ntime) {
		this.ntime = ntime;
	}

	public double getNfrequency() {
		return nfrequency;
	}

	public void setNfrequency(int nfrequency) {
		this.nfrequency = nfrequency;
	}

	public BufferedImage getBufferdImg() {
		return bufferdImg;
	}

	public void setBufferdImg(BufferedImage bufferdImg) {
		this.bufferdImg = bufferdImg;
	}

	
	
	public SpectrumType getSpcType() {
		return spcType;
	}

	public void setSpcType(SpectrumType spcType) {
		this.spcType = spcType;
	}

	public void drowCartesian(JPanel panel, SpectrumType type) {
		int panelWidth = panel.getWidth(), panelHeight = panel.getHeight();
		int orgenX = startX, orgenY = panelHeight - startY;
		int numberOfTimePoints = ntime / ntimebins_for_1sec, numberOfFrequPoints = nfrequency / nfreqbins_for_1unit;
		int drowSpaceWidth = panelWidth - (2 * startX), drowSpaceHeight = panelHeight - (2 * startY);

		int timeSpace = drowSpaceWidth / numberOfTimePoints, freqSpace = drowSpaceHeight / numberOfFrequPoints;

		Graphics2D g2d = (Graphics2D) panel.getGraphics();
		// drow x
		for (int i = 0; i <= numberOfTimePoints; i++) {
			g2d.drawLine(orgenX + (i * timeSpace), orgenY, orgenX + (i * timeSpace), orgenY + 5);
			g2d.drawString(i + "", orgenX + (i * timeSpace) - 5, orgenY + 20);
		}

		// drow y
		for (int i = 0; i <= numberOfFrequPoints; i++) {
			g2d.drawLine(orgenX, orgenY - (i * freqSpace), orgenX - 5, orgenY - (i * freqSpace));

			double num2disp = type == SpectrumType.log ? Math.log((i * nfreqbins_for_1unit))
					: (i * nfreqbins_for_1unit);
			String s =(num2disp + "");
			g2d.drawString(s.length()>=5?s.substring(0, 5):s, orgenX - startX, orgenY - (i * freqSpace));
		}

		g2d.drawString("time sec", panelWidth - (2 * startX), panelHeight - startY + 10);
		g2d.drawString(type == SpectrumType.log ? "Log Freq." : "Freq", 5, startY - 5);
	}

	public void JPanelResize(JPanel panel, boolean withCartesian) {

		if (withCartesian) {
			BufferedImage bi = this.resize(this.bufferdImg, panel.getWidth() - (2 * startX) - 4,
					panel.getHeight() - (2 * startY) - 4);
			Graphics g = panel.getGraphics();

			g.drawRoundRect(startX, startY, bi.getWidth() + 2, bi.getHeight() + 2, 4, 4);
			g.drawImage(bi, startX + 2, startY + 2, bi.getWidth(), bi.getHeight(), panel);
			this.drowCartesian(panel, this.spcType);
		} else {
			BufferedImage bi = this.resize(this.bufferdImg, panel.getWidth() - 4, panel.getHeight() - 4);
			Graphics g = panel.getGraphics();

			g.drawRoundRect(0, 0, bi.getWidth() + 2, bi.getHeight() + 2, 4, 4);
			g.drawImage(bi, 2, 2, bi.getWidth(), bi.getHeight(), panel);
		}
	}

	public void drowSpectrogramOnJPanel(JPanel panel, String wavfilename, boolean withCartesian, SpectrumType type) {
		switch (type) {
		case log:
			try {
				drowLogSpectrogramOnJPanel(panel, wavfilename);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case normal:
			try {
				drowSpectrogramOnJPanel(panel, wavfilename);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
		this.JPanelResize(panel, withCartesian);
		this.setShowCartesian(withCartesian);
			
	}

	public void drowLogSpectrogramOnJPanel(JPanel panel, String wavfilename) throws Exception {

		try {

			BufferedImage bi = this.resize(this.getLogSpectrogram(wavfilename), panel.getWidth() - (2 * startX) - 4,
					panel.getHeight() - (2 * startY) - 4);
			Graphics g = panel.getGraphics();

			g.drawRoundRect(startX, startY, bi.getWidth() + 2, bi.getHeight() + 2, 4, 4);
			g.drawImage(bi, startX + 2, startY + 2, bi.getWidth(), bi.getHeight(), panel);

			this.drowCartesian(panel, this.spcType);

		} catch (Exception e) {
			throw new Exception("Analyzer Exception : drowLogSpectrogramOnFrame ," + e);

		}
	}

	public void drowSpectrogramOnJPanel(JPanel panel, String wavfilename) throws Exception {

		try {

			BufferedImage bi = this.resize(this.getSpectrogram(wavfilename), panel.getWidth() - (2 * startX) - 4,
					panel.getHeight() - (2 * startY) - 4);
			Graphics g = panel.getGraphics();

			g.drawRoundRect(startX, startY, bi.getWidth() + 2, bi.getHeight() + 2, 4, 4);
			g.drawImage(bi, startX + 2, startY + 2, bi.getWidth(), bi.getHeight(), panel);

			this.drowCartesian(panel, this.spcType);

		} catch (Exception e) {
			throw new Exception("Analyzer Exception : drowLogSpectrogramOnFrame ," + e);

		}
	}

	public BufferedImage resize(BufferedImage img, int newW, int newH) {
		Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
		BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);

		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return dimg;
	}

	public boolean drowSpectrogram(String filename, String savedImgName, String suffix) throws Exception {
		try {
			this.spcType = SpectrumType.normal;
			BufferedImage bi = this.getSpectrogram(filename);

			if (suffix.charAt(0) != '.')
				suffix = "." + suffix;

			String compFileName, formatName;
			switch (suffix) {
			case ".jpg":
				compFileName = savedImgName + suffix;
				formatName = "JPG";
				break;

			case ".png":
				compFileName = savedImgName + suffix;
				formatName = "PNG";
				break;
			default:
				suffix = ".jpg";
				compFileName = savedImgName + suffix;
				formatName = "JPG";
				break;
			}

			return ImageIO.write(bi, formatName, new File(compFileName));
		} catch (Exception e) {
			throw new Exception("Analyzer Exception : drowLogPowerSpectrogram ," + e);
		}
	}

	public boolean drowLogPowerSpectrogram(String filename, String savedImgName, String suffix) throws Exception {
		try {
			this.spcType = SpectrumType.log;
			BufferedImage bi = this.getLogSpectrogram(filename);

			if (suffix.charAt(0) != '.')
				suffix = "." + suffix;

			String compFileName, formatName;
			switch (suffix) {
			case ".jpg":
				compFileName = savedImgName + suffix;
				formatName = "JPG";
				break;

			case ".png":
				compFileName = savedImgName + suffix;
				formatName = "PNG";
				break;
			default:
				suffix = ".jpg";
				compFileName = savedImgName + suffix;
				formatName = "JPG";
				break;
			}

			return ImageIO.write(bi, formatName, new File(compFileName));
		} catch (Exception e) {
			throw new Exception("Analyzer Exception : drowLogPowerSpectrogram ," + e);
		}
	}

	public BufferedImage getLogSpectrogram(String filename) throws Exception {
		try {
			this.spcType = SpectrumType.log;
			MatrixTools tool = new MatrixTools();
			Preprocessor pp = new Preprocessor();
			ComplexDoubleMatrix V = pp.preprocessing(filename, false);

			DoubleMatrix V_abs = tool.abs(V);
			//System.out.println("MIN Value = "+ V_abs.min());
			//V_abs = tool.spectrumDenoising(V_abs, SystemSettings.denoingThreeshold);

			double[][] arr = tool.log(V_abs).toArray2();

			double durationInSeconds = (pp.getWavfile().getNumFrames() + 0.0) / pp.getWavfile().getSampleRate();
			// System.out.println("duration = "+durationInSeconds);
			this.setNtimebins_for_1sec((int) Math.floor((double) arr[0].length / durationInSeconds));
			this.setNfrequency(arr.length);
			this.setNtime(arr[0].length);

			return getSpectrogram(arr);
		} catch (Exception e) {
			throw new Exception("Analyzer Exception .getSpectrogram : " + e);
		}

	}

	public BufferedImage getSpectrogram(String filename) throws Exception {
		try {
			this.spcType = SpectrumType.normal;
			MatrixTools tool = new MatrixTools();
			Preprocessor pp = new Preprocessor();
			ComplexDoubleMatrix V = pp.preprocessing(filename, false);

			DoubleMatrix V_abs = tool.abs(V);
			//System.out.println("MIN Value = "+ V_abs.min());
			//V_abs = tool.spectrumDenoising(V_abs, SystemSettings.denoingThreeshold);
			double[][] arr = V_abs.toArray2();

			double durationInSeconds = (pp.getWavfile().getNumFrames() + 0.0) / pp.getWavfile().getSampleRate();
			// System.out.println("duration = "+durationInSeconds);

			this.setNtimebins_for_1sec((int) Math.floor((double) arr[0].length / durationInSeconds));
			this.setNfrequency(arr.length);
			this.setNtime(arr[0].length);
			return getSpectrogram(arr);
		} catch (Exception e) {
			throw new Exception("Analyzer Exception .getSpectrogram : " + e);
		}

	}

	public BufferedImage getSpectrogram(double[][] arr) {

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
		this.bufferdImg = spectrogram;
		return spectrogram;
	}
}
