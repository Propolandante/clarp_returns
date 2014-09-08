package com.example.clarp_returns;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseFile;
import com.parse.ParseImageView;

public class CardHandAdapter extends ArrayAdapter<ClarpCard> {
	
	private ArrayList<ClarpCard> cards;
	private LayoutInflater vi;

	public CardHandAdapter(Context context, int resourceId, ArrayList<ClarpCard> cards) {
		super(context, resourceId, cards);
		vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.cards = cards;
	}

	
	@Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.card_item, null);
        }
    	
        final ClarpCard card = cards.get(position);
        if (card != null) {
        	// set the card name
            final TextView cardName = (TextView) v.findViewById(R.id.textCardName);
            if (cardName != null) {
            	cardName.setText(card.getCardName());
            }
            // set the card's image
            ParseImageView cardImage = (ParseImageView) v.findViewById(R.id.imageCardPic);
            ParseFile imageFile = card.getPhotoFile();
            
            // set the card's type
            final TextView cardType = (TextView) v.findViewById(R.id.textCardType);
            if (cardType != null){
            	String type = card.getCardType().toString();
            	if (type.equals("Location")) type = "Scene";
            	cardType.setText(type);
            }

            if (imageFile != null) {
            	cardImage.setParseFile(imageFile);
            	cardImage.loadInBackground();
            }
        }
        return v;
    }
}
