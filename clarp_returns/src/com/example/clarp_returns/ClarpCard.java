package com.example.clarp_returns;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

@ParseClassName("ClarpCard")
public class ClarpCard extends ParseObject {

    // to access this outside of ClarpCard, use
    // ClarpCard.CardType.SUSPECT (or whatever type)
    // for display purposes, use
    // ClarpCard.CardType.SUSPECT.toString()
    static enum CardType {
        SUSPECT("Suspect"),
        WEAPON("Weapon"),
        LOCATION("Location");

        private String typeName;

        CardType(String typeName) {
            this.typeName = typeName;
        }

        @Override public String toString() {
            return typeName;
        }

        public static CardType fromString(String typeName) {
            if (typeName != null) {
                for (CardType type : CardType.values()) {
                    if (typeName.equalsIgnoreCase(type.typeName)) {
                        return type;
                    }
                }
            }
            Log.d(ClarpApplication.TAG, "No constant with typeName " + typeName + " found");
            throw new IllegalArgumentException("No constant with typeName " + typeName + " found");
        }
    }

    public ClarpCard() {
        // A default constructor is required.
        // can/should I initialize the variables here? will that make the ParseObject dirty?
        // http://blog.parse.com/2013/05/30/parse-on-android-just-got-classier/
        // I think as long as I don't "put" anything, I'm fine...


    }

    public void initialize(CardType type, String name, String id) {

        setCardType(type);
        setCardName(name);
        setCardGame(id);
    }

    public void setCardType(CardType type) {
        put("cardType", type.toString());

    }

    // see note about type being a String
    public CardType getCardType() {
        return CardType.fromString(getString("cardType"));
    }

    public void setCardName(String name) {
        put("cardName", name);

    }

    public String getCardName() {
        return getString("cardName");
    }

    public ParseFile getPhotoFile() {
        return getParseFile("photo");
    }

    public void setPhotoFile(ParseFile file) {
        put("photo", file);
    }

    public String getCardGame() {
        return getString("gameId");
    }

    public void setCardGame(String id) {
        put("gameId", id);
    }
}