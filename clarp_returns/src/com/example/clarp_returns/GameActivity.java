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
import com.parse.ParseQuery;


public class GameActivity extends ActionBarActivity{
	
	private static final int TYPE_SUGGEST = 0;
    private static final int TYPE_ACCUSE = 1;
    private static final int TYPE_ALERT = 2;
	
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
	View popupView;
	PopupWindow pw;
	ListPopupWindow lpw;
	MyAdapter pageAdapter;
	HistoryFragment historyFragment;
	int page = 0;
	Player selectedPlayer;
	
	GameStates gameState = GameStates.IN_PROGRESS;
	
	Card queuedSuspect = null;
	Card queuedWeapon = null;
	Card queuedScene = null;
	
	Card murderer = null;
	Card murderWeapon = null;
	Card crimeScene = null;
	
	// PARSIFIED VARIABLES
	
	int num_of_players2;
	int num_of_weapons2;
	int num_of_scenes2;
	int total_cards2;
	
	ArrayList<ClarpCard> cards2 = new ArrayList<ClarpCard>();
	Boolean gotCards = false;
	

	ArrayList<Player> players2 = new ArrayList<Player>();
	

	
	ClarpGame game = null;
	
	/*
	 * This enum is currently in its own class... 
	 * can it be moved to ClarpApplication to make it a global enum that doesn't need its own file and class?
	 * Just a thought
	 * -Derky
	 */
	CardTypes selectType = CardTypes.SUSPECT;
	
	

	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        total_cards = num_of_players + num_of_weapons + num_of_scenes;
        
               
        
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
                    // get counts
                    num_of_players2 = game.getInt("numSuspects");
                	num_of_weapons2 = game.getInt("numWeapons");
                	num_of_scenes2 = game.getInt("numLocations");
                	total_cards2 = num_of_players2 + num_of_weapons2 + num_of_scenes2;
                	
                	// populate cards2[]
                	getCards(game);
                	
                	//populate players2[]
                	try {
						getPlayers(game);
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						Log.d(ClarpApplication.TAG, "Ran into an error trying to get the players");
					}
                	
