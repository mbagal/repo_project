package com.java.practice;

import java.util.*;

class P1 {
	public void convert() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter a sentence");
		String s = sc.nextLine();
		s = s + " ";
		char ch;
		String w;
		int l = s.length();
		for (int i = 0; i < l; i++) {
			w = "";
			while (s.charAt(i) != ' ') {
				w = w + s.charAt(i);
				i++;
			}
			ch = w.charAt(0);
			ch-= 32;
			w = ch + w.substring(1);
			System.out.print(w + " ");

		}

	}
	
	public static void main(String[] args) {
		P1 p1 = new P1();
		p1.convert();
	}
}