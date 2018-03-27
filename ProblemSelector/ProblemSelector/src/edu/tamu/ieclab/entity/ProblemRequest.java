package edu.tamu.ieclab.entity;

public class ProblemRequest {
	private String criteria;
	private String folder;
	private String studentID;
	public String getCriteria() {
		return criteria;
	}
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}
	public String getFolder() {
		return folder;
	}
	public void setFolder(String folder) {
		this.folder = folder;
	}
	public String getStudentID() {
		return studentID;
	}
	public void setStudentID(String studentID) {
		this.studentID = studentID;
	}
	
	@Override
	public String toString(){
		return "student ID: "+studentID+"  criteria: "+criteria+" folder: "+folder;
	}
}
