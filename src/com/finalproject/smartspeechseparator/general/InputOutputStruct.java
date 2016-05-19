package com.finalproject.smartspeechseparator.general;

import java.util.List;

import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;

public class InputOutputStruct {

	public DoubleMatrix doubleMatrix_1, doubleMatrix_2, doubleMatrix_3;

	public ComplexDoubleMatrix complexDoubleMatrix_1, complexDoubleMatrix_2, complexDoubleMatrix_3;

	public int fs, stft_win_len, nsample;

	public double[] d1_arraydouble_1, d1_arraydouble_2, d1_arraydouble_3;

	public int[] d1_arrayint_1, d1_arrayint_2, d1_arrayint_3;

	public double[][] d2_arraydouble_1, d2_arraydouble_2, d2_arraydouble_3;

	public int[][] d2_arrayint_1, d2_arrayint_2, d2_arrayint_3;

	public List<String> string_list_1, string_list_2, string_list_3;
	
}