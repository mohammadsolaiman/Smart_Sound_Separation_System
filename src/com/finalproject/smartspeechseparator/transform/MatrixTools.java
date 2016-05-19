package com.finalproject.smartspeechseparator.transform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;

public class MatrixTools {
	//
	// public static void main(String[] args){
	// MatrixTools mt = new MatrixTools();
	//
	// try{
	// DoubleMatrix dm =mt.File2DoubleMatrix("testM2F.txt");
	// dm.print();
	// }catch(IOException e){}
	// }
	//
	public DoubleMatrix abs(ComplexDoubleMatrix cdm) {
		int ro = cdm.getRows(), co = cdm.getColumns();
		DoubleMatrix out = new DoubleMatrix(ro, co);

		for (int i = 0; i < cdm.length; i++) {
			out.put(i, cdm.get(i).abs());
		}

		return out;
	}

	public DoubleMatrix round(DoubleMatrix x) {
		int cols, rows;
		cols = x.getColumns();
		rows = x.getRows();
		DoubleMatrix out = new DoubleMatrix(rows, cols);

		for (int i = 0; i < rows * cols; i++) {
			out.put(i, Math.round(x.get(i)));
		}

		return out;
	}

	public DoubleMatrix pow(DoubleMatrix x, double p) {
		int cols, rows;
		cols = x.getColumns();
		rows = x.getRows();
		DoubleMatrix out = new DoubleMatrix(rows, cols);

		for (int i = 0; i < rows * cols; i++) {
			out.put(i, Math.pow(x.get(i), p));
		}

		return out;
	}

	public DoubleMatrix log(DoubleMatrix x) {
		int cols, rows;
		cols = x.getColumns();
		rows = x.getRows();
		DoubleMatrix out = new DoubleMatrix(rows, cols);

		for (int i = 0; i < rows * cols; i++) {
			out.put(i, Math.log(x.get(i)));
		}

		return out;
	}

	public DoubleMatrix sqrt(DoubleMatrix x) {
		int cols, rows;
		cols = x.getColumns();
		rows = x.getRows();
		DoubleMatrix out = new DoubleMatrix(rows, cols);

		for (int i = 0; i < rows * cols; i++) {
			out.put(i, Math.sqrt(x.get(i)));
		}

		return out;
	}

	public DoubleMatrix abs(DoubleMatrix x) {
		int cols, rows;
		cols = x.getColumns();
		rows = x.getRows();
		DoubleMatrix out = new DoubleMatrix(rows, cols);

		for (int i = 0; i < rows * cols; i++) {
			out.put(i, Math.abs(x.get(i)));
		}

		return out;
	}

	public double[][] makeVector(double from, double step, double to) {
		// make 1D vector first value is 'from' increesing with 'step' and the
		// last value is 'to'
		// this method like MATLAB command x = [0.5:2:100.5]

		int length = (int) Math.floor(Math.abs(to - from) / step) + 1;
		double[][] out = new double[1][length];
		int ctr = 0;
		if (step > 0) {
			for (double i = from; i <= to; i += step) {
				out[0][ctr] = i;
				ctr++;
			}
		} else {
			for (double i = from; i >= to; i += step) {
				out[0][ctr] = i;
				ctr++;
			}
		}

		return out;
	}

	public int[] makeIndecesRange(int from, int to, int step) {
		// make 1D vector first value is 'from' increesing with 'step' and the
		// last value is 'to'
		// this method like MATLAB command x = [0.5:2:100.5]

		int length = (int) Math.floor((double) Math.abs(to - from) / Math.abs(step)) + 1;
		int[] out = new int[length];
		int ctr = 0;
		if (step > 0) {
			for (int i = from; i <= to; i += step) {
				out[ctr] = i;
				ctr++;
			}
		} else {
			for (int i = from; i >= to; i += step) {
				out[ctr] = i;
				ctr++;
			}
		}

		return out;
	}

	public int[] makeIndecesRange(int from, int to) {
		// make 1D vector first value is 'from' increesing with 'step' and the
		// last value is 'to'
		// this method like MATLAB command x = [0.5:100.5]

		int length = to - from;
		int[] out = new int[length + 1];
		int ctr = 0;
		for (int i = from; i <= to; i++) {
			out[ctr] = i;
			ctr++;
		}

		return out;
	}

	public double[][] appendColumnsRight(double[][] x, double[][] app) {

		int x_rows = x.length, x_cols = x[0].length, app_rows = app.length, app_cols = app[0].length;
		if (x_rows != app_rows) {
			System.err.println("Error: MATRIX Dimention!\nMETHOD: appendColumnsRight");
			return null;
		}
		double[][] out = new double[x_rows][x_cols + app_cols];
		for (int i = 0; i < out.length; i++) {
			for (int j = 0; j < out[0].length; j++) {
				if (j < x_cols) {
					out[i][j] = x[i][j];
				} else {
					out[i][j] = app[i][j - x_cols];
				}
			}
		}

		return out;
	}

	public double[][] appendColumnsLeft(double[][] x, double[][] app) {

		int x_rows = x.length, x_cols = x[0].length, app_rows = app.length, app_cols = app[0].length;
		if (x_rows != app_rows) {
			System.err.println("Error: MATRIX Dimention!\nMETHOD: appendColumnsRight");
			return null;
		}
		double[][] out = new double[x_rows][x_cols + app_cols];
		for (int i = 0; i < out.length; i++) {
			for (int j = 0; j < out[0].length; j++) {
				if (j < app_cols) {
					out[i][j] = app[i][j];
				} else {
					out[i][j] = x[i][j - app_cols];
				}
			}
		}

		return out;
	}

