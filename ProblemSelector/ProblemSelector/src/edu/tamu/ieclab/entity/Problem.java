package edu.tamu.ieclab.entity;

import java.util.List;

public class Problem {
    private String problemName;
    private List<SAI> sai;
    private String[] skills;
    private int Lcount;
    private double Lvalue;
    private boolean solved;
    
	public Problem(String problemName, List<SAI> sai,String[]  skills, int lcount, double lvalue) {
		super();
		this.problemName = problemName;
		this.sai = sai;
		this.skills = skills;
		Lcount = lcount;
		Lvalue = lvalue;
	}
	public String getProblemName() {
		return problemName;
	}
	public void setProblemName(String problemName) {
		this.problemName = problemName;
	}
	public List<SAI> getSai() {
		return sai;
	}
	public void setSai(List<SAI> sai) {
		this.sai = sai;
	}
	public  String[]  getSkills() {
		return skills;
	}
	public void setSkills( String[]  skills) {
		this.skills = skills;
	}
	public int getLcount() {
		return Lcount;
	}
	public void setLcount(int lcount) {
		Lcount = lcount;
	}
	public double getLvalue() {
		return Lvalue;
	}
	public void setLvalue(double lvalue) {
		Lvalue = lvalue;
	}
	public boolean isSolved() {
		return solved;
	}
	public void setSolved(boolean solved) {
		this.solved = solved;
	}
	
    
}
