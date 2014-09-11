package com.example.clarp_returns;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.example.clarp_returns.TurnHistoryItemAdapter.ViewHolder;

public class NoteItemAdapter extends ArrayAdapter<NoteItem> {

    private static final int TYPE_NOTE = 0;
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_MAX_COUNT = 2;

    private ArrayList<NoteItem> items;
    private LayoutInflater vi;
    private Context context;

    public NoteItemAdapter(Context context,	int textViewResourceId, ArrayList<NoteItem> items) {
        super(context, textViewResourceId, items);
        vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.items = items;
        this.context = context;
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
            // this is what i was using to save the checkbox states in shared preferences

            // http://stackoverflow.com/questions/9080682/how-do-i-make-a-checkbox-stay-in-the-same-state-every-time-i-open-my-app

            CheckBox checkNote = (CheckBox) convertView.findViewById(R.id.checkNote);
            //items.get(position).setChecked(getBooleanFromPreferences("isChecked"));

            checkNote.setChecked(items.get(position).getChecked());
            checkNote.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                    //context.putBooleanInPreferences(isChecked, "isChecked");
                }
            });

        }else{
            TextView header = (TextView) convertView.findViewById(R.id.textAlert);
            header.setText(items.get(position).name);

        }
        //final TurnHistoryItem item = items.get(position);

        return convertView;
    }

    // i think these need to go in NotesActivity?

    //    public void putBooleanInPreferences(boolean isChecked,String key){
    //        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
    //        SharedPreferences.Editor editor = sharedPreferences.edit();
    //        editor.putBoolean(key, isChecked);
    //        editor.commit();
    //    }
    //
    //    public boolean getBooleanFromPreferences(String key){
    //        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
    //        Boolean isChecked = sharedPreferences.getBoolean(key, false);
    //        return isChecked;
    //    }


}
