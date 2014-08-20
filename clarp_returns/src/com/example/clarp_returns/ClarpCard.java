package com.example.clarp_returns;

import com.parse.*;

@ParseClassName("clarpCard")
public class clarpCard extends ParseObject {
	
	public clarpCard() {
		// A default constructor is required.
		// can/should I initialize the variables here? will that make the ParseObject dirty?
		// http://blog.parse.com/2013/05/30/parse-on-android-just-got-classier/
		// I think as long as I don't "put" anything, I'm fine...
		
		
	}
		
	public void initialize(int type, String name) {
		
		setCardType(type);
		setCardName(name);
	}
	
	public void setCardType(int type) {
		
		// 0 is player, 1 is weapon, 2 is location
		//this should become an ENUM
		
		put("cardType", type);
		
	}
	
	public void setCardName(String name) {
		
		put("cardName", name);
		
	}
}