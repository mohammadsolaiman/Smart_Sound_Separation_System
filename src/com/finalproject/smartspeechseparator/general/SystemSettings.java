package com.finalproject.smartspeechseparator.general;

import java.io.File;

public class SystemSettings {
																			
	public static final int  sampleLemgth=200000, stft_win_len = 1024,/* how many columns each sound represent in Dictionary  */ SpeechSourceColInDec = 2, NoiseSourceColInDec = 2;
	public static final double SEPARATION_PARAM_SIGMA = 1, P = 2;//USED IN Mask.Separate function
	//public static final double denoingThreeshold = 0.00000001;
	
	public static final String currentDir = System.getProperty("user.dir");
	public static final String sound_speechDirPath =currentDir + File.separator+"data"+File.separator+"sounds"+File.separator+"speech"+File.separator;//data\sound\speech\
	public static final String sound_noiseDirPath =currentDir + File.separator+"data"+File.separator+"sounds"+File.separator+"noise"+File.separator;//data\sound\speech\
	public static final String sound_mixDirPath =currentDir + File.separator+"data"+File.separator+"sounds"+File.separator+"mix"+File.separator;//data\sound\speech\
	public static final String sound_sep_speechDirPath =currentDir + File.separator+"data"+File.separator+"sounds"+File.separator+"sep_speech"+File.separator;//data\sound\speech\
	public static final String sound_sep_noiseDirPath =currentDir + File.separator+"data"+File.separator+"sounds"+File.separator+"sep_noise"+File.separator;//data\sound\speech\
	public static final String sound_eqDirPath =currentDir + File.separator+"data"+File.separator+"sounds"+File.separator+"eq"+File.separator;//data\sound\speech\

	public static final String spectrum_speechDirPath =currentDir + File.separator+"data"+File.separator+"spectrorgrams"+File.separator+"speech"+File.separator;//data\sound\speech\
	public static final String spectrum_noiseDirPath =currentDir + File.separator+"data"+File.separator+"spectrorgrams"+File.separator+"noise"+File.separator;//data\sound\speech\
	public static final String spectrum_mixDirPath =currentDir + File.separator+"data"+File.separator+"spectrorgrams"+File.separator+"mix"+File.separator;//data\sound\speech\
	public static final String spectrum_sep_speechDirPath =currentDir + File.separator+"data"+File.separator+"spectrorgrams"+File.separator+"sep_speech"+File.separator;//data\sound\speech\
	public static final String spectrum_sep_noiseDirPath =currentDir + File.separator+"data"+File.separator+"spectrorgrams"+File.separator+"sep_noise"+File.separator;//data\sound\speech\
	public static final String spectrum_eqDirPath =currentDir + File.separator+"data"+File.separator+"spectrorgrams"+File.separator+"eq"+File.separator;//data\sound\speech\

	public static final String[] systemClusters = {sound_speechDirPath,sound_noiseDirPath,sound_mixDirPath,sound_sep_speechDirPath,sound_sep_noiseDirPath,sound_eqDirPath,
			spectrum_speechDirPath,spectrum_noiseDirPath,spectrum_mixDirPath,spectrum_sep_speechDirPath,spectrum_sep_noiseDirPath,spectrum_eqDirPath};
}
