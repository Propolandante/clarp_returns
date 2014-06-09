package com.example.clarp_returns;

import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class TurnHistoryItemAdapter extends ArrayAdapter<TurnHistoryItem>
		implements OnClickListener{
	private ArrayList<TurnHistoryItem> items;
	
	public TurnHistoryItemAdapter(Context context, int textViewResourceId, ArrayList<TurnHistoryItem> items) {
        super(context, textViewResourceId, items);
    this.items = items;
    }

	@Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.turn_item, null);
            
    	}
           
        final TurnHistoryItem item = items.get(position);
        /*if (item != null) {
            /*final EditText itemName = (EditText) v.findViewById(R.id.editText_ItemName);
            final TextView tallyValText = (TextView) v.findViewById(R.id.textView_TallyVal);
            Button plusButton = (Button) v.findViewById(R.id.buttonPlus);
            Button minusButton = (Button) v.findViewById(R.id.buttonMinus);
            Button deleteButton = (Button) v.findViewById(R.id.buttonDel);*/

            /*if (itemName != null) {
            	itemName.setText(item.tallyName);
            }

            if(tallyValText != null) {
            	tallyValText.setText(""+item.tallyVal);
            }
            
            plusButton.setOnClickListener(new OnClickListener(){
            	@Override
            	public void onClick(View view){
            		item.tallyVal += 1;
            		tallyValText.setText(""+item.tallyVal);
            	}
            });
            
            minusButton.setOnClickListener(new OnClickListener(){
            	@Override
            	public void onClick(View view){
            		item.tallyVal -= 1;
            		tallyValText.setText(""+item.tallyVal);
            	}
            });
            
            deleteButton.setOnClickListener(new OnClickListener(){
            	@Override
            	public void onClick(View view){
            		items.remove(position);
            		notifyDataSetChanged();
            	}
            });
            itemName.setOnFocusChangeListener(new OnFocusChangeListener(){

				@Override
				public void onFocusChange(View arg0, boolean hasFocus) {
					// TODO Auto-generated method stub
					if (!hasFocus){
						item.tallyName = itemName.getText().toString();
					}
				}
            	
            });
        }*/
        
        
    return v;
    }
	
	
	
	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

}
