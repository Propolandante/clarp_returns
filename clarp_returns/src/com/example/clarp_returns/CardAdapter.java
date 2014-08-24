package com.example.clarp_returns;

import java.util.ArrayList;

import com.example.clarp_returns.TurnHistoryItemAdapter.ViewHolder;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class CardAdapter extends ArrayAdapter<Card> {
	
	private ArrayList<Card> cards;
	private LayoutInflater vi;

	public CardAdapter(Context context, int resourceId, ArrayList<Card> cards) {
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
    	
        final Card card = cards.get(position);
        if (card != null) {
            final TextView cardName = (TextView) v.findViewById(R.id.textCardName);
            final ImageView cardImage = (ImageView) v.findViewById(R.id.imageCardPic);

            if (cardName != null) {
            	cardName.setText(card.name);
            }

            if(cardImage != null) {
        		cardImage.setImageResource(card.pic);
            }
        }
        return convertView;
    }
}
