package com.example.clarp_returns;

import android.util.Log;

public class TurnHistoryItem {
	
	public String turnTitle;
	
	public String playerName;
	public String result;
	public String resultPrivate;
	public int type;
	public String playerFbId;
	
	
	private ClarpCard suspect;
	private ClarpCard weapon;
	private ClarpCard location;
	
	public String alibiName;
	public String alibiCardName;
	public String alibiFbId;
	
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
	
	public TurnHistoryItem(int type, Player p, ClarpCard s, ClarpCard w, ClarpCard l, Player a, ClarpCard c, String r){
		this.type = type;
		
		if (this.type == GameActivity.TYPE_SUGGEST || this.type == GameActivity.TYPE_ACCUSE)
		{
			this.playerName = p.getFullName();
			this.playerFbId = p.getFbId();
			this.suspect = s;
			this.weapon = w;
			this.location = l;
			this.alibiName = null;
			this.alibiCardName = null;
			this.alibiFbId = null;
			
			if (this.type == GameActivity.TYPE_SUGGEST)
			{
				this.turnTitle = this.playerName + "'s Suggestion:";
			}
			else
			{
				this.turnTitle = this.playerName + "'s Accusation:";
			}
			
			
			if (a != null)
			{
				if (c != null)
				{
					this.alibiName = a.getFullName();
					this.alibiCardName = c.getCardName();
					this.alibiFbId = a.getFbId();
					
					this.result = this.alibiName + " proved " + this.playerName + " wrong";
					this.resultPrivate = this.alibiName + " ruled out " + this.alibiCardName;
				}
				else
				{
					Log.d(ClarpApplication.TAG, "(THI) Card is null but player is not? player: " + a.getFullName());
				}
			}
			else
			{
				this.result = "No one" + " proved " + this.playerName + " wrong";
				this.resultPrivate = this.result;
			}
		}
		else if (this.type == GameActivity.TYPE_ALERT)
		{
			if (r != null)
			{
				this.result = r;
			}
			else
			{
				Log.d(ClarpApplication.TAG, "Type is ALERT, result is null? (THI)");
			}
		}
		
		
		
		
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
	public String getPlayerFbId()
	{
		return this.playerFbId;
	}
	public String getAlibiFbId()
	{
		return this.alibiFbId;
	}
	
}
