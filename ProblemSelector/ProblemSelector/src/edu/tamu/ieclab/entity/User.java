package edu.tamu.ieclab.entity;

public class User {
	private String firstName;
	private int rollNumber;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public int getRollNumber() {
		return rollNumber;
	}

	public void setRollNumber(int rollNumber) {
		this.rollNumber = rollNumber;
	}
	
	@Override
	public String toString(){
		return firstName+"  "+rollNumber;
	}
}
