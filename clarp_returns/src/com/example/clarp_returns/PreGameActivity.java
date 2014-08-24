package com.example.clarp_returns;

import org.json.JSONArray;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


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
	
	
	
	
	private TextView gameNameView;
	private TextView playerCountView;
	private TextView suspectCountView;
	private TextView weaponCountView;
	private TextView locationCountView;
	private Button addCardButton;
	private Button startGameButton;
	private Button refreshButton;
	
	ClarpGame game = null;
	ParseUser user;
	Boolean isOwner = false;
	Boolean gameReady = true; // SET THIS TO FALSE, IT IS ONLY TRUE FOR TESTING PURPOSES
	
	JSONArray players;
	JSONArray suspects;
	JSONArray weapons;
	JSONArray locations;
	
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
                    Log.d(ClarpApplication.TAG, "Pregame: Game found, name is " + game.getGameName());
                    
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
                    
                    /*
                     *  Initialize the card arrays
                     *  We're going to work with a local copy of the arrays,
                     *  and then push the final, curated version to the ClarpGame when we're done
                     */
                    
                    
                    players = game.getJSONArray("players");
                    suspects = game.getJSONArray("suspects");
                    weapons = game.getJSONArray("weapons");
                    locations = game.getJSONArray("locations");
                    
                    /*
                     * Initialize all the views (requires game, so must wait for game to download)
                     */
                    
                    gameNameView = (TextView) findViewById(R.id.gameName);
                    gameNameView.setText("test");
                    playerCountView = (TextView) findViewById(R.id.playerCount);
                    suspectCountView = (TextView) findViewById(R.id.suspectCount);
                    weaponCountView = (TextView) findViewById(R.id.weaponCount);
                    locationCountView = (TextView) findViewById(R.id.locationCount);
                    
                    /*
                     * Set the text for the TextViews (requires game, so must wait for game to download)
                     */
                    
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
        
        
        
        addCardButton = (Button) findViewById(R.id.addCardButton);
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
                startActivityForResult(intent, ClarpApplication.ADD_CARD);
            }
        });
        startGameButton = (Button) findViewById(R.id.startGameButton);
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	/*
            	 * Takes user to the game activity.
            	 * 
            	 */
            	
            	// set isStarted to TRUE, so that we skip PreGame from now on.
            	game.startGame();
            	
            	Intent intent = new Intent(PreGameActivity.this, GameActivity.class);
                intent.putExtra("game_id", game.getObjectId());
                startActivity(intent);
            }
        });
        
        refreshButton = (Button) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	refreshCounts();
            }
        });
        
       
        
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	
    	//show or hide the Start Game button
    	if(game != null)
    	{
    		refreshCounts();
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
    
    private void refreshCounts()
    {
    	if(game == null)
    	{
    		Log.d(ClarpApplication.TAG, "Attempted to refresh with null game!");
    		return;
    	}
    	playerCountView.setText("Players: " + players.length() + "/" + minPlayers);
    	suspectCountView.setText("Suspects: " + suspects.length() + "/" + minSuspects);
    	weaponCountView.setText("Weapons: " + weapons.length() + "/" + minWeapons);
    	locationCountView.setText("Locations: " + locations.length() + "/" + minLocations);
    	
    	/*
    	 * The game may be ready to start now, so we should check if it's time
    	 * to show the StartGame button
    	 */
    	updateViewVisibility();
    	
    }
    
    private void updateViewVisibility()
    {
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
    							String cardType = card.getCardType();
    							
    							Log.d(ClarpApplication.TAG, "Card type is " + cardType);
    							
    							/*
    							 * Now, put it in the appropriate array:
    							 */
    							
    							// if it is a suspect card
    							if(cardType.equals("suspect"))
    							{
    								// put it in the suspects array
    								suspects.put(card.getObjectId());
    							}
    							// if it is a weapon card
    							else if(cardType.equals("weapon"))
    							{
    								// put it in the weapons array
    								weapons.put(card.getObjectId());
    							}
    							// if it is a location card
    							else if(cardType.equals("location"))
    							{
    								// put it in the locations array
    								locations.put(card.getObjectId());
    							}
    							else
    							{
    								Log.d(ClarpApplication.TAG, "Card is not of a valid type, ERROR ERROR ERROR!");
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
    
//    /**
//     * A placeholder fragment containing a simple view.
//     */
//    public static class PlaceholderFragment extends Fragment {
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_pre_game, container, false);
//            
//            TextView playerCountView = (TextView) rootView.findViewById(R.id.playerCount);
//            TextView suspectCountView = (TextView) rootView.findViewById(R.id.suspectCount);
//            TextView weaponCountView = (TextView) rootView.findViewById(R.id.weaponCount);
//            TextView locationCountView = (TextView) rootView.findViewById(R.id.locationCount);
//            
//            Button addCardButton = (Button) rootView.findViewById(R.id.addCardButton);
//            addCardButton.setOnClickListener(new View.OnClickListener() {
//            	@Override
//                public void onClick(View v) {
//                	
//                	/*
//                	 * Takes user to the card creation activity.
//                	 * Currently, this does not actually add the card to any game.
//                	 * We will store all the card ObjectIds in local arrays,
//                	 * and only save them to Parse once the Start Game button is pushed.
//                	 * 
//                	 * I don't think it's vital to send any extra data with the intent.
//                	 * The only thing I can think of would be to send the game name
//                	 * for a more helpful UI in the NewCard activity.
//                	 * Or maybe, send the current counts, so it can suggest a card type that the
//                	 * game needs more of. #stretchgoal
//                	 */
//                	
//                	Intent intent = new Intent(getActivity(), NewClarpCardActivity.class);
//                    startActivityForResult(intent, ClarpApplication.ADD_CARD);
//               }
//            });
//            
//            Button startGameButton = (Button) rootView.findViewById(R.id.startGameButton);
//            startGameButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                	
//                	/*
//                	 * Takes user to the game activity.
//                	 * 
//                	 */
//                	
//                	// set isStarted to TRUE, so that we skip PreGame from now on.
//                	game.startGame();
//                	
//                	Intent intent = new Intent(getActivity(), GameActivity.class);
//                    intent.putExtra("game_id", game.getObjectId());
//                    startActivity(intent);
//                }
//            });
//            
//            Button refreshButton = (Button) rootView.findViewById(R.id.refreshButton);
//            refreshButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                	refreshCounts();
//                }
//            });
//            return rootView;
//        }
//    }
//    
    
    
    
    
}
