package com.finlproject.smartspeechseparator.user;

import java.util.ArrayList;
import java.util.List;

public class TEST {

	public static void main(String[] args) {

		SeparatedOutoutStructure ss = new SeparatedOutoutStructure();
		String s = "jfj";
		ss.setMixedPath(s);
		System.out.println(ss.getMixedPath());
		s = "fasdfxcsadfdf";
		System.out.println(ss.getMixedPath());
		
		List<String> ll = new ArrayList<>();
		ll.add("mmm");
		ll.add("aaa");
		ss.setLi(ll);
		System.out.println(ss.getLi().get(0));
		
		ll.remove(0);

		System.out.println(ss.getLi().get(0));
		
	}

}
