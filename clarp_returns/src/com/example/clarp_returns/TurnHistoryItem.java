package com.example.clarp_returns;

public class TurnHistoryItem {
	public String playerName;
	public int type;
	public int person;
	public int weapon;
	public int location;

	public String player2Name;
	
	public TurnHistoryItem(int type){
		this.type = type;
	}
}
