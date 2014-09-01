package com.example.clarp_returns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class PreGameActivity extends ActionBarActivity
{

    /*
     * This Activity is intended to serve the following purposes:
     *  - Screen all users see once they accept the game invite
     *  - Screen all users see (if the game has not yet started) if they click the game in the StartActivity ListView
     *  - Counter to inform players how many players have joined
     *  - Counters to tell players how many suspect, weapon, and location cards have been added to the game
     *  - Button to allow any player to create a new card
     *  - ONLY FOR THE GAME CREATOR: Button to officially start the game (not visible until the game has enough players and cards)
     * 
     *  In order for the game to be started there must be a minimum number of players and cards
     * 
     *  ONCE THE START BUTTON HAS BEEN CLICKED:
     *  If there are extra cards, only the maximum number of cards are chosen
     *  The ClarpGame arrays are updated to ONLY reflect the cards that are to be used during the game.
     *  The cards are randomly distributed amongst the players
     *  A "Starting player" is chosen (they get the first turn)
     * 
     */


    private TextView loadTextView;
    private ProgressBar loadingBar;
    private TextView gameNameView;
    private TextView playerCountView;
    private TextView suspectCountView;
    private TextView weaponCountView;
    private TextView locationCountView;
    private Button addCardButton;
    private Button startGameButton;
    private Button refreshButton;
    private Button cardsListButton;

    String gameId;
    ClarpGame game = null;
    ParseUser user;
    Boolean isOwner = false;
    Boolean gameLoaded = false;
    Boolean gameReady = true; // SET THIS TO FALSE, IT IS ONLY TRUE FOR TESTING PURPOSES

    String gameName;
    JSONArray players;
    JSONArray suspects;
    JSONArray weapons;
    JSONArray locations;
    
    ArrayList<ClarpCard> cards;
    String murdererId = null;
    String murderWeaponId = null;
    String crimeSceneId = null;
    

    int minPlayers = 6;
    int minSuspects = 6;
    int minWeapons = 6;
    int minLocations = 6;

    int maxPlayers = 8;
    int maxSuspects = 8;
    int maxWeapons = 8;
    int maxLocations = 8;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_game);



        /*
         * Initialize all the views (requires game, so must wait for game to download)
         */

        loadTextView = (TextView) findViewById(R.id.loadText);
        loadingBar = (ProgressBar) findViewById(R.id.progressBar1);
        gameNameView = (TextView) findViewById(R.id.gameName);
        playerCountView = (TextView) findViewById(R.id.playerCount);
        suspectCountView = (TextView) findViewById(R.id.suspectCount);
        weaponCountView = (TextView) findViewById(R.id.weaponCount);
        locationCountView = (TextView) findViewById(R.id.locationCount);
        addCardButton = (Button) findViewById(R.id.addCardButton);
        startGameButton = (Button) findViewById(R.id.startGameButton);
        refreshButton = (Button) findViewById(R.id.refreshButton);
        cardsListButton = (Button) findViewById(R.id.cards_list_button);

        updateViewVisibility();

        /*
         * Next, get the ClarpGame so we know what we're working with
         */

        Intent mainIntent = getIntent();
        ParseQuery<ClarpGame> query = ParseQuery.getQuery("ClarpGame");
        gameId = mainIntent.getStringExtra("game_id");
        query.getInBackground(mainIntent.getStringExtra("game_id"), new GetCallback<ClarpGame>() {
            @Override
            public void done(ClarpGame object, ParseException e) {
                if (e == null)
                {
                    game = object;

                    gameName = game.getGameName();
                    players = game.getJSONArray("players");
                    suspects = game.getJSONArray("suspects");
                    weapons = game.getJSONArray("weapons");
                    locations = game.getJSONArray("locations");

                    Log.d(ClarpApplication.TAG, "Pregame: Game found, name is " + gameName);



                    /*
                     * Next, determine if the current user is the owner of this game
                     */

                    user = ParseUser.getCurrentUser();
                    //Log.d(ClarpApplication.TAG, "User ID: " + user.getObjectId());
                    //Log.d(ClarpApplication.TAG, "Owner ID: " + game.getOwner());
                    if (user.getObjectId().equals(game.getOwner()))
                    {
                        isOwner = true;
                        Log.d(ClarpApplication.TAG, "User is owner of this ClarpGame");
                    }
                    else
                    {
                        Log.d(ClarpApplication.TAG, "User is NOT owner of this ClarpGame");
                    }

                    refreshCounts();
                }
                else
                {
                    Log.d(ClarpApplication.TAG, "Something went wrong when querying the ClarpGame in PGA");
                }
            }
        });



        /*
         * Initialize buttons. These do not rely on game being loaded.
         */
        addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                 * Takes user to the card creation activity.
                 * Currently, this does not actually add the card to any game.
                 * We will store all the card ObjectIds in local arrays,
                 * and only save them to Parse once the Start Game button is pushed.
                 * 
                 * I don't think it's vital to send any extra data with the intent.
                 * The only thing I can think of would be to send the game name
                 * for a more helpful UI in the NewCard activity.
                 * Or maybe, send the current counts, so it can suggest a card type that the
                 * game needs more of. #stretchgoal
                 */

                Intent intent = new Intent(PreGameActivity.this, NewClarpCardActivity.class);
                intent.putExtra("game_id", game.getObjectId());
                startActivityForResult(intent, ClarpApplication.ADD_CARD);
            }
        });

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	/*
            	 * WE NEED TO BE SURE THAT WE HAVE THE LATEST GAME FROM THE SERVER FIRST
            	 */

                /*
                 * This is where we actually add all the cards to the game!
                 */
            	
            	cards = new ArrayList<ClarpCard>();
            	
            	ParseQuery<ClarpCard> query = ParseQuery.getQuery("ClarpCard");
                query.whereEqualTo("gameId", gameId);
                
                query.findInBackground(new FindCallback<ClarpCard>() {
                    public void done(List<ClarpCard> cList, ParseException e) {
                        if (e == null) {
                            
                        	
                        	// add the queried cards to the local ArrayList
                            for (int i = 0; i < cList.size(); ++i)
                            {
                            	cards.add(cList.get(i));
                            	Log.d(ClarpApplication.TAG, "added card " + cards.get(i).getCardName());
                            }
                            
                            // Shuffle the cards so they can be distributed
                            Collections.shuffle(cards);
                            Log.d(ClarpApplication.TAG, "shuffled cards");
                            
                            
                            
                            int player = 0; // this should be a random player, so player 1 doesn't always get most cards
                    		for (int i = 0; i < cards.size()-1; ++i){
                    			
                    			/*
                    			 * Take the first card of each type, and set it aside
                    			 * These cards will be the solution that the players attempt to guess
                    			 * They will be added to the game's solution[] array when we are
                    			 * done dealing the rest of the cards
                    			 */
                    			
                    			if (murdererId == null && cards.get(i).getCardType() == ClarpCard.CardType.SUSPECT){
                    				murdererId = cards.get(i).getObjectId();
                    				Log.d(ClarpApplication.TAG, "Murderer: " + cards.get(i).getCardName());
                    				continue;
                    			}
                    			if (murderWeaponId == null && cards.get(i).getCardType() == ClarpCard.CardType.WEAPON){
                    				murderWeaponId = cards.get(i).getObjectId();
                    				Log.d(ClarpApplication.TAG, "Murder Weapon: " + cards.get(i).getCardName());
                    				continue;
                    			}
                    			if (crimeSceneId == null && cards.get(i).getCardType() == ClarpCard.CardType.LOCATION){
                    				crimeSceneId = cards.get(i).getObjectId();
                    				Log.d(ClarpApplication.TAG, "Crime Scene: " + cards.get(i).getCardName());
                    				continue;
                    			}
                    			
                    			/*
                    			 * Any cards not part of the solution should be given to a player as a clue.
                    			 * Here's why this is harder than it should be:
                    			 * 
                    			 * I'm trying to add a String to the player's turns[] JSONArray.
                    			 * Which is part of a JSONObject,
                    			 * which is inside of another JSONArray
                    			 * which is inside of our ClarpGame
                    			 * 
                    			 * we already have JSONArray players, which is a copy of the ClarpGame's array
                    			 * (We just NEED to make sure it's the most recent version before this happens)
                    			 * 
                    			 * I need to change the correct element of the correct element of players
                    			 * each time we go through this for loop
                    			 * and then at the end, game.put("players", players) to overwrite it
                    			 */
                    			
                    			try {
									((JSONObject) players.get(player)).getJSONArray("facts").put(cards.get(i).getObjectId());
								} catch (JSONException e1) {
									// TODO Auto-generated catch block
									Log.d(ClarpApplication.TAG, "Messed up tying to add a card's id to facts[]");
									Log.d(ClarpApplication.TAG, "player index: " + player);
									Log.d(ClarpApplication.TAG, "card name: " + cards.get(i).getCardName() + " and id: " + cards.get(i).getObjectId());
									
									e1.printStackTrace();
								}
                    			
								
                    			
								//cycle through the player list so each player is dealt a card one at a time
                    			player++;
                    			if (player == players.length()) 
                    			{
                    				player = 0;
                    			}
                    		}
                    		
                    		// update ClarpGame to reflect the distributed clues
                    		game.put("players", players);
                    		
                    		if (murdererId == null || murderWeaponId == null || crimeSceneId== null)
                    		{
                    			// This should absolutely never happen.
                    			Log.d(ClarpApplication.TAG, "A murderer, murder weapon, or crime scene has not been selected!");
                    			return;
                    		}
                    		game.setSolution(murdererId, murderWeaponId, crimeSceneId);
                        	
                            game.startGame();
                            game.saveInBackground(new SaveCallback() {

                                @Override
                                public void done(ParseException e)
                                {
                                	if (e == null)
                                	{
                                		/*
                                         * Don't progres into GameActivity until Parse is fully synced
                                         */
                                        Intent intent = new Intent(PreGameActivity.this, GameActivity.class);
                                        intent.putExtra("game_id", game.getObjectId());
                                        startActivity(intent);
                                	}
                                	else
                                	{
                                		Log.d(ClarpApplication.TAG, "Messed up tying to save game :(");
                                		e.printStackTrace();
                                	}
                                    
                                }
                            });
                            
                        } else {
                        	Log.d(ClarpApplication.TAG, "query failure (?)");
                        }
                    }
                });
            	
            	
            }
        });


        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                syncGame();
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();

        if(game != null)
        {
            syncGame();
        }
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

    private void syncGame()
    {
        Log.d(ClarpApplication.TAG, "SYNC!");

        gameLoaded = false;
        updateViewVisibility();

        //Log.d(ClarpApplication.TAG, "Weapons before: " + game.getJSONArray("weapons").length());

        Log.d(ClarpApplication.TAG, "Must fetch data");
        Log.d(ClarpApplication.TAG, "Weapons old: " + game.getJSONArray("weapons").length());
        /*
         * I tried using refreshInBackground, and fetchInBackground, but neither of them worked. Oh well...
         */

        ParseQuery<ClarpGame> query = ParseQuery.getQuery("ClarpGame");
        query.getInBackground(game.getObjectId(), new GetCallback<ClarpGame>() {
            @Override
            public void done(ClarpGame object, ParseException e) {
                if (e == null)
                {
                    game = object;

                    Log.d(ClarpApplication.TAG, "Weapons new: " + game.getJSONArray("weapons").length());

                    gameName = game.getGameName();
                    players = game.getJSONArray("players");
                    suspects = game.getJSONArray("suspects");
                    weapons = game.getJSONArray("weapons");
                    locations = game.getJSONArray("locations");

                    refreshCounts();
                }
                else
                {
                    Log.d(ClarpApplication.TAG, "Error fetching game");
                }
            }
        });


    }


    private void refreshCounts()
    {
        if(game == null)
        {
            // this should NEVER happen
            Log.d(ClarpApplication.TAG, "Attempted to refresh with null game!");
            return;
        }

        gameNameView.setText(gameName);
        playerCountView.setText("Players: " + players.length() + "/" + minPlayers);
        suspectCountView.setText("Suspects: " + game.getInt("numSuspects") + "/" + minSuspects);
        weaponCountView.setText("Weapons: " + game.getInt("numWeapons") + "/" + minWeapons);
        locationCountView.setText("Locations: " + game.getInt("numLocations") + "/" + minLocations);

        gameLoaded = true;

        /*
         * The game may be ready to start now, so we should check if it's time
         * to show the StartGame button
         */
        updateViewVisibility();
    }

    private void updateViewVisibility()
    {
        if(!gameLoaded)
        {
            loadingBar.setVisibility(View.VISIBLE);
            loadTextView.setVisibility(View.VISIBLE);
            gameNameView.setVisibility(View.GONE);
            playerCountView.setVisibility(View.GONE);
            suspectCountView.setVisibility(View.GONE);
            weaponCountView.setVisibility(View.GONE);
            locationCountView.setVisibility(View.GONE);
        }
        else
        {
            loadingBar.setVisibility(View.GONE);
            loadTextView.setVisibility(View.GONE);
            gameNameView.setVisibility(View.VISIBLE);
            playerCountView.setVisibility(View.VISIBLE);
            suspectCountView.setVisibility(View.VISIBLE);
            weaponCountView.setVisibility(View.VISIBLE);
            locationCountView.setVisibility(View.VISIBLE);
        }

        if(isOwner && gameReady)
        {
            startGameButton.setVisibility(View.VISIBLE);
        }
        else
        {
            startGameButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode)
        {
            case (ClarpApplication.ADD_CARD) : {
                if (resultCode == Activity.RESULT_OK)
                {

                    /*
                     * Here we need to go look at the newly created card
                     * and add it to one of our local arrays
                     * Then, we need to update the counts to reflect the addition.
                     */

                    ParseQuery<ClarpCard> query = ParseQuery.getQuery("ClarpCard");
                    query.getInBackground(data.getStringExtra("cardId"), new GetCallback<ClarpCard>() {
                        @Override
                        public void done(ClarpCard card, ParseException e) {
                            if (e == null)
                            {
                                //determine card type
                                ClarpCard.CardType cardType = card.getCardType();
                                Log.d(ClarpApplication.TAG, "Card type is " + cardType.toString());

                                /*
                                 * Now, put it in the appropriate array:
                                 */
                                switch(cardType) {
                                    case SUSPECT:
                                        suspects.put(card.getObjectId());
                                        break;
                                    case WEAPON:
                                        weapons.put(card.getObjectId());
                                        break;
                                    case LOCATION:
                                        locations.put(card.getObjectId());
                                        break;
                                    default:
                                        Log.d(ClarpApplication.TAG, "Card is not of a valid type, ERROR ERROR ERROR!");
                                        break;
                                }
                                
                                /*
                                 * Refresh the counts to reflect the new addition
                                 */

                                refreshCounts();
                            }
                            else
                            {
                                Log.d(ClarpApplication.TAG, "Something went wrong when querying the ClarpCard in PGA");
                            }
                        }
                    });
                }

            }
            break;
        }
    }

    public void clickCardsList(View v) {
        Intent intent = new Intent(PreGameActivity.this, CardListActivity.class);
        intent.putExtra("game_id", gameId);
        intent.putExtra("requestCode", ClarpApplication.VIEW_ALL_GAME_CARDS);
        startActivityForResult(intent, ClarpApplication.VIEW_ALL_GAME_CARDS);

    }

}
