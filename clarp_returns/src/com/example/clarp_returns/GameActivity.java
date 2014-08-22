package com.example.clarp_returns;

import java.util.ArrayList;
import java.util.Collections;

import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.widget.ListPopupWindow;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;


public class GameActivity extends ActionBarActivity implements OnItemClickListener{
	
	private static final int TYPE_SUGGEST = 0;
    private static final int TYPE_ACCUSE = 1;
	
	ViewPager viewPager = null;
	int num_of_players = 6;
	int num_of_weapons = 6;
	int num_of_scenes = 6;
	int total_cards;
	int cur_player = 0;
	ArrayList<Card> cards = new ArrayList<Card>();
	ArrayList<Player> players = new ArrayList<Player>();
	ArrayList<String> weapons = new ArrayList<String>();
	ArrayList<String> scenes = new ArrayList<String>();
	PopupWindow pw;
	ListPopupWindow lpw;
	MyAdapter adapter;
	HistoryFragment historyFragment;
	int page = 0;
	Player selectedPlayer;
	
	/*
	 * This enum is currently in its own class... 
	 * can it be moved to ClarpApplication to make it a global enum that doesn't need its own file and class?
	 * Just a thought
	 * -Derky
	 */
	CardTypes selectType = CardTypes.SUSPECT;
	
	
	/*
	 * I moved buildDeck() and dealCards() down below onCreate() and onResume()
	 * Just to be more consistent with the flow of our other Activities
	 * -DJD
	 */
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        total_cards = num_of_players + num_of_weapons + num_of_scenes;
        
        /*
         * We may not have to add all the cards in this step, that might be handled by the PreGameActivity (to be created)
         * -Dongahue
         */
        
        
        
        /*
         * Our current framework distinguishes between "player" and suspect".
         * Players are the ones taking turns, suspects are cards
         * Every player is a suspect, but often there will be more suspects than player.
         * I suspect (heh) that this is meant to be suspects and not players
         * Regardless, this is all placeholder code. Once we implement PreGameActivity this will all be done
         * And with real user-generated cards!
         * -DerkDerkDerk
         */
        players.add(new Player("Joe"));
        players.add(new Player("Derek"));
        players.add(new Player("Brittany"));
        players.add(new Player("Joe2"));
        players.add(new Player("Jordan"));
        players.add(new Player("Golde"));
        selectedPlayer = players.get(0);
        
        weapons.add("poison");
        weapons.add("car battery");
        weapons.add("spoon");
        weapons.add("broken dvd");
        weapons.add("plastic bag");
        weapons.add("screwdriver");
        
        scenes.add("garage");
        scenes.add("coffee shop");
        scenes.add("park");
        scenes.add("pool");
        scenes.add("Jeffs House");
        scenes.add("overpass");
        buildDeck();
        dealCards();
        
        
        
		
        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
            .add(R.id.container, new PlaceholderFragment()).commit();
        }*/
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	
    	/*
    	 * I know what some of these words mean!
    	 * The Snapchat-style swiping is really cool
    	 * -Dennis
    	 */
    	
    	viewPager = (ViewPager) findViewById(R.id.pager);
		FragmentManager fragmentManager = getSupportFragmentManager();
		adapter = new MyAdapter(fragmentManager);
        viewPager.setAdapter(adapter);
    	
    	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
