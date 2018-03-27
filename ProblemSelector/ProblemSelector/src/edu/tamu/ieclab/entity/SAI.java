package edu.tamu.ieclab.entity;

public class SAI {
	private String selection;
	private String action;
	private String input;
	public SAI(String selection, String action, String input) {
		super();
		this.selection = selection;
		this.action = action;
		this.input = input;
	}
	public String getSelection() {
		return selection;
	}
	public void setSelection(String selection) {
		this.selection = selection;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getInput() {
		return input;
	}
	public void setInput(String input) {
		this.input = input;
	}
	
}
