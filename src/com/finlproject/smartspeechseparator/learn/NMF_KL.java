package com.finlproject.smartspeechseparator.learn;

import java.io.File;

import org.jblas.DoubleMatrix;

import com.finalproject.smartspeechseparator.debug.DebugLogger;
import com.finalproject.smartspeechseparator.general.Cell;
import com.finalproject.smartspeechseparator.transform.MatrixTools;

/**
 * %%
*	%    NMF by multiplicative update using Kullback-Leibler divergence cost 
*	%   
*	%%%%%INPUT
*	%       V: F x T mixed spectrogram contain positive values 
*	%       B_init: F x J basis vectors 
*	%       W_init: J x T weights vectors
*	%       niter: the number of iterations of the algorithm
*	%       comp_ind: component distripution indecis 
*	%       switch_B: bolean element to learn B or not
*	%       switch_W: bolean element to learn W or not
*	%       Mu: the regularization parameter can tack values like
*	%       0.0001,0.0003,0.001,...,0.1,0.3
*	%
*	%%%%OUTPUT
*	%       B: out basis vectors
*	%       W: out wieghtes vectors
*	%       COST: vector of size (niter x 1) contain the cost of each iteration
*	%
*	%
*	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
*	%%		MOHAMMAD SOLAIMAN AL-ASHKAR 9-4-2016
***/
public class NMF_KL implements Runnable{
	

	//INPUT PARAMETERS
	private DoubleMatrix V,B_init,W_init;
	private int niter = 500;
	private Cell comp_ind;
	boolean switch_B=true,switch_W=true;
	private double Mu = 0.001;
	STATE state;
	int numOfSupervisedComponents = 1;
	
	//OUTPUT PARAMETERS
	DoubleMatrix B, W;
	double[] COST;
	
	private MatrixTools tool;
	private DebugLogger logger;
	public NMF_KL(DoubleMatrix v, DoubleMatrix b_init, DoubleMatrix w_init, int niter, Cell comp_ind, boolean switch_B,
			boolean switch_W, double mU) {
		V = v;
		B_init = b_init;
		W_init = w_init;
		this.niter = niter;
		this.comp_ind = comp_ind;
		this.switch_B = switch_B;
		this.switch_W = switch_W;
		Mu = mU;
		
		tool = new MatrixTools();
		logger = new DebugLogger(new File("NMF_log.txt"));
	}


	public NMF_KL(DoubleMatrix v, DoubleMatrix b_init, DoubleMatrix w_init, int niter, Cell comp_ind, boolean switch_B,
			boolean switch_W, double mu, STATE state, int numOfSupervisedComponents, DoubleMatrix b, DoubleMatrix w,
			double[] cOST, MatrixTools tool, DebugLogger logger) {
		super();
		V = v;
		B_init = b_init;
		W_init = w_init;
		this.niter = niter;
		this.comp_ind = comp_ind;
		this.switch_B = switch_B;
		this.switch_W = switch_W;
		Mu = mu;
		this.state = state;
		this.numOfSupervisedComponents = numOfSupervisedComponents;
		B = b;
		W = w;
		COST = cOST;
		this.tool = tool;
		this.logger = logger;
	}

	public NMF_KL(DoubleMatrix v, DoubleMatrix b_init, DoubleMatrix w_init, int niter, Cell comp_ind, boolean switch_B,
			boolean switch_W, double mU, STATE state) {
		V = v;
		B_init = b_init;
		W_init = w_init;
		this.niter = niter;
		this.comp_ind = comp_ind;
		this.switch_B = switch_B;
		this.switch_W = switch_W;
		Mu = mU;
		this.state = state;
		tool = new MatrixTools();
		logger = new DebugLogger(new File("NMF_log.txt"));
	}

	public STATE getState() {
		return state;
	}


	public void setState(STATE state) {
		this.state = state;
	}


	public double getMu() {
		return Mu;
	}


	public void setMu(double mu) {
		Mu = mu;
	}


	public NMF_KL(DoubleMatrix v, DoubleMatrix b_init, DoubleMatrix w_init, Cell comp_ind) {
		V = v;
		B_init = b_init;
		W_init = w_init;
		this.comp_ind = comp_ind;
		
		tool = new MatrixTools();
		logger = new DebugLogger(new File("NMF_log.txt"));
	}
	
	public int getNumOfSupervisedComponents() {
		return numOfSupervisedComponents;
	}


	public void setNumOfSupervisedComponents(int numOfSupervisedComponents) {
		this.numOfSupervisedComponents = numOfSupervisedComponents;
	}


	public DebugLogger getDebugTool() {
		return logger;
	}


	public void setDebugTool(DebugLogger debugTool) {
		this.logger = debugTool;
	}

	

	public DoubleMatrix getB() {
		return B;
	}


	public DoubleMatrix getW() {
		return W;
	}


	public double[] getCOST() {
		return COST;
	}

	
	public int getNiter() {
		return niter;
	}


	public void setNiter(int niter) {
		this.niter = niter;
	}


	public void RunSupervised_NMF(){
		run_NMF(STATE.supervised);
	}

