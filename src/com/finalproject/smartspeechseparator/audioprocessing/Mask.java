package com.finalproject.smartspeechseparator.audioprocessing;

import java.util.ArrayList;

import org.jblas.ComplexDouble;
import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;

import com.finalproject.smartspeechseparator.general.*;
import com.finalproject.smartspeechseparator.transform.MatrixTools;

public class Mask {

	public DoubleMatrix getFilterMatrix(DoubleMatrix S, DoubleMatrix N, double p) {
		MatrixTools tool = new MatrixTools();
		DoubleMatrix H = tool.pow(S, p).div(tool.pow(S, p).add(tool.pow(N, p)));
		
		return H; //tool.round(H);
	}

	public Separate_output_struct Separate(DoubleMatrix B, DoubleMatrix W, Cell comp_ind, ComplexDoubleMatrix X) {
		double sigma = SystemSettings.SEPARATION_PARAM_SIGMA, P = SystemSettings.P;
		int J = comp_ind.size();

		DoubleMatrix[] sources = new DoubleMatrix[J];
		for (int j = 0; j < J; j++) {
			sources[j] = B.getColumns(comp_ind.get(j)).mmul(W.getRows(comp_ind.get(j)));
		}

		DoubleMatrix H_ = this.getFilterMatrix(sources[0], sources[1], P);
		ComplexDoubleMatrix H = H_.toComplex();
		ComplexDoubleMatrix S = H.mul(new ComplexDouble(sigma)).mul(X),
				N = ComplexDoubleMatrix.ones(H.getRows(), H.getColumns()).mul(new ComplexDouble(sigma)).sub(H).mul(X);
		;
		return new Separate_output_struct(S, N, H);
	}
	
	public Separate_output_struct Separate(DoubleMatrix B, DoubleMatrix W, Cell comp_ind, ComplexDoubleMatrix X, FILTERNAME filter, double sigma) {
		double P = SystemSettings.P;
		int J = comp_ind.size();

		if(filter == FILTERNAME.linear)
			P = 1;
		else
			P = 2;
		
		DoubleMatrix[] sources = new DoubleMatrix[J];
		for (int j = 0; j < J; j++) {
			sources[j] = B.getColumns(comp_ind.get(j)).mmul(W.getRows(comp_ind.get(j)));
		}

		DoubleMatrix H_ = this.getFilterMatrix(sources[0], sources[1], P);
		if(filter == FILTERNAME.hard){
			MatrixTools tool = new MatrixTools();
			H_= tool.round(H_);
			
		}
		
		ComplexDoubleMatrix H = H_.toComplex();
		ComplexDoubleMatrix S = H.mul(new ComplexDouble(sigma)).mul(X),
				N = ComplexDoubleMatrix.ones(H.getRows(), H.getColumns()).mul(new ComplexDouble(sigma)).sub(H).mul(X);
		;
		return new Separate_output_struct(S, N, H);
	}

}
