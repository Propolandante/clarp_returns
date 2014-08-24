package com.example.clarp_returns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

@ParseClassName("ClarpGame")
public class ClarpGame extends ParseObject {

    public ClarpGame() {
        // A default constructor is required.
        // can/should I initialize the variables here? will that make the ParseObject dirty?
        // http://blog.parse.com/2013/05/30/parse-on-android-just-got-classier/
        // I think as long as I don't "put" anything, I'm fine...


    }

    public void initialize() throws ParseException {

        //array of JSONObjects with relevant player information.
        put("players", new JSONArray());

        //array of ObjectId strings that point to the suspect ClarpCards
        put("suspects", new JSONArray());

        //array of ObjectId strings that point to the weapon ClarpCards
        put("weapons", new JSONArray());

        //array of ObjectId strings that point to the location ClarpCards
        put("locations", new JSONArray());

        //don't randomly generate a solution yet -- wait for all suspects, weapons, and players to be finished
        put("solution", new JSONArray());

        put("turns", new JSONArray());

        // I need to be sure this object has an objectId before addPlayer is called.
        // Usually we're supposed to use saveInBackground, so there's probably a better way than this
        // but if it works and doesn't slow everything down.... fuck it!
        save();

    }

    public String getGameName() {
        return getString("gameName");
    }
    public void setGameName(String name) {
        put("gameName", name);
    }
    
    public String getOwner() {
        return getString("owner");
    }
    public void setOwner(ParseUser user) {
        put("owner", user.getObjectId());
    }

    public void addPlayer( ParseUser user ) throws JSONException
    {
        // Need to add  user to ClarpGame player list AND add  ClarpGame to  player's game list

        // First, add user to ClarpGame player list:

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

        Log.d(ClarpApplication.TAG, "Adding suspect now");
        addSuspect(player);



        Log.d(ClarpApplication.TAG, "Attempting to add game to User");

        // Next, add ClarpGame to player's game list
        //can't change the existing JSONArray on Parse, we need to overwrite it:
        // grab the existing players
        JSONArray newGames;
        if (user.get("games") != null) // if it exists
        {
            newGames = user.getJSONArray("games");
            Log.d(ClarpApplication.TAG, "grabbing exitsing game list");
        }
        else  // otherwise create it
        {
            newGames = new JSONArray();
            Log.d(ClarpApplication.TAG, "creating new game list");
        }
        //append player to existing players
        newGames.put(getObjectId());
        Log.d(ClarpApplication.TAG, "added game's objectId: " + getObjectId());
        // push newly updated players to the server
        user.put("games", newGames);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.d(ClarpApplication.TAG, "user info saved to server");
            }
        });



    }

    public void addSuspect (JSONObject player) throws JSONException {


        final ClarpCard suspect = new ClarpCard();

        Log.d(ClarpApplication.TAG, "card created");

        suspect.initialize(Integer.toString(0), player.getString("prefix") + " " + player.getString("name"));


        // ObjectId isn't created until the object is saved
        suspect.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                //Now that the card object is saved, we can add its ObjectId to the game's suspect array
                //can't change the existing JSONArray on Parse, we need to overwrite it:
                // grab the existing suspects
                JSONArray newSuspects = getJSONArray("suspects");
                //append player to existing suspects
                newSuspects.put(suspect.getObjectId());
                // push newly updated suspects
                put("suspects", newSuspects);

                //now we need to save the game so that the new suspect array is reflected in the cloud
                saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {

                        Log.d(ClarpApplication.TAG, "suspect list updated");

                    }
                });


            }
        });



    }

    @Override
	public String toString()
	{
		return getGameName();
		
	}
}

