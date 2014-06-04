package com.example.clarp_returns;

public class Message {
	
	private String text;
    private String image; // not sure about

    public Message(String text, String image) {
        super();
        this.text = text;
        this.image = image;
    }

    public Message() {
        super();
        // TODO Auto-generated constructor stub
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Message [text=" + text + ", image=" + image + "]";
    }

}