	public void RunSimiSupervised_NMF(){
		run_NMF(STATE.simisupervised);
	}
	
	public void RunUnupervised_NMF(){
		run_NMF(STATE.unsupervised);
	}
	
	private void run_NMF(STATE state){
		int F = V.getRows(), T = V.getColumns(), num_src = comp_ind.size();
		DoubleMatrix V_approx = DoubleMatrix.zeros(F, T);
		COST = new double[niter];
		
		W = W_init; B = B_init;
		//calculate V_approx
		for(int j = 0; j<num_src;j++){
			DoubleMatrix S_j = B.getColumns(comp_ind.get(j)).mmul(W.getRows(comp_ind.get(j)));
			V_approx = V_approx.add(S_j);
		}
		
		COST[0] = costfunction(V, V_approx, Mu, W);
		
		int j_start =0;
		switch( state){
			case supervised:{
				j_start = num_src;
				break;
			}
			case simisupervised:{
				j_start = numOfSupervisedComponents;
				break;
			}
			default:
				j_start = 0;
				break;
		}
		//Run NMF core
		for(int itr = 1; itr<niter;itr++){
			
			//UPDATE B
			if(switch_B){
				for(int j=j_start; j<num_src;j++){
					int Kj = comp_ind.get(j).length;
					
					//Numerator and Denominator of update rule
					DoubleMatrix B_num = DoubleMatrix.zeros(Kj, T), 
							B_Den = DoubleMatrix.zeros(Kj, T);
					
					B_num =   V.div(V_approx).mmul(W.getRows(comp_ind.get(j)).transpose()).add( ((DoubleMatrix.ones(F,T).mmul(W.getRows(comp_ind.get(j)).transpose())).mul(B.getColumns(comp_ind.get(j)))).mul(B.getColumns(comp_ind.get(j))) );
					
					B_Den =( DoubleMatrix.ones(F,T).mmul(W.getRows(comp_ind.get(j)).transpose())).add( ((V.div(V_approx)).mmul(W.getRows(comp_ind.get(j)).transpose()).mul(B.getColumns(comp_ind.get(j)))).mul(B.getColumns(comp_ind.get(j))) );

					DoubleMatrix B_old = B.getColumns(comp_ind.get(j));
					
					//In matlab --|>
					// B(:,comp_ind{j}) = B(:,comp_ind{j}) .* (B_num ./ B_Den);
					DoubleMatrix replacement = B.getColumns(comp_ind.get(j) ).mul(B_num.div(B_Den));
					int[] inds = comp_ind.get(j);
					int ctr=0;
					for(int in : inds){
						B.putColumn(in,  replacement.getColumn(ctr++));
					}
					
					V_approx = V_approx.add(  ( B.getColumns(comp_ind.get(j)).sub(B_old) ).mmul(W.getRows(comp_ind.get(j)))    );
				}
			}
			
			//UPDATE W
			if(switch_W){
				for(int j=0; j<num_src;j++){
					
					int Kj = comp_ind.get(j).length;
					
					//Numerator and Denominator of update rule
					DoubleMatrix W_num = DoubleMatrix.zeros(Kj, T), 
							W_Den = DoubleMatrix.zeros(Kj, T);
					
					W_num = (B.getColumns(comp_ind.get(j)).transpose() ).mmul(V.div(V_approx));
					W_Den = ((B.getColumns(comp_ind.get(j)).transpose()).mmul(DoubleMatrix.ones(F, T))).add(Mu);
					
					DoubleMatrix W_old = W.getRows(comp_ind.get(j));
					
					
					//In Matlab --|>
					//W(comp_ind{j},:) = W(comp_ind{j},:) .* (W_num ./ W_Den);
					DoubleMatrix replacement = W.getRows(comp_ind.get(j)).mul(W_num.div(W_Den));
					int[] inds = comp_ind.get(j);
					int ctr=0;
					for(int in : inds){
						W.putRow(in, replacement.getRow(ctr++));
					}
					
					V_approx = V_approx.add(   B.getColumns(comp_ind.get(j)).mmul( W.getRows(comp_ind.get(j)).sub(W_old) )   );
				}
			}
			
			COST[itr] = costfunction(V, V_approx, Mu, W);
			
			//    %%Normalization%%
			DoubleMatrix scale = B.columnSums();
			B = B.div(scale.repmat(F, 1));
			W = W.mul(scale.transpose().repmat(1, T));
			
			if(Double.isNaN(COST[itr])){
				B = null;
				W = null;
				COST = null;
				return;
			}
			
			logger.log("MU update: iteration  "+(itr + 1)+"  of  "+niter+",  cost = "+COST[itr]);
		}
	}
	
	private double costfunction(DoubleMatrix V, DoubleMatrix V_approx, double Mu, DoubleMatrix W){
	
		//System.out.println( V_approx.get(10));
		return V.mul(	tool.log(V.div(V_approx))).sub(V).add(V_approx).sum() + (Mu * W.sum());
	}


	@Override
	public void run() {
		this.run_NMF(this.state);
	}
}

