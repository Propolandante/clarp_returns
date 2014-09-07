package com.example.clarp_returns;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;

public class NotesFragment extends Fragment {
	ArrayList<NoteItem> items;
	public NoteItemAdapter adapter;
	ListView listView;
	
	public NotesFragment(ArrayList<NoteItem> items){
		super();
		this.items = items;
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_notes, container, false);

		listView = (ListView) rootView.findViewById(R.id.listNotes);
		listView.setItemsCanFocus(true);
		adapter = new NoteItemAdapter(getActivity(), android.R.layout.simple_list_item_1,items);
		listView.setAdapter(adapter);
		return rootView;
	}
	
	
	public void add(NoteItem newItem){
		adapter.add(newItem);
	}
	
	public int getNotesSize()
	{
		return adapter.getCount();
	}
	
	public NoteItemAdapter getAdapter(){
		return adapter;
	}
	
	
}
