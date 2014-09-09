package com.example.clarp_returns;

import java.util.ArrayList;
import java.util.Collections;

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

        //Boolean to indicate if the game is started yet
        put("isStarted", false);

        //array of JSONObjects with relevant player information.
        put("players", new JSONArray());

        // this array is a little redundant, but makes querying the user's current ClarpGames MUCH easier
        put("fbUsers", new JSONArray());

        // array of prefixes that have already been used for suspects
        put("usedPrefixes", new JSONArray());

        //array of ObjectId strings that point to the suspect ClarpCards
        put("suspects", new JSONArray());

        //array of ObjectId strings that point to the weapon ClarpCards
        put("weapons", new JSONArray());

        //array of ObjectId strings that point to the location ClarpCards
        put("locations", new JSONArray());

        //don't randomly generate a solution yet -- wait for all suspects, weapons, and players to be finished
        put("solution", new JSONArray());

        put("turns", new JSONArray());

        put("numSuspects", 0);
        put("numWeapons", 0);
        put("numLocations", 0);

        put("gameOver", false);

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

    public Boolean ifStarted()
    {
        return getBoolean("isStarted");
    }

    public void startGame()
    {
        put("isStarted", true);
    }

    public void addFbPlayer(ParseUser user) throws JSONException
    {
        JSONObject userFbInfo = user.getJSONObject("profile");

        //can't change the existing JSONArray on Parse, we need to overwrite it:
        // grab the existing players
        JSONArray newFbUsers = getJSONArray("fbUsers");
        //append player to existing players
        newFbUsers.put(userFbInfo.getString("facebookId"));
        // push newly updated players
        put("fbUsers", newFbUsers);


    }

    public void addPlayer( ParseUser user ) throws JSONException
    {
        // Need to add  user to ClarpGame player list AND add  ClarpGame to  player's game list
        //can't change the existing JSONArray on Parse, we need to overwrite it:
        // grab the existing players
        JSONArray newPlayers = getJSONArray("players");
        JSONObject userFbInfo = user.getJSONObject("profile");
        JSONObject player = new JSONObject();

        boolean playerAlreadyAdded = false;

        // check all JSONObjects in newPlayers for same player id
        for (int i = 0; i < newPlayers.length(); i++) {
            player = newPlayers.getJSONObject(i);
            if(player.getString("id") == user.getUsername()) {
                // player ids are equal, stop loop and
                // don't add new player
                playerAlreadyAdded = true;
                Log.d(ClarpApplication.TAG, "HEY Player is already in this game, won't add");
                break;
            }
        }


        if(!playerAlreadyAdded) {

            // reset player
            player = new JSONObject();

            // First, add user to ClarpGame player list:

            player.put("id", user.getUsername());
            player.put("name", userFbInfo.getString("firstName"));
            player.put("facebookId", userFbInfo.getString("facebookId"));

            // should be randomly assigned prefix
            String prefix = getSuspectPrefix();
            player.put("prefix", prefix);
            Log.d(ClarpApplication.TAG, "Value of prefix is " + prefix);

            // player starts out not-disqualified
            player.put("dq", false);

            // player's facts will be assigned when the game's solution is determined
            player.put("facts", new JSONArray());

            // Debug BEFORE for adding player
            Log.d(ClarpApplication.TAG, "Clarping the new player list now");
            Log.d(ClarpApplication.TAG, "Current list:");
            for (int i = 0; i < newPlayers.length(); ++i)
            {
                Log.d(ClarpApplication.TAG, (newPlayers.getJSONObject(i).getString("name")));
            }
            Log.d(ClarpApplication.TAG, "Player to be added: " + player.getString("name"));

            //append player to existing players
            newPlayers.put(player);
            // push newly updated players
            put("players", newPlayers);

            // Debug AFTER for adding player
            Log.d(ClarpApplication.TAG, "After putting:");
            for (int i = 0; i < newPlayers.length(); ++i)
            {
                Log.d(ClarpApplication.TAG, (newPlayers.getJSONObject(i).getString("name")));
            }

            Log.d(ClarpApplication.TAG, "Adding suspect now");
            addSuspect(player);
            addFbPlayer(user);
        }
    }

    public void addSuspect (JSONObject player) throws JSONException {

        ParseUser currentUser = ParseUser.getCurrentUser();
        final ClarpCard suspect = new ClarpCard();
        suspect.setPhotoFile(currentUser.getParseFile("profilePicture"));

        Log.d(ClarpApplication.TAG, "card created");

        suspect.initialize(ClarpCard.CardType.SUSPECT, player.getString("prefix") + " " + player.getString("name"), getObjectId());


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

                increment("numSuspects");

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

    public void setSolution (String s, String w, String l)
    {
        JSONArray solution = new JSONArray();
        solution.put(s);
        solution.put(w);
        solution.put(l);

        put("solution", solution);
    }

    public void end()
    {
        put("gameOver", false);
    }

    @Override
    public String toString()
    {
        return getGameName();

    }

    public void rotateTurn() throws JSONException {

        String prevId = getString("whoseTurn");
        String nextId = null;
        JSONArray players = getJSONArray("fbUsers");

        /*
         * Loop through the players to find the previous player
         */

        int p = 0;

        for (p = 0; p < players.length(); ++p)
        {
            if (players.getString(p).equals(prevId))
            {
                Log.d(ClarpApplication.TAG, "prev index: " + p);
                break;
            }
        }

        /*
         * now find the player after that, and make it their turn
         */

        p++;
        if (p == players.length()){p=0;}

        Log.d(ClarpApplication.TAG, "next index: " + p);
        nextId = players.getString(p);

        put("whoseTurn", nextId);

    }

    public String getSuspectPrefix() {
        String prefix = null;

        ArrayList<String> prefixes = new ArrayList<String>();

        for(int i = 0; i < ClarpApplication.PREFIXES.size(); ++i) {
            prefixes.add( i, ClarpApplication.PREFIXES.get(i) );
        }

        JSONArray used = getJSONArray("usedPrefixes");
        if(used == null) {
            Log.d(ClarpApplication.TAG, "No used prefixes yet");
            used = new JSONArray();
        }

        Collections.shuffle(prefixes);

        for(int i = 0; i < prefixes.size(); ++i) {
            boolean skip = false;
            prefix = prefixes.get(i);
            for(int j = 0; j < used.length(); ++j) {

                try {
                    Log.d(ClarpApplication.TAG, "Checking for equal prefixes");
                    if( (prefixes.get(i)) .equals( used.getString(j) )){
                        prefix = prefixes.get(i);
                        Log.d(ClarpApplication.TAG, "Just picked first unused prefix: " + prefix);
                        skip = true;
                        break;
                    }
                } catch (JSONException e) {
                    Log.d(ClarpApplication.TAG, "Error getting JSON used prefix string");
                    Log.e("Error", "Error message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            if(!skip) {
                Log.d(ClarpApplication.TAG, "Picked an unused prefix, returning with prefix " + prefix);
                break;
            }
        }


        return prefix;
    }
}

