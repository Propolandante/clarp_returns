package com.example.clarp_returns;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

@ParseClassName("ClarpCard")
public class ClarpCard extends ParseObject {

    public ClarpCard() {
        // A default constructor is required.
        // can/should I initialize the variables here? will that make the ParseObject dirty?
        // http://blog.parse.com/2013/05/30/parse-on-android-just-got-classier/
        // I think as long as I don't "put" anything, I'm fine...


    }

    public void initialize(String type, String name, String id) {

        setCardType(type);
        setCardName(name);
        setCardGame(id);
    }

    public void setCardType(String type) {

        // 0 is player, 1 is weapon, 2 is location
        //this should become an ENUM

        // i think for Parse this actually has to be a string
        // (the rating in Mealspotting is a string, despite
        // actually referring to a number...)

        put("cardType", type);

    }

    // see note about type being a String
    public String getCardType() {
        return getString("cardType");
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

    public void setPhotoFile(ParseFile file){
        put("photo", file);
    }
    
    public String getCardGame()
    {
    	return getString("gameId");
    }
    
    public void setCardGame(String id)
    {
    	put("gameId", id);
    }
}