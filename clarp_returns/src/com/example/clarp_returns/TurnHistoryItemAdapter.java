package com.example.clarp_returns;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TurnHistoryItemAdapter extends ArrayAdapter<TurnHistoryItem>
		implements OnClickListener{
	
	private static final int TYPE_SUGGEST = 0;
    private static final int TYPE_ACCUSE = 1;
    private static final int TYPE_MAX_COUNT = 2;
    
    
	private ArrayList<TurnHistoryItem> items;
	private LayoutInflater vi;
	
	public TurnHistoryItemAdapter(Context context, int textViewResourceId, ArrayList<TurnHistoryItem> items) {
        super(context, textViewResourceId, items);
        vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.items = items;
    }
	
	@Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }
	
	@Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }

	@Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int type = getItemViewType(position);
        if (convertView == null) {
        	holder = new ViewHolder();
        	switch(type){
        		case TYPE_SUGGEST:
        			convertView = vi.inflate(R.layout.turn_item, null);
        			break;
        		case TYPE_ACCUSE:
        			convertView = vi.inflate(R.layout.dark_turn_item, null);
        			break;
        	}
            convertView.setTag(holder);
            
    	}else{
    		holder = (ViewHolder)convertView.getTag();
    	}
        ImageView suspectPic = (ImageView) convertView.findViewById(R.id.imageSuspect);
        suspectPic.setImageResource(items.get(position).person);
        ImageView weaponPic = (ImageView) convertView.findViewById(R.id.imageWeapon);
        weaponPic.setImageResource(items.get(position).weapon);
        ImageView scenePic = (ImageView) convertView.findViewById(R.id.imageLocation);
        scenePic.setImageResource(items.get(position).location);
        TextView result = (TextView) convertView.findViewById(R.id.textResult);
        result.setText(items.get(position).result);
           
        //final TurnHistoryItem item = items.get(position);
        
        return convertView;
    }
	
	static class ViewHolder {
		
	}
	
	
	
	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

}