//    public static class PlaceholderFragment extends Fragment {
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_game, container,
//                    false);
//            return rootView;
//        }
//    }
    
    private void buildDeck(){
    	
    	/*
    	 * This will be all Parse-ified
    	 * Once this activity has started, all of the ObjectIds of the cards created in PreGameActivity 
    	 * will already have been stored in their appropriate suspects[] weapons[] and locations[] arrays
    	 * Though, maybe we should take Joe's approach and just store them all in one array called cards[]
    	 * We will need to be able to just list them separately at times (like when making selections),
    	 * but a simple if statement within a for loop can be used to add them to the ListView's Array
    	 * Either way, this function will really only need to shuffle the existing deck, since it will already be built
    	 * In that case, the shuffling can probably be done in dealCards(), with this function deleted entirely
    	 * -djdonahu
    	 */
    	
		for(int i = 0; i < num_of_players; ++i){
			Resources res = getResources();
			String picName = players.get(i).name.toLowerCase().replaceAll(" ", "_");
			int resID = res.getIdentifier(picName, "drawable", getPackageName());
			cards.add(new Card(players.get(i).name, resID, CardTypes.SUSPECT));
		}
		for(int i = 0; i < num_of_weapons; ++i){
			Resources res = getResources();
			String picName = weapons.get(i).toLowerCase().replaceAll(" ", "_");
			int resID = res.getIdentifier(picName, "drawable", getPackageName());
			cards.add(new Card(weapons.get(i),resID,CardTypes.WEAPON));
		}
		for(int i = 0; i < num_of_scenes; ++i){
			Resources res = getResources();
			String picName = scenes.get(i).toLowerCase().replaceAll(" ", "_");
			int resID = res.getIdentifier(picName, "drawable", getPackageName());
			cards.add(new Card(scenes.get(i),resID,CardTypes.SUSPECT));
		}
		Collections.shuffle(cards);
	}
	
	public void dealCards(){
		
		/*
		 * I think we can all agree that this is a sexy, sexy string of functions.
		 * I wouldn't change a thing.
		 * Except for how it needs to use Parse.
		 * Aw, we have to change it.
		 * Fuck.
		 * -His Derkiness
		 */
		
		int player = 0;
		for (int i = 0; i < cards.size()-1; ++i){
			players.get(player++).cards.add(cards.get(i));
			if (player == num_of_players) player = 0;
		}
	}
    
    
    public void clickSuggest(View v) {
		LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		View popupView = inflater.inflate(R.layout.popup_suggest,null);
		popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		pw = new PopupWindow(popupView,popupView.getMeasuredWidth(),popupView.getMeasuredHeight());
		pw.setAnimationStyle(android.R.style.Animation_Dialog);
		pw.showAtLocation(findViewById(R.id.layoutGame), Gravity.CENTER,0,0);
		ArrayList<Card> suspectList = new ArrayList<Card>();
		for (Card c : cards){
			if (c.type == CardTypes.SUSPECT)
				suspectList.add(c);
		}
		
		ListView listCards = (ListView) findViewById(R.id.listCards);
		final CardAdapter adapter = new CardAdapter(GameActivity.this, R.layout.select_item, suspectList);
		
		/*
		 * I get a NPE when the following line attempts to execute
		 * Pretty sure this is a known bug Joe is working on though
		 * Just wanted to be sure.
		 * - Diligent Derk
		 */
		
		listCards.setAdapter(adapter);
		
		
//		ImageView playerPic = (ImageView) findViewById(R.id.imageSuspectSelect);
//		Resources res = getResources();
//		String picName = "derek";
//		int resID = res.getIdentifier(picName, "drawable", getPackageName());
//		playerPic.setImageResource(resID);
		
	}
    
    public void clickAccuse(View v) {
		LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		View popupView = inflater.inflate(R.layout.popup_accuse,null);
		popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		pw = new PopupWindow(popupView,popupView.getMeasuredWidth(),popupView.getMeasuredHeight());
		pw.setAnimationStyle(android.R.style.Animation_Dialog);
		pw.showAtLocation(findViewById(R.id.layoutGame), Gravity.CENTER,0,0);
		
	}
    
    public void clickSelectSuspect(View v) {
		//LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		//ImageView anchor =(ImageView) findViewById(R.id.imageSuspectSelect);
		//lpw = new ListPopupWindow(GameActivity.this);
		ArrayList<Card> suspectList = new ArrayList<Card>();
		for (Card c : cards){
			if (c.type == CardTypes.SUSPECT)
				suspectList.add(c);
		}
		
		ListView listCards = (ListView) findViewById(R.id.listCards);
		final CardAdapter adapter = new CardAdapter(GameActivity.this, R.layout.select_item, suspectList);
		listCards.setAdapter(adapter);
//		lpw.setAnchorView(anchor);
//		lpw.setContentWidth(400);
//		lpw.setModal(false);
//		lpw.setOnItemClickListener(GameActivity.this);
		selectType = CardTypes.SUSPECT;
//		lpw.show();
//		anchor.setOnClickListener(new OnClickListener(){
//			public void onClick(View v) {
//				
//			}
//		});
		
		
	}
    
    public void clickSuggestSubmit(View v) {
    	historyFragment = adapter.getHistoryFragment();
    	TurnHistoryItem temp = new TurnHistoryItem(TYPE_SUGGEST);
    	historyFragment.add(temp);
    	
    	pw.dismiss();
    }
    
    public void clickSuggestAccuse(View v) {
    	historyFragment = adapter.getHistoryFragment();
    	TurnHistoryItem temp = new TurnHistoryItem(TYPE_ACCUSE);
    	historyFragment.add(temp);
    	
    	pw.dismiss();
    }
    
    public void clickCancel(View v){
    	pw.dismiss();
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		
		
	}


}

class MyAdapter extends FragmentPagerAdapter{
	ArrayList<TurnHistoryItem> historyItems;
	int pos = 0;
	Fragment fragment = null;
	HistoryFragment historyFragment;
	
	public MyAdapter(FragmentManager fm) {
		super(fm);
		//TODO This is a temp first item
		historyItems = new ArrayList<TurnHistoryItem>();
		historyItems.add(new TurnHistoryItem(0));
	}
	
	@Override
	public Fragment getItem(int i){
		fragment = null;
		if (i == 0){
			fragment = new HistoryFragment(historyItems);
			historyFragment = (HistoryFragment) fragment;
		}
		if (i == 1){
			fragment = new NotesFragment();
		}
		return fragment;
	}
	
	@Override
	public int getCount() {
		return 2;
	}
	
	@Override
	public CharSequence getPageTitle(int position){
		String title = new String();
		if(position == 0){
			return "Turn History";
		}else if (position == 1){
			return "Notes";
		}else
			return null;
	}
	
	public HistoryFragment getHistoryFragment(){
		return historyFragment;
	}

}
