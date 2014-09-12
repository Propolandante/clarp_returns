package com.example.clarp_returns;

import java.util.ArrayList;

import com.example.clarp_returns.TurnHistoryItemAdapter.ViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

public class NoteItemAdapter extends ArrayAdapter<NoteItem> {

	private static final int TYPE_NOTE = 0;
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_MAX_COUNT = 2;
	
	private ArrayList<NoteItem> items;
	private LayoutInflater vi;
	
	public NoteItemAdapter(Context context,	int textViewResourceId, ArrayList<NoteItem> items) {
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
        		case TYPE_NOTE:
        			convertView = vi.inflate(R.layout.note_item, null);
        			break;
        		case TYPE_HEADER:
        			convertView = vi.inflate(R.layout.alert_item, null);
        			break;
        			
        	}
            convertView.setTag(holder);
            
    	}else{
    		holder = (ViewHolder)convertView.getTag();
    	}
        if (type == TYPE_NOTE){
	        TextView itemName = (TextView) convertView.findViewById(R.id.textNoteName);	        
	        itemName.setText(items.get(position).name);
	        final EditText editNote = (EditText) convertView.findViewById(R.id.editNote);
	        
	        if (editNote != null){
	        	editNote.setText(items.get(position).notes);
	        }
	        
	        editNote.setOnFocusChangeListener(new OnFocusChangeListener(){

				@Override
				public void onFocusChange(View arg0, boolean hasFocus) {
					// TODO Auto-generated method stub
					if (!hasFocus){
						items.get(position).notes = editNote.getText().toString();
					}
				}
            	
            });
	        	
        }else{
        	TextView header = (TextView) convertView.findViewById(R.id.textAlert);
        	header.setText(items.get(position).name);
        	
        }
        
        
           
        //final TurnHistoryItem item = items.get(position);
        
        return convertView;
    }

}
