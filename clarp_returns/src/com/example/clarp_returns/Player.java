package com.example.clarp_returns;

import java.util.ArrayList;

public class Player {
	public String name;
	public ArrayList<Card> cards;
	
	public Player(String name){
		this.name = name;
		cards = new ArrayList<Card>();
	}

}
