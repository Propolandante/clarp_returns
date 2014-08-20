package com.example.clarp_returns;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class HistoryFragment extends Fragment {
	ArrayList<TurnHistoryItem> items;
	public TurnHistoryItemAdapter adapter;
	ListView listView;
	
	public HistoryFragment(ArrayList<TurnHistoryItem> items){
		super();
		this.items = items;
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View rootView = inflater.inflate(R.layout.fragment_game, container, false);

		
		listView = (ListView) rootView.findViewById(R.id.listViewHistory);
		adapter = new TurnHistoryItemAdapter(getActivity(), android.R.layout.simple_list_item_1,items);
		listView.setAdapter(adapter);
		return rootView;
	}
	
	
	public void add(TurnHistoryItem newItem){
		adapter.add(newItem);
		scrollMyListViewToBottom();
		
	}
	
	public TurnHistoryItemAdapter getAdapter(){
		return adapter;
	}

	private void scrollMyListViewToBottom() {
		listView.post(new Runnable() {
	        @Override
	        public void run() {
	            // Select the last row so it will scroll into view...
	        	listView.setSelection(adapter.getCount() - 1);
	        }
	    });
	}
	
}
