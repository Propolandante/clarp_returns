package com.example.clarp_returns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseUser;


public class GameActivity extends ActionBarActivity{

    public static final int TYPE_SUGGEST = 0;
    public static final int TYPE_ACCUSE = 1;
    public static final int TYPE_ALERT = 2;

    ListView historyListView;
    Button suggestButton;
    Button accuseButton;
    TextView whoseTurnText;

    ViewPager viewPager = null;
    int cur_player = 0;
    View popupView;
    PopupWindow pw;
    ListPopupWindow lpw;
    MyAdapter pageAdapter;
    HistoryFragment historyFragment;
    NotesFragment notesFragment;
    CardHandFragment cardHandFragment;
    int page = 0;

    GameStates gameState = GameStates.IN_PROGRESS;

    // PARSIFIED VARIABLES

    int num_of_players;
    int num_of_weapons;
    int num_of_scenes;
    int total_cards;

    ArrayList<ClarpCard> cards = new ArrayList<ClarpCard>();
    Boolean loading = true;

    ArrayList<Player> players = new ArrayList<Player>();

    Boolean isMyTurn = true;

    ClarpCard queuedSuspect = null;
    ClarpCard queuedWeapon = null;
    ClarpCard queuedScene = null;

    ClarpCard.CardType selectType = ClarpCard.CardType.SUSPECT;

    ClarpGame game = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        historyListView = (ListView) findViewById(R.id.listViewHistory);
        if (historyListView == null)
        {
            Log.d(ClarpApplication.GA, "could not find historyListView");
        }
        suggestButton = (Button) findViewById(R.id.buttonSuggest);
        if (suggestButton == null)
        {
            Log.d(ClarpApplication.GA, "could not find suggestButton");
        }
        accuseButton = (Button) findViewById(R.id.buttonAccuse);
        if (accuseButton == null)
        {
            Log.d(ClarpApplication.GA, "could not find accuseButton");
        }
        whoseTurnText = (TextView) findViewById(R.id.textViewWhoseTurn);
        if (whoseTurnText == null)
        {
            Log.d(ClarpApplication.GA, "could not find whoseTurnText");
        }

        viewPager = (ViewPager) findViewById(R.id.pager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        pageAdapter = new MyAdapter(fragmentManager);
        viewPager.setAdapter(pageAdapter);
        viewPager.setCurrentItem(1);
        
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
                    getPlayers(game);

                    getCards(game);

                    // listView is updated once cards have been grabbed




                }
                else
                {
                    Log.d(ClarpApplication.GA, "Something went wrong when querying the ClarpGame in GA");
                }
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();

        Log.d(ClarpApplication.GA, "onResume() called.");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d(ClarpApplication.GA, "onPause() called.");
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.d(ClarpApplication.GA, "onStop() called.");
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

        loading = true;
        updateViews();

