package com.example.clarp_returns;

public class Game {
	private String name;
    private Integer id;
    private Integer type; // integer codes: 1 for murder mystery
    private String description;
    //private ArrayList users;

    public Game(String name, Integer id, Integer type, String description) {
        super();
        this.name = name;
        this.id = id;
        this.type = type;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Game [name=" + name + ", id=" + id + "]";
    }
}
