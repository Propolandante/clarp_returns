package com.example.clarp_returns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.widget.ListPopupWindow;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseQuery;


public class GameActivity extends ActionBarActivity{
	
	private static final int TYPE_SUGGEST = 0;
    private static final int TYPE_ACCUSE = 1;
    private static final int TYPE_ALERT = 2;
	
	ViewPager viewPager = null;
	int cur_player = 0;
	View popupView;
	PopupWindow pw;
	ListPopupWindow lpw;
	MyAdapter pageAdapter;
	HistoryFragment historyFragment;
	int page = 0;
	
	GameStates gameState = GameStates.IN_PROGRESS;
	
	// PARSIFIED VARIABLES
	
	int num_of_players;
	int num_of_weapons;
	int num_of_scenes;
	int total_cards;
	
	ArrayList<ClarpCard> cards = new ArrayList<ClarpCard>();
	Boolean gotCards = false;

	ArrayList<Player> players = new ArrayList<Player>();
	
	ClarpCard queuedSuspect = null;
	ClarpCard queuedWeapon = null;
	ClarpCard queuedScene = null;
	
	ClarpCard.CardType selectType = ClarpCard.CardType.SUSPECT;
	
	ClarpGame game = null;
	
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        /*
         * First, get the ClarpGame so we know what we're working with
         */
        
        Intent mainIntent = getIntent();
        ParseQuery<ClarpGame> query = ParseQuery.getQuery("ClarpGame");
        query.getInBackground(mainIntent.getStringExtra("game_id"), new GetCallback<ClarpGame>() {
            @Override
            public void done(ClarpGame object, ParseException e) {
                if (e == null)
                {
                    game = object;
                    // get counts. idk if we will use these, but Joe might know
                    num_of_players = game.getInt("numSuspects");
                	num_of_weapons = game.getInt("numWeapons");
                	num_of_scenes = game.getInt("numLocations");
                	total_cards = num_of_players + num_of_weapons + num_of_scenes;
                	
                	/*
                	 * These two functions run in the background.
                	 * We should show a loading bar while they process
                	 * So the user can't attempt to play before the info is here
                	 */
                	getCards(game);
                	
                	getPlayers(game);
                	
                	// PGA will have already distributed the facts to each player.
                	
                }
                else
                {
                    Log.d(ClarpApplication.TAG, "Something went wrong when querying the ClarpGame in GA");
                }
            }
        });
        
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	
    	viewPager = (ViewPager) findViewById(R.id.pager);
		FragmentManager fragmentManager = getSupportFragmentManager();
		pageAdapter = new MyAdapter(fragmentManager);
        viewPager.setAdapter(pageAdapter);

    	
    	
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

    
	
	private void getCards(ClarpGame g)
	{
		
		/*
		 * Since the cards hold the game ID and not the other way around,
		 * we need to go find all the cards and add them to a local array
		 */
		
		cards = new ArrayList<ClarpCard>();
		String id = g.getObjectId();
		
		ParseQuery<ClarpCard> query = ParseQuery.getQuery("ClarpCard");
        query.whereEqualTo("gameId", id);
        
        query.findInBackground(new FindCallback<ClarpCard>() {
            public void done(List<ClarpCard> cList, ParseException e) {
                if (e == null) {
                    
                    for (int i = 0; i < cList.size(); ++i)
                    {
                    	cards.add(cList.get(i));
                    	Log.d(ClarpApplication.TAG, "grabbed card " + cards.get(i).getCardName());
                    }
                    
                    // shuffle it, so the first 3 cards aren't the solution...
                    
                    Collections.shuffle(cards);
                    
                    // use this to update views and hide the loading bar (once we implement that)
                    gotCards = true;
                    
                } else {
                	Log.d(ClarpApplication.TAG, "query failure (?)");
                }
            }
        });
	}
	
	private void getPlayers(ClarpGame g)
	{
		
		/*
		 * The players array in ClarpGame is not very accessible
		 * and we can't use getters and setters with it.
		 * To remedy this, we're copying the players to our local array
		 * where we can better access the data.
		 */
		
		JSONArray gPlayers = g.getJSONArray("players");
		
		
		for (int i = 0; i < gPlayers.length(); ++i)
		{
			try {
				players.add(new Player(gPlayers.getJSONObject(i)));
				Log.d(ClarpApplication.TAG, "Added player " + players.get(i).getFullName());
			} catch (JSONException e) {
				Log.d(ClarpApplication.TAG, "Failed to add player");
				e.printStackTrace();
			}
			
		}
	}
    
    //clickSuggest is called when the Suggest button is pressed in GameActivity.  It creates a popup window that
	//contains 3 clickable imageviews and initalizes them to the first of each type of card found in the deck.
    public void clickSuggest(View v) {
    	if (gameState == GameStates.IN_PROGRESS)
    		createPopup(true);
		
	}
    
