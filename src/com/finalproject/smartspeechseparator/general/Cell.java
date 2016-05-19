package com.finalproject.smartspeechseparator.general;

import java.util.ArrayList;

public class Cell extends ArrayList<int[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<int[]> indexAddresses;

	public ArrayList<int[]> getIndexAddresses() {
		return indexAddresses;
	}

	public void setIndexAddresses(ArrayList<int[]> indexAddresses) {
		this.indexAddresses = indexAddresses;
	}

	public Cell() {
		this.indexAddresses = new ArrayList<int[]>();
	}

	public Cell(int[][] cell) {
		this.indexAddresses = new ArrayList<int[]>();
		for (int[] row : cell)
			this.indexAddresses.add(row);
	}

	public void addToCell(int[] row) {
		this.indexAddresses.add(row);
	}

	public  int length() {
		return this.indexAddresses.size();
	}
	public static String print(Cell c) {
		StringBuilder sb = new StringBuilder("{");
		for (int[] row : c) {
			sb.append(" [");
			for (int val : row)
				sb.append(val + ", ");

			sb.append("] ");
		}
		sb.append("}");
		
		System.out.println(sb.toString());
		return sb.toString();
	}

//	public static void main(String[] args) {
//		Cell cell = new Cell();
//
//		cell.add(new int[] { 0, 2, 4, 6, 8 });
//		cell.add(new int[] { 1, 3, 5, 7, 9 });
//
//		System.out.println(cell.get(1)[0]);
//		Cell.print(cell);
//	}
	
}
