package com.example.clarp_returns;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;

public class CardListViewAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    //ImageLoader imageLoader;
    private List<ClarpCard> cardList = null;
    private ArrayList<ClarpCard> cardArrayList;

    public CardListViewAdapter(Context context, List<ClarpCard> cardList) {
        this.context = context;
        this.cardList = cardList;
        inflater = LayoutInflater.from(context);
        this.cardArrayList = new ArrayList<ClarpCard>();
        this.cardArrayList.addAll(cardList);
        //imageLoader = new ImageLoader(context);
    }

    public class ViewHolder {
        TextView cardType;
        TextView cardName;
        ParseImageView cardImage;
    }

    @Override
    public int getCount() {
        return cardList.size();
    }

    @Override
    public Object getItem(int position) {
        return cardList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.cardlistview_item, null);
            holder.cardType = (TextView) view.findViewById(R.id.cardType);
            holder.cardName = (TextView) view.findViewById(R.id.cardName);
            holder.cardImage = (ParseImageView) view.findViewById(R.id.cardImage);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.cardType.setText(cardList.get(position).getCardType());
        holder.cardName.setText(cardList.get(position).getCardName());

        //imageLoader.DisplayImage(cardList.get(position).getPhotoFile(), holder.cardImage);

        holder.cardImage.setPlaceholder(context.getResources().getDrawable(R.drawable.jordan));
        holder.cardImage.setParseFile(cardList.get(position).getPhotoFile());
        holder.cardImage.loadInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                Log.d("ParseImageView",
                        "Fetched! Data length: " + data.length + ", or exception: " + e.getMessage());
            }
        });

        //        view.setOnClickListener(new OnClickListener() {
        //
        //            @Override
        //            public void onClick(View arg0) {
        //                Intent intent = new Intent(context, SingleItemView.class);
        //
        //                intent.putExtra("cardType", (cardList.get(position).getCardType()));
        //                intent.putExtra("cardName", (cardList.get(position).getCardName()));
        //                intent.putExtra("cardImage", (cardList.get(position).getPhotoFile().getUrl()));
        //
        //                context.startActivity(intent);
        //            }
        //        });

        return view;
    }


}
