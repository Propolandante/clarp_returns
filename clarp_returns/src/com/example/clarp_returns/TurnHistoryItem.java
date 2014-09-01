package com.example.clarp_returns;

public class TurnHistoryItem {
	public String playerName;
	public String result;
	public int type;
	
	private ClarpCard suspect;
	private ClarpCard weapon;
	private ClarpCard location;
	
	public String player2Name;
	
	public TurnHistoryItem(int type){
		this.type = type;
	}
	
	public TurnHistoryItem(int type, ClarpCard s, ClarpCard w, ClarpCard l){
		this.type = type;
		this.suspect = s;
		this.weapon = w;
		this.location = l;
		this.playerName = "Sir Joe";
	}
	
	public void setResult(String disprover) {
		
		this.result = disprover + " proved " + this.playerName + " wrong";
	}
	
	public ClarpCard getSuspect()
	{
		return this.suspect;
	}
	public ClarpCard getWeapon()
	{
		return this.weapon;
	}
	public ClarpCard getLocation()
	{
		return this.location;
	}
}
