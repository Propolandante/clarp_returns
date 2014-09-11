package com.example.clarp_returns;

public class NoteItem {
    String name;
    Boolean isChecked;
    String notes;
    int type;

    public NoteItem(String name, int type){
        this.name = name;
        this.type = type;
        notes = "";
        isChecked = false;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public boolean getChecked() {
        return this.isChecked;
    }
}
