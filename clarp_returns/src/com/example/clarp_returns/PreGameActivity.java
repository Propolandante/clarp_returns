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
import com.parse.ParseAnalytics;
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
    Boolean gameReady = false; // SET THIS TO FALSE, IT IS ONLY TRUE FOR TESTING PURPOSES

    String gameName;
    JSONArray players;

    ArrayList<ClarpCard> cards;
    String murdererId = null;
    String murderWeaponId = null;
    String crimeSceneId = null;


    int minPlayers = 1;
    int minSuspects = 1;
    int minWeapons = 1;
    int minLocations = 1;

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
        user = ParseUser.getCurrentUser();
        Intent mainIntent = getIntent();
        gameId = mainIntent.getStringExtra("game_id");
        ParseAnalytics.trackAppOpened(mainIntent);
        if(mainIntent.getExtras().getString("notification") != null){
            // invite alert dialog
            Log.d(ClarpApplication.PGA, "PGA was opened via notification, showing dialog now");
            showInviteDialog();
        }
        ParseQuery<ClarpGame> query = ParseQuery.getQuery("ClarpGame");

        query.getInBackground(mainIntent.getStringExtra("game_id"), new GetCallback<ClarpGame>() {
            @Override
            public void done(ClarpGame object, ParseException e) {
                if (e == null)
                {
                    game = object;

                    gameName = game.getGameName();
                    players = game.getJSONArray("players");

                    Log.d(ClarpApplication.PGA, "Pregame: Game found, name is " + gameName);



                    /*
                     * Next, determine if the current user is the owner of this game
                     */

                    //user = ParseUser.getCurrentUser();
                    //Log.d(ClarpApplication.PGA, "User ID: " + user.getObjectId());
                    //Log.d(ClarpApplication.PGA, "Owner ID: " + game.getOwner());
                    if (user.getObjectId().equals(game.getOwner()))
                    {
                        isOwner = true;
                        Log.d(ClarpApplication.PGA, "User is owner of this ClarpGame");
                    }
                    else
                    {
                        Log.d(ClarpApplication.PGA, "User is NOT owner of this ClarpGame");
                    }

                    refreshCounts();
                }
                else
                {
                    Log.d(ClarpApplication.PGA, "Something went wrong when querying the ClarpGame in PGA");
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

                ParseQuery<ClarpGame> query = ParseQuery.getQuery("ClarpGame");
                query.getInBackground(game.getObjectId(), new GetCallback<ClarpGame>() {
                    @Override
                    public void done(ClarpGame object, ParseException e) {
                        if (e == null)
                        {
                            game = object;



                            gameName = game.getGameName();
                            players = game.getJSONArray("players");

                            /*
                             * This is where we actually add all the cards to the game!
                             */

                            cards = new ArrayList<ClarpCard>();

                            ParseQuery<ClarpCard> query = ParseQuery.getQuery("ClarpCard");
                            query.whereEqualTo("gameId", gameId);

                            query.findInBackground(new FindCallback<ClarpCard>() {
                                @Override
                                public void done(List<ClarpCard> cList, ParseException e) {
                                    if (e == null) {


                                        // add the queried cards to the local ArrayList
                                        for (int i = 0; i < cList.size(); ++i)
                                        {
                                            cards.add(cList.get(i));
                                            Log.d(ClarpApplication.PGA, "added card " + cards.get(i).getCardName());
                                        }

                                        // Shuffle the cards so they can be distributed
                                        Collections.shuffle(cards);
                                        Log.d(ClarpApplication.PGA, "shuffled cards");



                                        int player = 0; // this should be a random player, so player 1 doesn't always get most cards
                                        for (int i = 0; i < cards.size(); ++i){

                                            /*
                                             * Take the first card of each type, and set it aside
                                             * These cards will be the solution that the players attempt to guess
                                             * They will be added to the game's solution[] array when we are
                                             * done dealing the rest of the cards
                                             */

                                            if ((murdererId == null) && (cards.get(i).getCardType() == ClarpCard.CardType.SUSPECT)){
                                                murdererId = cards.get(i).getObjectId();
                                                Log.d(ClarpApplication.PGA, "Murderer: " + cards.get(i).getCardName());
                                                continue;
                                            }
                                            if ((murderWeaponId == null) && (cards.get(i).getCardType() == ClarpCard.CardType.WEAPON)){
                                                murderWeaponId = cards.get(i).getObjectId();
                                                Log.d(ClarpApplication.PGA, "Murder Weapon: " + cards.get(i).getCardName());
                                                continue;
                                            }
                                            if ((crimeSceneId == null) && (cards.get(i).getCardType() == ClarpCard.CardType.LOCATION)){
                                                crimeSceneId = cards.get(i).getObjectId();
                                                Log.d(ClarpApplication.PGA, "Crime Scene: " + cards.get(i).getCardName());
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
                                                Log.d(ClarpApplication.PGA, "Messed up tying to add a card's id to facts[]");
                                                Log.d(ClarpApplication.PGA, "player index: " + player);
                                                Log.d(ClarpApplication.PGA, "card name: " + cards.get(i).getCardName() + " and id: " + cards.get(i).getObjectId());

                                                e1.printStackTrace();
                                            }



                                            //cycle through the player list so each player is dealt a card one at a time
                                            player++;
                                            if (player == players.length())
                                            {
                                                player = 0;
                                            }


                                        }

                                        // The player who was next to be dealt a card (if there were any cards left) will go first
                                        try {
                                            game.put("whoseTurn", ((JSONObject) players.get(player)).getString("facebookId"));
                                        } catch (JSONException e1) {
                                            // TODO Auto-generated catch block
                                            e1.printStackTrace();
                                            Log.d(ClarpApplication.PGA, "Error trying to set whoseTurn");
                                        }

                                        // update ClarpGame to reflect the distributed clues
                                        game.put("players", players);

                                        if ((murdererId == null) || (murderWeaponId == null) || (crimeSceneId== null))
                                        {
                                            // This should absolutely never happen.
                                            Log.d(ClarpApplication.PGA, "A murderer, murder weapon, or crime scene has not been selected!");
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
                                                    Log.d(ClarpApplication.PGA, "Messed up tying to save game :(");
                                                    e.printStackTrace();
                                                }

                                            }
                                        });

                                    } else {
                                        Log.d(ClarpApplication.PGA, "query failure (?)");
                                    }
                                }
                            });


                        }
                        else
                        {
                            Log.d(ClarpApplication.PGA, "Error fetching game");
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
        Log.d(ClarpApplication.PGA, "SYNC!");

        gameLoaded = false;
        updateViewVisibility();

        //Log.d(ClarpApplication.PGA, "Weapons before: " + game.getJSONArray("weapons").length());

        Log.d(ClarpApplication.PGA, "Must fetch data");
        Log.d(ClarpApplication.PGA, "Weapons old: " + game.getJSONArray("weapons").length());
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

                    Log.d(ClarpApplication.PGA, "Weapons new: " + object.getJSONArray("weapons").length());

                    gameName = game.getGameName();
                    players = game.getJSONArray("players");

                    refreshCounts();
                }
                else
                {
                    Log.d(ClarpApplication.PGA, "Error fetching game");
                }
            }
        });


    }


    private void refreshCounts()
    {
        if(game == null)
        {
            // this should NEVER happen
            Log.d(ClarpApplication.PGA, "Attempted to refresh with null game!");
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

        if(!gameReady)
        {
            /*
             * Check to see if we meet the minimum requirements
             */

            Log.d(ClarpApplication.PGA, "Players: " + players.length() + "/" + minPlayers);
            Log.d(ClarpApplication.PGA, "Suspects: " + game.getInt("numSuspects") + "/" + minSuspects);
            Log.d(ClarpApplication.PGA, "Weapons: " + game.getInt("numWeapons") + "/" + minWeapons);
            Log.d(ClarpApplication.PGA, "Locations: " + game.getInt("numLocations") + "/" + minLocations);


            if((players.length() >= minPlayers) && (game.getInt("numSuspects") >= minSuspects) && (game.getInt("numWeapons") >= minWeapons) && (game.getInt("numLocations") >= minLocations))
            {
                gameReady = true;
            }

        }

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

                    Log.d(ClarpApplication.PGA, "Returning to PGA from card creation");

                    // this is unnecessary, it's already being called in onResume()
                    //syncGame();
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

    private void showInviteDialog() {
        InviteDialogFragment inviteDialog = new InviteDialogFragment();
        Bundle args = new Bundle();
        args.putString("game_id", gameId);
        args.putString("user_id", user.getObjectId());
        inviteDialog.setArguments(args);
        inviteDialog.show(getFragmentManager(), "invite");
        Log.d(ClarpApplication.PGA, "Invite Dialog is shown");
    }

}
