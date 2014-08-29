package com.example.clarp_returns;

public class Card {
	public String name;
	public int pic;
	public CardTypes type;
	
	public Card(String name, int pic, CardTypes type){
		this.name = name;
		this.pic = pic;
		this.type = type;
	}
	
	public boolean equals(Card c){
		if (this.name == c.name  && this.pic == c.pic && this.type == c.type)
			return true;
		return false;
	}

}
