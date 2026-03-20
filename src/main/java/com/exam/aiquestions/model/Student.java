package com.exam.aiquestions.model;

public class Student {
	
	{
		System.out.println("Anonymous Block");
	}
	
	static {
		System.out.println("Static Block");
	}
	public  Student() {
	     
		System.out.println("Welcome to Student Constructor");

	}
	
		public static void Student() {
     
			System.out.println("Welcome to Student Method");

		}

	public void sitdown() {
		System.out.println("Calling Sit down method");
	}

	
	
	public void study1() {
		
		System.out.println("Calling Study1 method");
	}
	private static void study() {
		// TODO Auto-generated method stub
		System.out.println("Calling Study method");
	}
	
	
	
	public static void main(String[] args) {
		Student anil=new Student();
		/*anil.sitdown();
		anil.Student();		
		anil.study1();*/
		//Student.study();
		
	}



	
	
}