        query.findInBackground(new FindCallback<ClarpCard>() {
            @Override
            public void done(List<ClarpCard> cList, ParseException e) {
                if (e == null) {
                    ArrayList<String> suspects = new ArrayList<String>();
                    ArrayList<String> weapons = new ArrayList<String>();
                    ArrayList<String> scenes = new ArrayList<String>();

                    for (int i = 0; i < cList.size(); ++i)
                    {
                        cards.add(cList.get(i));
                        if (cards.get(i).getCardType() == ClarpCard.CardType.SUSPECT){
                            suspects.add(cards.get(i).getCardName());
                        }else if (cards.get(i).getCardType() == ClarpCard.CardType.WEAPON){
                            weapons.add(cards.get(i).getCardName());
                        }else{
                            scenes.add(cards.get(i).getCardName());
                        }
                        Log.d(ClarpApplication.GA, "grabbed card " + cards.get(i).getCardName());
                    }

                    // put the card names into the notes fragment
                    notesFragment = pageAdapter.getNotesFragment();
                    notesFragment.add(new NoteItem("Suspects",1));
                    for(String suspect : suspects){
                        notesFragment.add(new NoteItem(suspect,0));
                    }
                    notesFragment.add(new NoteItem("Weapons",1));
                    for(String weapon : weapons){
                        notesFragment.add(new NoteItem(weapon,0));
                    }
                    notesFragment.add(new NoteItem("Scenes",1));
                    for(String scene : scenes){
                        notesFragment.add(new NoteItem(scene,0));
                    }
                    Log.d("going to iterate", "yeah");
                    cardHandFragment = pageAdapter.getCardHandFragment();
                    ParseUser thisPlayer = ParseUser.getCurrentUser();
                    for (Player player : players){
                    	Log.d("iterating", player.name);
                    	try {
                    		Log.d("found", player.getFbId());
                    		Log.d("found", thisPlayer.getJSONObject("profile").getString("facebookId"));
                    		boolean check = player.getFbId().equals(thisPlayer.getJSONObject("profile").getString("facebookId"));
                    		Log.d("check", Boolean.toString(check));
							if (check){
								Log.d("found", player.name);
								for(String playerCard : player.getCardIds()){
									Log.d("iterating", playerCard);
									for (ClarpCard card : cards){
										Log.d("iterating", card.getCardName());
										if (card.getObjectId().equals(playerCard)){
											cardHandFragment.add(card);
											Log.d("card", card.getCardName());
										}
											
									}
								}
							}
							
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
                    }
                    // shuffle it, so the first 3 cards aren't the solution...

                    Collections.shuffle(cards);

                    // use this to update views and hide the loading bar (once we implement that)


                    /*
                     * Refresh the history
                     * This can only be done once the cards have been grabbed
                     */
                    try {
                        refreshHistory();
                    } catch (JSONException e1) {
                        Log.d(ClarpApplication.GA, "JSON error in onCreate");
                        e1.printStackTrace();
                    }

                } else {
                    Log.d(ClarpApplication.GA, "query failure (?)");
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
                Log.d(ClarpApplication.GA, "Added player " + players.get(i).getFullName());
            } catch (JSONException e) {
                Log.d(ClarpApplication.GA, "Failed to add player");
                e.printStackTrace();
            }

        }
    }

    //clickSuggest is called when the Suggest button is pressed in GameActivity.  It creates a popup window that
    //contains 3 clickable imageviews and initalizes them to the first of each type of card found in the deck.
    public void clickSuggest(View v) {
        if (gameState == GameStates.IN_PROGRESS) {
            createPopup(true);
        }

    }

    //Bassically the same as clickSuggest, but a different layout is inflated.  Maybe later I can have each click divert
    //to the same method.
    public void clickAccuse(View v) {
        if (gameState == GameStates.IN_PROGRESS) {
            createPopup(false);
        }
    }


    private void createPopup(boolean isSuggest){
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        if (isSuggest) {
            popupView = inflater.inflate(R.layout.popup_suggest,null);
        } else {
            popupView = inflater.inflate(R.layout.popup_accuse,null);
        }

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        /*
         * Grab the first card of each type so we can put something as the default suggestion.
         * Maybe we should use placeholder images instead? it seems weird to preload a suggestion...
         */

        for (ClarpCard c : cards){
            if ((queuedSuspect == null) && (c.getCardType() == ClarpCard.CardType.SUSPECT)) {
                queuedSuspect = c;
            }
            if ((queuedWeapon == null) && (c.getCardType() == ClarpCard.CardType.WEAPON)) {
                queuedWeapon = c;
            }
            if ((queuedScene == null) && (c.getCardType() == ClarpCard.CardType.LOCATION)) {
                queuedScene = c;
            }
            if ((queuedSuspect != null) && (queuedWeapon != null) && (queuedScene != null)) {
                break;
            }
        }

        /*
         * this will return null if the game doesn't have sufficient pictures
         */

        ParseFile suspectImageFile = queuedSuspect.getPhotoFile();
        ParseFile weaponImageFile = queuedWeapon.getPhotoFile();
        ParseFile locationImageFile = queuedScene.getPhotoFile();

        ParseImageView suspectPic = (ParseImageView) popupView.findViewById(R.id.imageSuspectSelect);
        ParseImageView weaponPic = (ParseImageView) popupView.findViewById(R.id.imageWeaponSelect);
        ParseImageView scenePic = (ParseImageView) popupView.findViewById(R.id.imageSceneSelect);

        if (suspectImageFile != null) {
            suspectPic.setParseFile(suspectImageFile);
            suspectPic.loadInBackground();

        }
        else
        {
            Log.d(ClarpApplication.GA, "no suspect image");
        }

        if (weaponImageFile != null) {
            weaponPic.setParseFile(weaponImageFile);
            weaponPic.loadInBackground();

        }
        else
        {
            Log.d(ClarpApplication.GA, "no weapon image");
        }

        if (locationImageFile != null) {
            scenePic.setParseFile(locationImageFile);
            scenePic.loadInBackground();

        }
        else
        {
            Log.d(ClarpApplication.GA, "no location image");
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
            if (c.getCardType() == selectType) {
                suspectList.add(c);
            }
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
        selectType = ClarpCard.CardType.WEAPON;
        ArrayList<ClarpCard> weaponList = new ArrayList<ClarpCard>();
        for (ClarpCard c : cards){
            if (c.getCardType() == selectType) {
                weaponList.add(c);
            }
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
        selectType = ClarpCard.CardType.LOCATION;
        ArrayList<ClarpCard> sceneList = new ArrayList<ClarpCard>();
        for (ClarpCard c : cards){
            if (c.getCardType() == selectType) {
                sceneList.add(c);
            }
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

        /*
         * Because this function can only be called if it is the current user's turn,
         * and no one else can add anything to turns[] when it is not their turn,
         * we can safely assume the current user's turns[] array is fully up-to-date.
         * Therefore, they should create a new JSONOBject clarpTurn,
         * and push it to their local ClarpGame,
         * and then populate their local TurnHistory array WHILE saving the turn to the cloud
         */
        historyFragment = pageAdapter.getHistoryFragment();
        Player currentPlayer = null;
        String curPlayerId = game.getString("whoseTurn");
        for (Player p : players)
        {
            if (p.getFbId().equals(curPlayerId))
            {
                currentPlayer = p;
            }
        }
        if (currentPlayer == null)
        {
            Log.d(ClarpApplication.GA, "currentPlayer is null in submit!!!!!!!!");
        }
        
        // helpful debug output
        if(type == TYPE_SUGGEST)
        {
        	Log.d(ClarpApplication.GA, "My suggestion: " + queuedSuspect.getCardName() + ", " + queuedWeapon.getCardName() + ", " + queuedScene.getCardName());
        }
        else if(type == TYPE_ACCUSE)
        {
        	Log.d(ClarpApplication.GA, "My accusation: " + queuedSuspect.getCardName() + ", " + queuedWeapon.getCardName() + ", " + queuedScene.getCardName());
        }

        JSONObject clarpTurn = createClarpTurn(type, currentPlayer, queuedSuspect, queuedWeapon, queuedScene);
        game.getJSONArray("turns").put(clarpTurn);

        /*
         * it's OK for this one to be null
         * but this will be deleted for Parsification anyways
         * since the functionality has been moved to createClarpCard
         */

        TurnHistoryItem temp = createTurnItem(clarpTurn);

        historyFragment.add(temp);

        pw.dismiss();

        if (type == TYPE_ACCUSE){
            // if the accusation matches the solution...
            if (queuedSuspect.getObjectId().equals(game.getJSONArray("solution").get(0))
                    && queuedWeapon.getObjectId().equals(game.getJSONArray("solution").get(1))
                    && queuedScene.getObjectId().equals(game.getJSONArray("solution").get(2))){
                // the player has won!
                JSONObject clarpAlert = createClarpAlert(TYPE_ALERT, "The mystery is solved!");
                game.getJSONArray("turns").put(clarpAlert);
                gameState = GameStates.WON;
                game.end();
            }else{
                // the player has lost!
                // TODO they need to be disqualified... keep in mind our player[] works differently than Parse's
                JSONObject clarpAlert = createClarpAlert(TYPE_ALERT, "That accusation was dead wrong!");
                game.getJSONArray("turns").put(clarpAlert);
                gameState = GameStates.DISQUALIFIED;
            }
        }

        refreshHistory();

        game.rotateTurn();


        game.saveInBackground();

        queuedSuspect = null;
        queuedWeapon = null;
        queuedScene = null;
    }

    private JSONObject createClarpTurn( int type, Player player, ClarpCard suspect, ClarpCard weapon, ClarpCard location) throws JSONException{

        if((type != TYPE_SUGGEST) && (type != TYPE_ACCUSE))
        {
            Log.d(ClarpApplication.GA, "INCORRECT USE OF createClarpCard!");
        }

        JSONObject turn = new JSONObject();

        Player alibi = null;
        String alibiCardId = null;

        /*
         * Determine if there is an alibi, and which card they are ruling out
         */

        // loop through all the players
        for (Player p : players){
            // exclude the current player
            if(!p.equals(player) && alibi == null)
            {
                // check all of each player's cards
                for (String id : p.getCardIds()){
                    // if they have the same suspect card
                    if (id.equals(queuedSuspect.getObjectId()))
                    {
                        // then they are ruling out this suspect
                        alibi = p;
                        alibiCardId = queuedSuspect.getObjectId();
                        turn.put("alibiFbId", alibi.getFbId());
                        turn.put("alibiCardId", alibiCardId);
                        Log.d(ClarpApplication.GA, alibi.getName() + " is ruling out" + alibiCardId);
                        break;
                    }
                    // if they have the same weapon card
                    else if (id.equals(queuedWeapon.getObjectId()))
                    {
                        // then they are ruling out this weapon
                        alibi = p;
                        alibiCardId = queuedWeapon.getObjectId();
                        turn.put("alibiFbId", alibi.getFbId());
                        turn.put("alibiCardId", alibiCardId);
                        Log.d(ClarpApplication.GA, alibi.getName() + " is ruling out" + alibiCardId);
                        break;
                    }
                    // if they have the same location card
                    else if (id.equals(queuedScene.getObjectId()))
                    {
                        // then they are ruling out this location
                        alibi = p;
                        alibiCardId = queuedScene.getObjectId();
                        turn.put("alibiFbId", alibi.getFbId());
                        turn.put("alibiCardId", alibiCardId);
                        Log.d(ClarpApplication.GA, alibi.getName() + " is ruling out" + alibiCardId);
                        break;

                    }
                }
            }
        }
        // if no alibi, then put null
        if(alibi == null)
        {
            turn.put("alibiFbId", null);
            turn.put("alibiCardId", null);
            Log.d(ClarpApplication.GA, "No one is ruling out this suggestion/accusation");
        }


        turn.put("type", type);
        turn.put("playerFbId", player.getFbId());
        turn.put("suspectId", suspect.getObjectId());
        turn.put("weaponId", weapon.getObjectId());
        turn.put("locationId", location.getObjectId());
        turn.put("alertText", null);

        return turn;
    }

    private JSONObject createClarpAlert( int type, String alertText ) throws JSONException {

        if(type != TYPE_ALERT)
        {
            Log.d(ClarpApplication.GA, "INCORRECT USE OF createClarpAlert!");
        }

        JSONObject alert = new JSONObject();

        alert.put("type", type);
        alert.put("alertText", alertText);

        // idk if it's necessary to set these as null, but why not?
        alert.put("playerFbId", null);
        alert.put("suspectId", null);
        alert.put("weaponId", null);
        alert.put("locationId", null);
        alert.put("alibiFbId", null);
        alert.put("alibiCardId", null);

        return alert;

    }

    TurnHistoryItem createTurnItem( JSONObject clarpTurn) throws JSONException{

        TurnHistoryItem turn = null;

        int type = clarpTurn.getInt("type");

        if((type == TYPE_SUGGEST) || (type == TYPE_ACCUSE))
        {
            Log.d(ClarpApplication.GA, "Creating a standard turn history item");

            String playerFbId = clarpTurn.getString("playerFbId");
            String suspectId = clarpTurn.getString("suspectId");
            String weaponId = clarpTurn.getString("weaponId");
            String locationId = clarpTurn.getString("locationId");
            String alibiFbId = null;
            String alibiCardId = null;
            if (clarpTurn.has("alibiFbId"))
            {
                alibiFbId = clarpTurn.getString("alibiFbId");
            }
            else {Log.d(ClarpApplication.GA, "No alibi");}
            if (clarpTurn.has("alibiCardId"))
            {
                alibiCardId = clarpTurn.getString("alibiCardId");
            }
            else {Log.d(ClarpApplication.GA, "No alibi card");}

            Player player = null;
            ClarpCard suspect = null;
            ClarpCard weapon = null;
            ClarpCard location = null;
            Player alibi = null;
            ClarpCard alibiCard = null;

            // loop through all the players to find player and alibi (if applicable)
            for (Player p : players){

                if (p.getFbId().equals(playerFbId))
                {
                    player = p;
                    
                }

                if (p.getFbId().equals(alibiFbId))
                {
                    alibi = p;
                }

            }
            // make sure we found the player
            if (player == null)
            {
                Log.d(ClarpApplication.GA, "player is null in createTurnItem!!!!!!!");
            }
            // alibi should NEVER be the same as player
            if (player.getFbId().equals(alibiFbId))
            {
            	Log.d(ClarpApplication.GA, "player is equal to alibi? Major porblem here...");
            }
            // loop through all the cards to find the suspect, weapon, and location
            for (ClarpCard c : cards)
            {
                if (c.getObjectId().equals(suspectId))
                {
                    suspect = c;
                }
                if (c.getObjectId().equals(weaponId))
                {
                    weapon = c;
                }
                if (c.getObjectId().equals(locationId))
                {
                    location = c;
                }
                if (c.getObjectId().equals(alibiCardId))
                {
                    alibiCard = c;
                }

                // we can stop looping once we've found all three cards
                // if there is an alibi card, it will have been found already
                if ((suspect != null) && (weapon != null) && (location != null))
                {
                    break;
                }
            }
            // make sure we found the player
            if (suspect == null)
            {
                Log.d(ClarpApplication.GA, "suspect is null in createTurnItem!!!!!!!");
            }
            // make sure we found the player
            if (weapon == null)
            {
                Log.d(ClarpApplication.GA, "weapon is null in createTurnItem!!!!!!!");
            }
            // make sure we found the player
            if (location == null)
            {
                Log.d(ClarpApplication.GA, "location is null in createTurnItem!!!!!!!");
            }

            turn = new TurnHistoryItem(type, player, suspect, weapon, location, alibi, alibiCard, null);

        }
        else if(type == TYPE_ALERT)
        {
            Log.d(ClarpApplication.GA, "Creating an alert turn history item");
            String alertText = clarpTurn.getString("alertText");

            turn = new TurnHistoryItem(type, null, null, null, null, null, null, alertText);
        }

        if (turn == null)
        {
            Log.d(ClarpApplication.GA, "Something went terribly wrong creating a turn history item");
        }

        return turn;
    }

    public void refreshHistory() throws JSONException{
        /*
         * PRECONDITIONS: ClarpGame is fully downloaded
         * 
         * We're going to compare the current ListView's array with the ClarpGame's turns array
         * Then, we'll add any missing items.
         * 
         * This should be called after refreshing the game info
         */

        Log.d(ClarpApplication.GA, "Refreshing History");



        loading = true;
        updateViews();

        historyFragment = pageAdapter.getHistoryFragment();
        JSONArray clarpTurns = game.getJSONArray("turns");

        int numHistory = historyFragment.getHistorySize();
        int numClarp = clarpTurns.length();

        Log.d(ClarpApplication.GA, "History: " + numHistory + ", Clarp: " + numClarp);

        // loop through any clarpTur items that do not yet exist in historyFragment
        for (int i = numHistory; i < numClarp; ++i)
        {
            /*
             * numHistory is the first index of clarpGame to add,
             * and numClarp - 1 is the final index of ClarpGame to add
             * i is our index
             */

            historyFragment.add( createTurnItem( clarpTurns.getJSONObject(i) ) );
        }

        if(game.getBoolean("gameOver") && (gameState == GameStates.IN_PROGRESS))
        {
            gameState = GameStates.LOST;
        }

        loading = false;
        updateViews();

    }

    //    public Player refuteSuggestion(){
    //
    //    	// TODO This needs to loop through all players, excluding the current one
    //    	// Right now, it's basically hardcoded to assume player #0 is the one who made the suggestion
    //
    //    	if (players.size() > 1)
    //    	{
    //    		for (Player p : players.subList(1, players.size())){
    //        		for (String id : p.getCardIds()){
    //        			if (id.equals(queuedSuspect.getObjectId()) || id.equals(queuedWeapon.getObjectId()) || id.equals(queuedScene.getObjectId()))
    //        				return p;
    //        		}
    //        	}
    //    	}
    //
    //    	return null;
    //    }

    public void clickCancel(View v){
        pw.dismiss();
    }

    public enum GameStates{
        IN_PROGRESS,WON,LOST,DISQUALIFIED;
    }

    private void updateViews()
    {
        if(loading)
        {
            /*
             * Show loading bar
             * Show loading text
             * Hide ListView
             * Hide Submit Button
             * Hide Accuse Button
             * Hide WhoseTurn TextView
             */

            //historyListView.setVisibility(View.GONE);
            suggestButton.setVisibility(View.GONE);
            accuseButton.setVisibility(View.GONE);
            whoseTurnText.setVisibility(View.GONE);

        }
        else
        {

            /*
             * Hide loading bar
             * Hide loading text
             * Show ListView
             * Show WhoseTurn TextView
             */
            //historyListView.setVisibility(View.VISIBLE);
            whoseTurnText.setVisibility(View.VISIBLE);

            if(isMyTurn)
            {
                /*
                 * Show Submit Button
                 * Show Accuse Button
                 * Set WhoseTurn TextView to say "It's your turn!"
                 */
                suggestButton.setVisibility(View.VISIBLE);
                accuseButton.setVisibility(View.VISIBLE);
                whoseTurnText.setText("It's your turn!");
            }
            else
            {
                /*
                 * Hide Submit Button
                 * Hide Accuse Button
                 * Set WhoseTurn TextView to say "It's _______'s turn."
                 */
                suggestButton.setVisibility(View.GONE);
                accuseButton.setVisibility(View.GONE);
                whoseTurnText.setText("It's not your turn yet.");
            }
        }


    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        View v = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

	    if (v instanceof EditText) {
	        View w = getCurrentFocus();
	        if (w != null){
		        int scrcoords[] = new int[2];
		        w.getLocationOnScreen(scrcoords);
		        float x = event.getRawX() + w.getLeft() - scrcoords[0];
		        float y = event.getRawY() + w.getTop() - scrcoords[1];
	
		        
		        if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom()) ) { 
	
		            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		            imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
		        }
	        }
	    }
	return ret;
	}
	
    
}



class MyAdapter extends FragmentPagerAdapter{
	ArrayList<TurnHistoryItem> historyItems;
	ArrayList<NoteItem> noteItems;
	ArrayList<ClarpCard> handItems;
	int pos = 1;
	Fragment fragment = null;
	HistoryFragment historyFragment;
	NotesFragment notesFragment;
	CardHandFragment cardHandFragment;
	
	public MyAdapter(FragmentManager fm) {
		super(fm);
		
		historyItems = new ArrayList<TurnHistoryItem>();
		noteItems = new ArrayList<NoteItem>();
		handItems = new ArrayList<ClarpCard>();
//		TurnHistoryItem firstItem = new TurnHistoryItem(2);
//		firstItem.result = "The Game is Afoot!";  	
//    	historyItems.add(firstItem);
	}
	
	@Override
	public Fragment getItem(int i){
		fragment = null;
		if (i == 0){
			fragment = new CardHandFragment(handItems);
			cardHandFragment = (CardHandFragment) fragment;
		}
		if (i == 1){
			fragment = new HistoryFragment(historyItems);
			historyFragment = (HistoryFragment) fragment;
		}
		if (i == 2){
			fragment = new NotesFragment(noteItems);
			notesFragment = (NotesFragment) fragment;
		}
		return fragment;
	}
	
	@Override
	public int getCount() {
		return 3;
	}
	
	@Override
	public CharSequence getPageTitle(int position){
		//String title = new String();
		if(position == 0){
			return "Hand";
		}else if (position == 1){
			return "Turn History";
		}else if (position == 2){
			return "Notes";
		}else
			return null;
	}
	
	public CardHandFragment getCardHandFragment(){
		return cardHandFragment;
	}
	
	public HistoryFragment getHistoryFragment(){
		return historyFragment;
	}
	
	public NotesFragment getNotesFragment(){
		return notesFragment;
	}
	
}
