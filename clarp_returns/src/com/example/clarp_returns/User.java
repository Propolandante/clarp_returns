package com.example.clarp_returns;

import java.util.ArrayList;

public class User {
    private int id; // id
    private ArrayList<Game> gamesList;

    public User(int id, ArrayList<Game> gamesList) {
        super();
        this.id = id;
        this.gamesList = gamesList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<Game> getGamesList() {
        return gamesList;
    }

    public void setGamesList(ArrayList<Game> gamesList) {
        this.gamesList = gamesList;
    }
}
