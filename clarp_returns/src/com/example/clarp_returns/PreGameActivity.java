package com.example.clarp_returns;

import org.json.JSONArray;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


public class PreGameActivity extends ActionBarActivity{
	
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
	
	// result codes for activities that return with a result
    public static final int NEW_GAME = 10;
    public static final int ADD_CARD = 11;
	
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
                }
                else
                {
                    Log.d(ClarpApplication.TAG, "Something went wrong when querying the ClarpGame in PGA");
                }
            }
        });
        
        /*
         * Next, determine if the current user is the owner of this game
         */
        
        user = ParseUser.getCurrentUser();
        if (user.getObjectId() == game.getOwner())
        {
        	isOwner = true;
        	Log.d(ClarpApplication.TAG, "User is owner of this CLarpGame");
        }
        else
        {
        	Log.d(ClarpApplication.TAG, "User is NOT owner of this CLarpGame");
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
         * Initialize all the views and buttons
         */
        
        gameNameView = (TextView) findViewById(R.id.gameName);
        gameNameView.setText(game.getGameName());
        playerCountView = (TextView) findViewById(R.id.playerCount);
        suspectCountView = (TextView) findViewById(R.id.suspectCount);
        weaponCountView = (TextView) findViewById(R.id.weaponCount);
        locationCountView = (TextView) findViewById(R.id.locationCount);
        
        /*
         * Set the text for the TextViews
         */
        
        refreshCounts();
        
        addCardButton = (Button) findViewById(R.id.addCardButton);
        addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	/*
            	 * Takes user to the card creation activity.
            	 * Currently, this does not actually add the card to any game.
            	 * We will store all the card ObjectIds in local arrays,
            	 * and only save them to Parse once the Start Game button is pushed.
            	 */
            	
            	Intent intent = new Intent(PreGameActivity.this, NewClarpCardActivity.class);
                startActivityForResult(intent, ADD_CARD);
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
            	updateViewVisibility();
            }
        });
        
       
        
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	
    	//show or hide the Start Game button
    	updateViewVisibility();
    	
    	
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
    	playerCountView.setText("Players: " + players.length() + "/" + minPlayers);
    	suspectCountView.setText("Players: " + players.length() + "/" + minSuspects);
    	weaponCountView.setText("Players: " + players.length() + "/" + minWeapons);
    	locationCountView.setText("Players: " + players.length() + "/" + minLocations);
    	
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
    
}