                	// PGA will have already distributed the facts to each player.
                	
                	
                	
                	
                    
                }
                else
                {
                    Log.d(ClarpApplication.TAG, "Something went wrong when querying the ClarpGame in GA");
                }
            }
        });
        
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
			cards.add(new Card(scenes.get(i),resID,CardTypes.LOCATION));
		}
		Collections.shuffle(cards);
	}
	
	public void dealCards(){
		
		int player = 0;
		for (int i = 0; i < cards.size(); ++i){
			if (murderer == null && cards.get(i).type == CardTypes.SUSPECT){
				murderer = cards.get(i);
				continue;
			}
			if (murderWeapon == null && cards.get(i).type == CardTypes.WEAPON){
				murderWeapon = cards.get(i);
				continue;
			}
			if (crimeScene == null && cards.get(i).type == CardTypes.LOCATION){
				crimeScene = cards.get(i);
				continue;
			}
			players.get(player++).cards.add(cards.get(i));
			if (player == num_of_players) player = 0;
		}
	}
	
	private void getCards(ClarpGame g)
	{
		cards2 = new ArrayList<ClarpCard>();
		
		String id = g.getObjectId();
		
		ParseQuery<ClarpCard> query = ParseQuery.getQuery("ClarpCard");
        query.whereEqualTo("gameId", id);
        
        query.findInBackground(new FindCallback<ClarpCard>() {
            public void done(List<ClarpCard> cList, ParseException e) {
                if (e == null) {
                    
                    for (int i = 0; i < cList.size(); ++i)
                    {
                    	cards2.add(cList.get(i));
                    	Log.d(ClarpApplication.TAG, "grabbed card " + cards2.get(i).getCardName());
                    }
                    
                    gotCards = true;
                    
                } else {
                	Log.d(ClarpApplication.TAG, "query failure (?)");
                }
            }
        });
	}
	
	private void getPlayers(ClarpGame g) throws JSONException
	{
		JSONArray gPlayers = g.getJSONArray("players");
		
		for (int i = 0; i < gPlayers.length(); ++i)
		{
			players2.add(new Player(gPlayers.getJSONObject(i)));
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
		
		
		for (Card c : cards){
			if (queuedSuspect == null && c.type == CardTypes.SUSPECT)
				queuedSuspect = c;
			if (queuedWeapon == null && c.type == CardTypes.WEAPON)
				queuedWeapon = c;
			if (queuedScene == null && c.type == CardTypes.LOCATION)
				queuedScene = c;
			if (queuedSuspect != null && queuedWeapon != null && queuedScene != null)
				break;
		}
		
		ImageView suspectPic = (ImageView) popupView.findViewById(R.id.imageSuspectSelect);
		suspectPic.setImageResource(queuedSuspect.pic);
		ImageView weaponPic = (ImageView) popupView.findViewById(R.id.imageWeaponSelect);
		weaponPic.setImageResource(queuedWeapon.pic);
		ImageView scenePic = (ImageView) popupView.findViewById(R.id.imageSceneSelect);
		scenePic.setImageResource(queuedScene.pic);
		
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
    	selectType = CardTypes.SUSPECT;
    	ArrayList<Card> suspectList = new ArrayList<Card>();
		for (Card c : cards){
			if (c.type == CardTypes.SUSPECT)
				suspectList.add(c);
		}
    	final ListView listCards = (ListView) popupView.findViewById(R.id.listCards);
		final CardAdapter adapter = new CardAdapter(GameActivity.this, R.layout.select_item, suspectList);
		listCards.setAdapter(adapter);
		listCards.setClickable(true);
		listCards.setOnItemClickListener(new OnItemClickListener() {

			 @Override
			 public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

				 queuedSuspect = (Card) listCards.getItemAtPosition(position);
				 ImageView suspectPic = (ImageView) popupView.findViewById(R.id.imageSuspectSelect);
				 suspectPic.setImageResource(queuedSuspect.pic);
			 }
		});
	}
    
  //Called when the Weapon image is clicked in a Suggest or Accuse popup.  Will populate the listview with Weapon cards.
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void clickSelectWeapon(View v) {
    	//TODO current work
    	selectType = CardTypes.WEAPON;
    	ArrayList<Card> weaponList = new ArrayList<Card>();
		for (Card c : cards){
			if (c.type == CardTypes.WEAPON)
				weaponList.add(c);
		}
		final ListView listCards = (ListView) popupView.findViewById(R.id.listCards);
		final CardAdapter adapter = new CardAdapter(GameActivity.this, R.layout.select_item, weaponList);
		listCards.setAdapter(adapter);
		listCards.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			 @Override
			 public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

				 queuedWeapon = (Card) listCards.getItemAtPosition(position);
				 ImageView weaponPic = (ImageView) popupView.findViewById(R.id.imageWeaponSelect);
				 weaponPic.setImageResource(queuedWeapon.pic);
			 }
		});
	}
    
    
  //Called when the Scene image is clicked in a Suggest or accuse popup.  Will populate the listview with Scene cards.
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void clickSelectScene(View v) {
    	//TODO current work
    	selectType = CardTypes.LOCATION;
    	ArrayList<Card> sceneList = new ArrayList<Card>();
		for (Card c : cards){
			if (c.type == CardTypes.LOCATION)
				sceneList.add(c);
		}
    	final ListView listCards = (ListView) popupView.findViewById(R.id.listCards);
		final CardAdapter adapter = new CardAdapter(GameActivity.this, R.layout.select_item, sceneList);
		listCards.setAdapter(adapter);
		listCards.setOnItemClickListener(new OnItemClickListener() {

			 @Override
			 public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

				 queuedScene = (Card) listCards.getItemAtPosition(position);
				 ImageView scenePic = (ImageView) popupView.findViewById(R.id.imageSceneSelect);
				 scenePic.setImageResource(queuedScene.pic);
			 }
		});
	}
    
    public void clickSuggestSubmit(View v) {
    	submit(TYPE_SUGGEST);
    }
    
    public void clickSuggestAccuse(View v) {
    	submit(TYPE_ACCUSE);
    }
    
    
    private void submit(int type){
    	historyFragment = pageAdapter.getHistoryFragment();
    	TurnHistoryItem temp = new TurnHistoryItem(type);
    	temp.person = queuedSuspect.pic;
    	temp.weapon = queuedWeapon.pic;
    	temp.location = queuedScene.pic;
    	
    	String disprover = refuteSuggestion();
    	
    	temp.result = disprover + " proved " + players.get(0).name + " wrong";
    	
    	historyFragment.add(temp);
    	
    	pw.dismiss();
    	
    	if (type == TYPE_ACCUSE){
    		if (queuedSuspect.equals(murderer) && queuedWeapon.equals(murderWeapon) 
    				&& queuedScene.equals(crimeScene)){
    			gameState = GameStates.WON;
    			temp = new TurnHistoryItem(TYPE_ALERT);
    			temp.result = players.get(0).name + " has solved the mystery!";  	
    			historyFragment.add(temp);
    		}else{
    			gameState = GameStates.LOST;
    			temp = new TurnHistoryItem(TYPE_ALERT);
    			temp.result = players.get(0).name + " was Dead Wrong!";  	
    			historyFragment.add(temp);
    		}
    	}
    		
    		
    	queuedSuspect = null;
    	queuedWeapon = null;
    	queuedScene = null;
    }
    
    
    
    public String refuteSuggestion(){
    	for (Player p : players.subList(1, players.size())){
    		for (Card c : p.cards){
    			if (c.equals(queuedSuspect) || c.equals(queuedWeapon) || c.equals(queuedScene))
    				return p.name;
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
