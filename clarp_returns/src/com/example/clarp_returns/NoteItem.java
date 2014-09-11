package com.example.clarp_returns;

public class NoteItem {
	String name;
	boolean isChecked;
	String notes;
	int type;
	
	public NoteItem(String name, int type){
		this.name = name;
		this.type = type;
		notes = "";
		isChecked = false;
	}
}
