package com.finalproject.smartspeechseparator.general;

import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;

public class Separate_output_struct {

	public ComplexDoubleMatrix S, N;
	public ComplexDoubleMatrix H;
	public Separate_output_struct(ComplexDoubleMatrix s, ComplexDoubleMatrix n, ComplexDoubleMatrix h) {
		S = s;
		N = n;
		H = h;
	}
	public Separate_output_struct(ComplexDoubleMatrix s, ComplexDoubleMatrix n) {
		S = s;
		N = n;
	}
	public Separate_output_struct() {
	}
	
	
}
