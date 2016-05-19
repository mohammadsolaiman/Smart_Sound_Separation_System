package com.finlproject.smartspeechseparator.learn;

import org.jblas.DoubleMatrix;

import com.finalproject.smartspeechseparator.transform.MatrixTools;

public class Initializer {
	private DoubleMatrix B_init, W_init;

	public DoubleMatrix getB_init() {
		return B_init;
	}

	public DoubleMatrix getW_init() {
		return W_init;
	}

	/**
	 * return F x K none negative matrix depending on the mean values of Y
	 **/
	public DoubleMatrix initialize_B(DoubleMatrix Y, int F, int K) {
		MatrixTools mt = new MatrixTools();

		DoubleMatrix Mu = Y.mul(Y).rowMeans();

		B_init = mt.abs(DoubleMatrix.rand(F, K)).mul(0.5)
				.add(DoubleMatrix.ones(F, K).mul(Mu.mmul(DoubleMatrix.ones(1, K))));

		
		return B_init;
	}

	/**
	 * return K x T none negative matrix
	 **/
	public DoubleMatrix initialize_W(int K, int T){
		MatrixTools mt = new MatrixTools();

		W_init =  mt.abs(DoubleMatrix.rand( K,T)).mul(0.5).add(DoubleMatrix.ones( K,T));
		return W_init;
	}
	
//TEST/////////////////////////////////////////////////////////////////////
//	public static void main(String[] args) {
//		DoubleMatrix Y = new DoubleMatrix(new double[][] { { 0.1980, 0.0662, 0.2121 }, { 1.9153, 1.0516, 1.3284 },
//				{ 1.5423, 1.4583, 1.3460 }, { 0.3256, 0.6842, 0.1337 }, { 0.2778, 0.3516, 0.7530 } });
//
//		Initializer init = new Initializer();
//		init.initialize_B(Y, 5, 2).print();
//		init.initialize_W(2, 3).print();
//	}
}
