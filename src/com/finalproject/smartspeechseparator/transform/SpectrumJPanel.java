package com.finalproject.smartspeechseparator.transform;

import javax.swing.JPanel;

public class SpectrumJPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Analyzer analyzer ;

	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}
}