	public double[][] appendColumns(double[][] left, double[][] x, double[][] right) {
		double[][] out = appendColumnsLeft(x, left);
		return appendColumnsRight(out, right);

	}

	public double[][] zeros(int rows, int columns) {
		double[][] out = new double[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				out[i][j] = 0;
			}
		}

		return out;
	}

	public double[][] ones(int rows, int columns) {
		double[][] out = new double[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				out[i][j] = 1;
			}
		}

		return out;
	}

	public double[][] select(double[][] x, int rfrom, int rto, int cfrom, int cto) {

		double[][] out = new double[rto - rfrom][cto - cfrom];

		for (int i = rfrom; i < rto; i++) {
			for (int j = cfrom; j < cto; j++) {
				out[i - rfrom][j - cfrom] = x[i][j];
			}
		}

		return out;
	}

	public double[][] dotPow(double[][] x, double pow) {
		double[][] out = new double[x.length][x[0].length];
		for (int i = 0; i < out.length; i++) {
			for (int j = 0; j < out[0].length; j++) {
				out[i][j] = Math.pow(x[i][j], pow);
			}
		}

		return out;
	}

	public void replace(double[][] x, int rfrom, int rto, int cfrom, int cto, double[][] newElements) {
		double[][] out = new double[x.length][x[0].length];
		for (int i = rfrom; i < rto; i++) {
			for (int j = cfrom; j < cto; j++) {
				x[i][j] = newElements[i - rfrom][j - cfrom];
			}
		}
	}

	public double[][] plus(double[][] x, double a) {
		double[][] out = new double[x.length][x[0].length];
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < x[0].length; j++) {
				out[i][j] = x[i][j] + a;
			}
		}
		return out;
	}

	public double[][] plus(double[][] x, double[][] a) {
		double[][] out = new double[x.length][x[0].length];
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < x[0].length; j++) {
				out[i][j] = x[i][j] + a[i][j];
			}
		}
		return out;
	}

	public double[][] minus(double[][] x, double a) {
		double[][] out = new double[x.length][x[0].length];
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < x[0].length; j++) {
				out[i][j] = x[i][j] - a;
			}
		}
		return out;
	}

	public DoubleMatrix File2DoubleMatrix(String filePath) throws IOException {

		File in = new File(filePath);
		Scanner sc = new Scanner(in);

		String line;
		Scanner lineSC;
		List<List<Double>> dMat_list = new ArrayList<List<Double>>();
		while (sc.hasNextLine()) {
			line = sc.nextLine();
			lineSC = new Scanner(line);
			List<Double> rowList = new ArrayList<Double>();
			while (lineSC.hasNext()) {
				rowList.add(Double.parseDouble(lineSC.next()));
			}
			dMat_list.add(rowList);

		}

		int rows = dMat_list.size(), cols = rows == 0 ? 0 : dMat_list.get(0).size();
		DoubleMatrix out = new DoubleMatrix(rows, cols);
		for (int i = 0; i < rows; i++) {
			List<Double> li = dMat_list.get(i);
			for (int j = 0; j < cols; j++) {
				out.put(i, j, li.get(j));
			}
		}

		sc.close();
		return out;

	}

	public StringBuilder DoubleMatrex2File(DoubleMatrix B, String path) throws IOException {

		FileWriter writer = new FileWriter(new File(path));
		StringBuilder SB = DoubleMatrex2StringBuilder(B);

		writer.write(SB.toString());
		writer.close();

		return SB;
	}

	public DoubleMatrix String2DoubleMatrix(String matrixTxt){
		
		Scanner sc = new Scanner(matrixTxt);

		String line;
		Scanner lineSC;
		List<List<Double>> dMat_list = new ArrayList<List<Double>>();
		while (sc.hasNextLine()) {
			line = sc.nextLine();
			lineSC = new Scanner(line);
			List<Double> rowList = new ArrayList<Double>();
			while (lineSC.hasNext()) {
				String nxt = lineSC.next();
				//System.out.println("NXT ======================= "+nxt);
				rowList.add(Double.parseDouble(nxt));
			}
			dMat_list.add(rowList);

		}

		int rows = dMat_list.size(), cols = rows == 0 ? 0 : dMat_list.get(0).size();
		DoubleMatrix out = new DoubleMatrix(rows, cols);
		for (int i = 0; i < rows; i++) {
			List<Double> li = dMat_list.get(i);
			for (int j = 0; j < cols; j++) {
				out.put(i, j, li.get(j));
			}
		}

		sc.close();
		return out; 
	}
	
	public StringBuilder DoubleMatrex2StringBuilder(DoubleMatrix B) {

		StringBuilder SB = new StringBuilder();
		for (int i = 0; i < B.getRows(); i++) {
			for (int j = 0; j < B.getColumns(); j++) {
				SB.append(B.get(i, j) + "\t");
			}

			SB.append("\n");
		}

		return SB;
	}
	
	public DoubleMatrix spectrumDenoising(DoubleMatrix V_abs, double threeshold){
		DoubleMatrix out = new DoubleMatrix(V_abs.getRows(), V_abs.getColumns());
		for(int i = 0;i<V_abs.length;i++){
			if(Math.abs(V_abs.get(i)) < threeshold){
				out.put(i, 0);
			}else{
				out.put(i, V_abs.get(i));
			}
		}
		return out;
	}
}
