package com.example.clarp_returns;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

public class CardViewAdapter extends ParseQueryAdapter<ClarpCard>{

    public CardViewAdapter(Context context) {
        super(context, new ParseQueryAdapter.QueryFactory<ClarpCard>() {
            @Override
            public ParseQuery<ClarpCard> create() {
                ParseQuery query = new ParseQuery("clarpCard");
                return query;
            }
        });

        Log.d(ClarpApplication.TAG, "CardViewAdapter constructed");
    }

    @Override
    public View getItemView(ClarpCard card, View v, ViewGroup parent) {

        if(v == null) {
            v = View.inflate(getContext(), R.layout.item_list_cards, null);
        }

        super.getItemView(card, v, parent);

        Log.d(ClarpApplication.TAG, "got itemView of " + card.getCardName());

        ParseImageView cardImage = (ParseImageView) v.findViewById(R.id.icon);
        ParseFile photoFile = card.getParseFile("photo");
        if(photoFile != null) {
            cardImage.setParseFile(photoFile);
            cardImage.loadInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    //nothing
                }
            });

            Log.d(ClarpApplication.TAG, "loaded photo " + card.getCardName());
        } else {
            Log.d(ClarpApplication.TAG, "PhotoFile is null");
        }


        TextView nameTextView = (TextView) v.findViewById(R.id.cardName);
        nameTextView.setText(card.getCardName());
        return v;
    }
}
