package com.java.practice;

public class LambadaPractice {

	public static void main(String[] args) {
		
		LambadaPractice lp =new LambadaPractice();
		
		MathOperation addition = (a, b) -> a + b;
		MathOperation sunstraction = (a, b) -> a - b;
		MathOperation multiplication = (a, b) -> { return a * b; };
		MathOperation divisition = (a, b) -> a / b;
		
		System.out.println("Addition : 5 + 2 = "+addition.opration(5, 2));
		
		Greetings g = message -> System.out.println("Hello "+message);
		g.greet("Manisha");
	}
		
	public interface MathOperation{
		int opration(int a, int b);
	} 
	
	public interface Greetings {
		void greet(String message);
	}
}