    //Bassically the same as clickSuggest, but a different layout is inflated.  Maybe later I can have each click divert
    //to the same method.
    public void clickAccuse(View v) {
    	if (gameState == GameStates.IN_PROGRESS)
    		createPopup(false);
	}
    
    
    private void createPopup(boolean isSuggest){
    	LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
    	
    	if (isSuggest)
    		popupView = inflater.inflate(R.layout.popup_suggest,null);
    	else
    		popupView = inflater.inflate(R.layout.popup_accuse,null);
		
		popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		
		/*
		 * Grab the first card of each type so we can put something as the default suggestion.
		 * Maybe we should use placeholder images instead? it seems weird to preload a suggestion...
		 */
		
		for (ClarpCard c : cards){
			if (queuedSuspect == null && c.getCardType() == ClarpCard.CardType.SUSPECT)
				queuedSuspect = c;
			if (queuedWeapon == null && c.getCardType() == ClarpCard.CardType.WEAPON)
				queuedWeapon = c;
			if (queuedScene == null && c.getCardType() == ClarpCard.CardType.LOCATION)
				queuedScene = c;
			if (queuedSuspect != null && queuedWeapon != null && queuedScene != null)
				break;
		}
		
		ParseFile suspectImageFile = queuedSuspect.getPhotoFile();
		ParseFile weaponImageFile = queuedWeapon.getPhotoFile();
		ParseFile locationImageFile = queuedScene.getPhotoFile();
		
		ParseImageView suspectPic = (ParseImageView) popupView.findViewById(R.id.imageSuspectSelect);
		ParseImageView weaponPic = (ParseImageView) popupView.findViewById(R.id.imageWeaponSelect);
		ParseImageView scenePic = (ParseImageView) popupView.findViewById(R.id.imageSceneSelect);
		
		if (suspectImageFile != null) {
			suspectPic.setParseFile(suspectImageFile);
			suspectPic.loadInBackground();
			Log.d(ClarpApplication.TAG, "We don't have any suspects...?");
        }
		
		if (weaponImageFile != null) {
			weaponPic.setParseFile(weaponImageFile);
			weaponPic.loadInBackground();
			Log.d(ClarpApplication.TAG, "We don't have any weapons...?");
        }
		
		if (locationImageFile != null) {
			scenePic.setParseFile(locationImageFile);
			scenePic.loadInBackground();
			Log.d(ClarpApplication.TAG, "We don't have any locations...?");
        }
		
		
		pw = new PopupWindow(popupView,popupView.getMeasuredWidth(),popupView.getMeasuredHeight(),true);
		pw.setAnimationStyle(android.R.style.Animation_Dialog);
		pw.showAtLocation(findViewById(R.id.layoutGame), Gravity.CENTER,0,0);
		pw.setOutsideTouchable(true);
		pw.setBackgroundDrawable(getResources().getDrawable(android.R.color.white));
		pw.setFocusable(true);
    }
    
    
    //Called when the Suspect image is clicked in a Suggest or accuse popup.  Will populate the listview with Suspect cards.
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void clickSelectSuspect(View v) {
    	selectType = ClarpCard.CardType.SUSPECT;
    	ArrayList<ClarpCard> suspectList = new ArrayList<ClarpCard>();
		for (ClarpCard c : cards){
			if (c.getCardType() == selectType)
				suspectList.add(c);
		}
    	final ListView listCards = (ListView) popupView.findViewById(R.id.listCards);
		final CardAdapter adapter = new CardAdapter(GameActivity.this, R.layout.select_item, suspectList);
		listCards.setAdapter(adapter);
		listCards.setClickable(true);
		listCards.setOnItemClickListener(new OnItemClickListener() {

			 @Override
			 public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

				 queuedSuspect = (ClarpCard) listCards.getItemAtPosition(position);
				 ParseFile suspectImageFile = queuedSuspect.getPhotoFile();
				 ParseImageView suspectPic = (ParseImageView) popupView.findViewById(R.id.imageSuspectSelect);
				 if (suspectImageFile != null) {
						suspectPic.setParseFile(suspectImageFile);
						suspectPic.loadInBackground();
			        }
			 }
		});
	}
    
  //Called when the Weapon image is clicked in a Suggest or Accuse popup.  Will populate the listview with Weapon cards.
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void clickSelectWeapon(View v) {
    	//TODO current work
    	selectType = ClarpCard.CardType.WEAPON;
    	ArrayList<ClarpCard> weaponList = new ArrayList<ClarpCard>();
		for (ClarpCard c : cards){
			if (c.getCardType() == selectType)
				weaponList.add(c);
		}
		final ListView listCards = (ListView) popupView.findViewById(R.id.listCards);
		final CardAdapter adapter = new CardAdapter(GameActivity.this, R.layout.select_item, weaponList);
		listCards.setAdapter(adapter);
		listCards.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			 @Override
			 public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

				 queuedWeapon = (ClarpCard) listCards.getItemAtPosition(position);
				 ParseFile weaponImageFile = queuedWeapon.getPhotoFile();
				 ParseImageView weaponPic = (ParseImageView) popupView.findViewById(R.id.imageWeaponSelect);
				 if (weaponImageFile != null) {
						weaponPic.setParseFile(weaponImageFile);
						weaponPic.loadInBackground();
				 }
			 }
		});
	}
    
    
  //Called when the Scene image is clicked in a Suggest or accuse popup.  Will populate the listview with Scene cards.
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void clickSelectScene(View v) {
    	//TODO current work
    	selectType = ClarpCard.CardType.LOCATION;
    	ArrayList<ClarpCard> sceneList = new ArrayList<ClarpCard>();
		for (ClarpCard c : cards){
			if (c.getCardType() == selectType)
				sceneList.add(c);
		}
    	final ListView listCards = (ListView) popupView.findViewById(R.id.listCards);
		final CardAdapter adapter = new CardAdapter(GameActivity.this, R.layout.select_item, sceneList);
		listCards.setAdapter(adapter);
		listCards.setOnItemClickListener(new OnItemClickListener() {

			 @Override
			 public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

				 queuedScene = (ClarpCard) listCards.getItemAtPosition(position);
				 ParseFile sceneImageFile = queuedScene.getPhotoFile();
				 ParseImageView scenePic = (ParseImageView) popupView.findViewById(R.id.imageSceneSelect);
				 if (sceneImageFile != null) {
					 scenePic.setParseFile(sceneImageFile);
					 scenePic.loadInBackground();
				 }
			 }
		});
	}
    
    public void clickSuggestSubmit(View v) throws JSONException {
    	submit(TYPE_SUGGEST);
    }
    
    public void clickSuggestAccuse(View v) throws JSONException {
    	submit(TYPE_ACCUSE);
    }
    
    
    private void submit(int type) throws JSONException{
    	historyFragment = pageAdapter.getHistoryFragment();
    	TurnHistoryItem temp = new TurnHistoryItem(type, queuedSuspect, queuedWeapon, queuedScene);
    	
    	String disprover = refuteSuggestion();
    	
    	temp.setResult(disprover);
    	
    	historyFragment.add(temp);
    	
    	pw.dismiss();
    	
    	if (type == TYPE_ACCUSE){
    		// if the accusation matches the solution...
    		if (queuedSuspect.getObjectId().equals(game.getJSONArray("solution").get(0)) 
    				&& queuedWeapon.getObjectId().equals(game.getJSONArray("solution").get(1)) 
    				&& queuedScene.getObjectId().equals(game.getJSONArray("solution").get(2))){
    			// the player has won!
    			gameState = GameStates.WON;
    			temp = new TurnHistoryItem(TYPE_ALERT);
    			temp.result = players.get(0).getFullName()+ " has solved the mystery!";  	
    			historyFragment.add(temp);
    		}else{
    			// the player has lost! they need to be disqualified...
    			gameState = GameStates.LOST;
    			temp = new TurnHistoryItem(TYPE_ALERT);
    			temp.result = players.get(0).getFullName() + " was Dead Wrong!";  	
    			historyFragment.add(temp);
    		}
    	}
    		
    		
    	queuedSuspect = null;
    	queuedWeapon = null;
    	queuedScene = null;
    }
    
    
    
    public String refuteSuggestion(){
    	
    	if (players.size() > 1)
    	{
    		for (Player p : players.subList(1, players.size())){
        		for (String id : p.getCardIds()){
        			if (id.equals(queuedSuspect.getObjectId()) || id.equals(queuedWeapon.getObjectId()) || id.equals(queuedScene.getObjectId()))
        				return p.getFullName();
        		}
        	}
    	}
    	
    	return "No one";
    }
    
    public void clickCancel(View v){
    	pw.dismiss();
    }
    
    
    public enum GameStates{
    	IN_PROGRESS,WON,LOST;
    }



}

class MyAdapter extends FragmentPagerAdapter{
	ArrayList<TurnHistoryItem> historyItems;
	int pos = 0;
	Fragment fragment = null;
	HistoryFragment historyFragment;
	
	public MyAdapter(FragmentManager fm) {
		super(fm);
		
		historyItems = new ArrayList<TurnHistoryItem>();
		TurnHistoryItem firstItem = new TurnHistoryItem(2);
		firstItem.result = "The Game is Afoot!";  	
    	historyItems.add(firstItem);
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
