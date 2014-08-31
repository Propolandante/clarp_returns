package com.example.clarp_returns;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

public class CardQueryAdapter extends ParseQueryAdapter<ClarpCard>{

    public CardQueryAdapter(Context context) {

        super(context, new ParseQueryAdapter.QueryFactory<ClarpCard>() {
            @Override
            public ParseQuery create() {
                ParseQuery query = new ParseQuery("ClarpCard");
                return query;
            }
        });
    }

    // Customize the layout by overriding getItemView
    @Override
    public View getItemView(ClarpCard card, View v, ViewGroup parent) {
        if (v == null) {
            v = View.inflate(getContext(), R.layout.cardlistview_item, null);
        }


        super.getItemView(card, v, parent);

        // add and download the image
        ParseImageView cardImage = (ParseImageView) v.findViewById(R.id.cardImage);
        ParseFile imageFile = card.getPhotoFile();
        if (imageFile != null) {
            cardImage.setParseFile(imageFile);
            cardImage.loadInBackground();
        }

        // add the name and type text
        TextView nameTextView = (TextView) v.findViewById(R.id.cardName);
        nameTextView.setText(card.getCardName());

        TextView typeTextView = (TextView) v.findViewById(R.id.cardType);
        typeTextView.setText(card.getCardType());

        return v;
    }

}
