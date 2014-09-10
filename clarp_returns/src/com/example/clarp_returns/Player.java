package com.example.clarp_returns;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Player {
    public String name;
    public ArrayList<Card> cards;

    private String prefix;
    private String fbId;
    private ArrayList<String> cardIds;
    private Boolean disqualified;

    public Player(String name){
        this.name = name;
        cards = new ArrayList<Card>();
    }

    public Player(JSONObject p) throws JSONException
    {
        name = p.getString("name");
        prefix = p.getString("prefix");
        fbId = p.getString("facebookId");
        disqualified = p.getBoolean("dq");

        cardIds = new ArrayList<String>();

        JSONArray cardList = p.getJSONArray("facts");

        for (int i = 0; i < cardList.length(); ++i)
        {
            cardIds.add((String) cardList.get(i));
            Log.d(ClarpApplication.TAG, "Gave card id: " + cardList.get(i) + " to " + this.name);
        }

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {

        return this.name;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {

        return this.prefix;
    }

    public String getFullName() {

        String fullName = this.prefix + " " + this.name;

        return fullName;
    }

    public void setFbId(String fbId) {
        this.fbId = fbId;
    }

    public String getFbId() {

        return this.fbId;
    }

    public Boolean isDisqualified() {

        return this.disqualified;
    }

    public void disqualify() {

        this.disqualified = true;
    }

    public Boolean isAlibi(String cardId) {

        /*
         * Use this to check if this player has the given card
         */

        for (int i = 0; i < cardIds.size(); ++i)
        {
            if (cardIds.get(i).equals(cardId))
            {
                return true;
            }
        }

        return false;
    }

    public ArrayList<String> getCardIds()
    {
        return cardIds;
    }

    public void giveCard(ClarpCard c)
    {
        this.cardIds.add(c.getObjectId());
    }





}
