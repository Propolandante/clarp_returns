package com.example.clarp_returns;

public class gameListItem {
	
	private String name;
	private String gameId;
	
	// Constructors
	
	public gameListItem()
	{
		name = "Default Constructor Name";
		gameId = "420";
	}
	
	public gameListItem(String n, String id)
	{
		name = n;
		gameId = id;
	}
	
	
	// Getters and Setters
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String n)
	{
		name = n;
	}
	
	public String getId()
	{
		return gameId;
	}
	
	public void setId(String id)
	{
		gameId = id;
	}
	
	// Describe how the Item will be displayed in the ListView
	
	@Override
	public String toString()
	{
		return name;
		
	}
	
	

}
