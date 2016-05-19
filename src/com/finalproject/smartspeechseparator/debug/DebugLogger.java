package com.finalproject.smartspeechseparator.debug;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class DebugLogger {
	File logFile;
	FileWriter fw;

	@FXML
	private TextArea console;
	
	private StringBuilder logSB;
	
	public TextArea getConsole() {
		return console;
	}

	public void setConsole(TextArea console) {
		this.console = console;
	}

	public static StringBuilder logHistory = new StringBuilder();

	public DebugLogger() {
		this.logSB = new StringBuilder();
		this.logFile = new File("log.txt");
		try {
			fw = new FileWriter(logFile);
			fw.write("");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void clearHistory() {
		System.out.println("Clearing History...");
		try {
			fw = new FileWriter(logFile);
			fw.write("");
			fw.close();
			logHistory = new StringBuilder("");
			System.out.println("Clearing History DONE!...");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void commit(){
		try {
			fw = new FileWriter(logFile);
			fw.write(logSB.toString());;
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public DebugLogger(File logFile) {
		this.logFile = logFile;
		this.logSB = new StringBuilder();
		try {
			fw = new FileWriter(logFile);
			fw.write("");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void log(String s) {
		logHistory.append(s + "\n\n");
		logSB.append(s + "\n\n");
		System.out.println(s);
		if(console != null){
			console.setText(console.getText()+"\n"+s);
		}
	}

	public void log(double[] arr) {
		System.out.println("Writing...");
		try {
			fw = new FileWriter(logFile);

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < arr.length; i++) {
				sb.append(arr[i] + "\n");
			}
			logHistory.append(sb.toString() + "\n\n");
			fw.append(sb.toString() + "\n\n>>\n");
			fw.close();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void log(double[][] arr) {
		System.out.println("Writing...");

		try {
			fw = new FileWriter(logFile);

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < arr.length; i++) {
				for (int j = 0; j < arr[0].length; j++)
					sb.append(arr[i][j] + "\t");
				sb.append("\n");
			}
			logHistory.append(sb.toString() + "\n\n");
			fw.append(sb.toString() + "\n\n>>\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void log(int[] arr) {
		System.out.println("Writing...");

		try {
			String s = "";
			for (int i = 0; i < arr.length; i++) {
				s += arr[i] + "\n";
			}
			logHistory.append(s + "\n\n");
			fw.append(s + "\n\n>>\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void log(int[][] arr) {
		System.out.println("Writing...");

		try {
			String s = "";
			for (int i = 0; i < arr.length; i++) {
				for (int j = 0; j < arr[0].length; j++)
					s += arr[i][j] + "\t";
				s += "\n";
			}
			logHistory.append(s + "\n\n");
			fw.append(s + "\n\n>>\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
