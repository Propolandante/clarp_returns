package com.example.clarp_returns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.parse.*;

@ParseClassName("clarpGame")
public class clarpGame extends ParseObject {
	
	/*
	class player {
		
		int id;
		String name;
		String prefix;
		Boolean dq;
		int[] suspectFacts;
		int[] weaponFacts;
		int[] locationFacts;
		
	}
	*/
	
	class suspect {
		String name;
		int imageId;
	}
	
	class weapon {
		String name;
		int imageId;
	}
	
	class location {
		String name;
		int imageId;
	}
	
	class turn{
		int playerIndex;
		int turnType; //should be an ENUM
		int suspectIndex;
		int weaponIndex;
		int locationIndex;
		int alibiIndex;
		int ruledOut; // should be an ENUM
	}
	/*
	String gameName;
	JSONArray players;
	JSONArray suspects;
	JSONArray weapons;
	JSONArray locations;
	JSONArray solution;
	JSONArray turns;
	*/
	
	public clarpGame() {
		// A default constructor is required.
		// can/should I initialize the variables here? will that make the ParseObject dirty?
		// http://blog.parse.com/2013/05/30/parse-on-android-just-got-classier/
		// I think as long as I don't "put" anything, I'm fine...
		
		
	}
	
	public void initialize() {
		
		//players = new JSONArray();
		put("players", new JSONArray());
		
		//suspects = new JSONArray();
		put("suspects", new JSONArray());
		
		//weapons = new JSONArray();
		put("weapons", new JSONArray());
		
		//locations = new JSONArray();
		put("locations", new JSONArray());
		
		//solution = new JSONArray();
		//don't randomly generate a solution yet -- wait for all suspects, weapons, and players to be finished
		put("solution", new JSONArray());
		
		//turns = new JSONArray();
		put("turns", new JSONArray());
		
	}
 
	public String getGameName() {
		return getString("gameName");
	}
	public void setGameName(String name) {
		put("gameName", name);
	}
	
	public void addPlayer( ParseUser user ) throws JSONException
	{
		JSONObject userFbInfo = user.getJSONObject("profile");
		
		JSONObject player = new JSONObject();
		
		player.put("id", user.getUsername()); 
		player.put("name", userFbInfo.getString("firstName"));
		player.put("facebookId", userFbInfo.getString("facebookId"));
		
		// should be randomly assigned prefix
		player.put("prefix", "Sir"); 
		
		// player starts out not-disqualified
		player.put("dq", false); 
		
		// player's facts will be assigned when the game's solution is determined
		player.put("suspectFacts", new JSONArray());
		player.put("weaponFacts", new JSONArray());
		player.put("locationFacts", new JSONArray());
		
		//can't change the existing JSONArray on Parse, we need to overwrite it:
		// grab the existing players
		JSONArray newPlayers = getJSONArray("players");
		//append player to existing players
		newPlayers.put(player);
		// push newly updated players
		put("players", newPlayers);
		
		addSuspect(player);
		
	}
	
	public void addSuspect (JSONObject player) throws JSONException {
		
		JSONObject suspect = new JSONObject();
		
		suspect.put("name", player.getString("prefix") + " " + player.getString("name"));
		
		//can't change the existing JSONArray on Parse, we need to overwrite it:
		// grab the existing suspects
		JSONArray newSuspects = getJSONArray("suspects");
		//append player to existing suspects
		newSuspects.put(suspect);
		// push newly updated suspects
		put("suspects", newSuspects);
		
	}
	
	public void play() {
		// Ah, that takes me back!
	}
}

