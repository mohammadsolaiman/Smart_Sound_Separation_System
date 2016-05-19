package com.finlproject.smartspeechseparator.user;

import java.util.List;

import com.finalproject.smartspeechseparator.transform.Analyzer;

public class SeparatedOutoutStructure {

	private String mixedPath, speechPath, noisePath, spectro_mixPath, spectro_speechPath, spectro_noisePath;

	private Analyzer speechAnalyzer, noiseAnalizer, mixAnalyzer;

	List<String> li ;
	
	public List<String> getLi() {
		return li;
	}

	public void setLi(List<String> li) {
		this.li = li;
	}

	public Analyzer getSpeechAnalyzer() {
		return speechAnalyzer;
	}

	public void setSpeechAnalyzer(Analyzer speechAnalyzer) {
		this.speechAnalyzer = speechAnalyzer;
	}

	public Analyzer getNoiseAnalizer() {
		return noiseAnalizer;
	}

	public void setNoiseAnalizer(Analyzer noiseAnalizer) {
		this.noiseAnalizer = noiseAnalizer;
	}

	public Analyzer getMixAnalyzer() {
		return mixAnalyzer;
	}

	public void setMixAnalyzer(Analyzer mixAnalyzer) {
		this.mixAnalyzer = mixAnalyzer;
	}

	public String getMixedPath() {
		return mixedPath;
	}

	public void setMixedPath(String mixedPath) {
		this.mixedPath = mixedPath;
	}

	public String getSpeechPath() {
		return speechPath;
	}

	public void setSpeechPath(String speechPath) {
		this.speechPath = speechPath;
	}

	public String getNoisePath() {
		return noisePath;
	}

	public void setNoisePath(String noisePath) {
		this.noisePath = noisePath;
	}

	public String getSpectro_mixPath() {
		return spectro_mixPath;
	}

	public void setSpectro_mixPath(String spectro_mixPath) {
		this.spectro_mixPath = spectro_mixPath;
	}

	public String getSpectro_speechPath() {
		return spectro_speechPath;
	}

	public void setSpectro_speechPath(String spectro_speechPath) {
		this.spectro_speechPath = spectro_speechPath;
	}

	public String getSpectro_noisePath() {
		return spectro_noisePath;
	}

	public void setSpectro_noisePath(String spectro_noisePath) {
		this.spectro_noisePath = spectro_noisePath;
	}

}
