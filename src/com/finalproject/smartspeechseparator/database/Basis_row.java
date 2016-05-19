package com.finalproject.smartspeechseparator.database;

public class Basis_row {

	private int id, sound_id, K, stft_win_len;
	private StringBuilder data;
	
	public Basis_row(int id, int sound_id, int k, int stft_win_len, StringBuilder data) {
		this.id = id;
		this.sound_id = sound_id;
		K = k;
		this.stft_win_len = stft_win_len;
		this.data = data;
	}
	
	public Basis_row() {
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSound_id() {
		return sound_id;
	}
	public void setSound_id(int sound_id) {
		this.sound_id = sound_id;
	}
	public int getK() {
		return K;
	}
	public void setK(int k) {
		K = k;
	}
	public int getStft_win_len() {
		return stft_win_len;
	}
	public void setStft_win_len(int stft_win_len) {
		this.stft_win_len = stft_win_len;
	}
	public StringBuilder getData() {
		return data;
	}
	public void setData(StringBuilder data) {
		this.data = data;
	}



}
