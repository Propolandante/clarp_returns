package com.example.clarp_returns;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class StartActivity extends ActionBarActivity {

    // result codes for activities that return with a result


    //protected static final String TAG = "StartActivity";
    private ListView gameListView;
    private ArrayList<ClarpGame> gameList;
    private GameAdapter arrayAdapter;
    private Dialog progressDialog;
    private Button newGameButton;
    private Button loginButton;
    private TextView userNameView;
    private ProgressBar listLoadingView;

    public Boolean gamesLoaded = false;
    public Boolean infoUpdated = false;
    
    public Boolean loadHalter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_start);
        
        loadHalter = true;

        // Parse Analytics to track when people respond to Push Notifications
        ParseAnalytics.trackAppOpened(getIntent());

        // this view displays the name of the logged in user
        userNameView = (TextView) findViewById(R.id.message);
        // this is the list of the user's active games. Each is a button to enter Game activity
        gameListView = (ListView) findViewById(R.id.games_list_view);
        // this loading bar shows when waiting for the list of games to be queries
        listLoadingView = (ProgressBar) findViewById(R.id.progressBar1);
        // self explanatory. Button enters New Game activity
        newGameButton = (Button) findViewById(R.id.footer);
        loginButton = (Button) findViewById(R.id.fb_login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(ClarpApplication.TAG, "Login button clicked");
                onLoginButtonClicked();
            }
        });

        ParseUser currentUser = ParseUser.getCurrentUser();
        if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
            // Go to the user info activity
            ClarpApplication.IS_LOGGED_IN = true;
            Log.v(ClarpApplication.TAG, "User already logged in!");

            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
            try {
                Log.d(ClarpApplication.TAG, "This installation is " + installation.getInstallationId());
                installation.put("facebookId", currentUser.getJSONObject("profile").getString("facebookId"));
                installation.saveInBackground();
            } catch (IllegalArgumentException e) {
                Log.d(ClarpApplication.TAG, "ArgumentError putting installation info");
                e.printStackTrace();
            } catch (JSONException e) {
                Log.d(ClarpApplication.TAG, "JSONError putting installation info");
                e.printStackTrace();
            }
            ParseInstallation.getCurrentInstallation().saveInBackground();
        }
        else
        {
            ClarpApplication.IS_LOGGED_IN = false;
        }


        if(ClarpApplication.IS_LOGGED_IN)
        {
            makeMeRequest();
            Log.v(ClarpApplication.TAG, "Since user is already logged in, we will update this text to welcome them.");
        }
        else
        {
            userNameView.setText("Please log in to use CLARP");
            Log.v(ClarpApplication.TAG, "User is not logged in");
        }

    }



    @Override
    protected void onResume() {
        super.onResume();

        updateViewVisibility();

        ParseUser user = ParseUser.getCurrentUser();

        //gamesListView.setEmptyView(findViewById(R.id.empty_games_view));

        gameListView.setOnItemClickListener(new OnItemClickListener() {

            // user clicks to go to a select screen where they can choose
            // to view any list
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {

                /*
                 * The user clicked on a game!
                 * First, we have to see if this game has been started
                 * If so, we send it to GameActivity (GA)
                 * If not, we send it to PreGameActivity (PGA)
                 * Either way, we must be sure to send the game's ObjectId as part of the Intent
                 */

                ClarpGame clickedGame = gameList.get((int) id);
                Intent intent;

                if(clickedGame.ifStarted())
                {
                    intent = new Intent(StartActivity.this, GameActivity.class);
                }
                else
                {
                    intent = new Intent(StartActivity.this, PreGameActivity.class);
                }

                intent.putExtra("game_id", clickedGame.getObjectId());
                startActivity(intent);
            }
        });

        if (ClarpApplication.IS_LOGGED_IN && !loadHalter)
        {
            refreshGames(user);
        }
        
        loadHalter = false;
    }

    public void refreshGames(ParseUser user) {

        // This function ASSUMES that user is not null.

        // every time the user resumes the activity, refresh the game List
        gamesLoaded = false;
        updateViewVisibility();

        if(gameList != null){
            gameList.clear();
        }

        gameList = new ArrayList<ClarpGame>();
        arrayAdapter = new GameAdapter(getApplicationContext(), gameList);
        gameListView.setAdapter(arrayAdapter);

        String id;

        JSONObject userProfile = user.getJSONObject("profile");
        try {
            id = userProfile.getString("facebookId");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(ClarpApplication.TAG, "JSON Error, quiting now");
            return;
        }

        ParseQuery<ClarpGame> query = ParseQuery.getQuery("ClarpGame");
        query.whereEqualTo("fbUsers", id);

        query.findInBackground(new FindCallback<ClarpGame>() {
            @Override
            public void done(List<ClarpGame> games, ParseException e) {
                if (e == null) {
                    Log.d(ClarpApplication.TAG, "query success (?)");
                    for (int i = 0; i < games.size(); ++i)
                    {
                        gameList.add(games.get(i));
                    }

                    arrayAdapter.notifyDataSetChanged();
                    gamesLoaded = true;
                    updateViewVisibility();

                } else {
                    Log.d(ClarpApplication.TAG, "query failure (?)");
                }
            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);
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
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_start, container, false);
            return rootView;
        }
    }

    public void clickNewGame(View v) {
        Intent intent = new Intent(StartActivity.this, NewGameActivity.class);
        startActivityForResult(intent, ClarpApplication.NEW_GAME);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseUser currentUser = ParseUser.getCurrentUser();
        if(requestCode == ClarpApplication.NEW_GAME) {
            //refreshGameList(currentUser);
        } else {
            ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
        }
    }


    public void onLoginButtonClicked() {
        StartActivity.this.progressDialog = ProgressDialog.show(StartActivity.this, "", "Logging in...", true);
        List<String> permissions = Arrays.asList("public_profile", "user_friends");
        ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                StartActivity.this.progressDialog.dismiss();
                if (user == null) {
                    Log.d(ClarpApplication.TAG, "Uh oh. The user cancelled the Facebook login.");
                    ClarpApplication.IS_LOGGED_IN = false;
                } else if (user.isNew()) {
                    Log.d(ClarpApplication.TAG, "User signed up and logged in through Facebook!");
                    ClarpApplication.IS_LOGGED_IN = true;
                    makeMeRequest();
                } else {
                    Log.d(ClarpApplication.TAG, "User logged in through Facebook!");
                    ClarpApplication.IS_LOGGED_IN = true;
                    makeMeRequest();
                }
            }
        });

        updateViewVisibility();
    }

    private void makeMeRequest() {
        Request request = Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (user != null) {
                    // Create a JSON object to hold the profile info
                    JSONObject userProfile = new JSONObject();
                    try {
                        // Populate the JSON object
                        userProfile.put( "facebookId", user.getId() );
                        userProfile.put( "name", user.getName() );
                        userProfile.put( "firstName", user.getFirstName() );

                        // Save the user profile info in a user property
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        currentUser.put("profile", userProfile);
                        // tis function will save the User to Parse
                        grabProfilePic(currentUser, user.getId());

                        // Show the user info
                        updateViewsWithProfileInfo();
                    } catch (JSONException e) {
                        Log.d(ClarpApplication.TAG, "Error parsing returned user data.");
                        Log.e("Error", "Error message is " + e.getMessage());
                        e.printStackTrace();
                    } catch (ClientProtocolException e) {
                        Log.d(ClarpApplication.TAG, "Error grabbing user's profile pic");
                        Log.e("Error", "Error message is " + e.getMessage());
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.e("Error", "Error message is " + e.getMessage());
                        e.printStackTrace();
                    }

                    refreshGames(ParseUser.getCurrentUser());

                } else if (response.getError() != null) {
                    if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY) || (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
                        Log.d(ClarpApplication.TAG, "The facebook session was invalidated.");
                        //onLogoutButtonClicked();
                    } else {
                        Log.d(ClarpApplication.TAG, "Some other error: " + response.getError().getErrorMessage());
                    }
                }
                updateViewVisibility();
            }
        });
        request.executeAsync();

    }

    private void grabProfilePic( final ParseUser currentUser, String fbId ) throws ClientProtocolException, IOException {
        URL img_value = null;
        try {
            img_value = new URL("https://graph.facebook.com/" + fbId + "/picture?type=large");
        } catch (MalformedURLException e){
            Log.e("Error", "Error message is " + e.getMessage());
            e.printStackTrace();
        }
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Bitmap bitmap = BitmapFactory.decodeStream(img_value.openConnection().getInputStream());
            Log.i(ClarpApplication.TAG, "image retrieved from facebook");

            if(bitmap != null){
                ParseFile saveImageFile= new ParseFile("profilePicture.jpg",compressAndConvertImageToByteFrom(bitmap));
                currentUser.put("profilePicture", saveImageFile);
            }
            currentUser.saveInBackground(new SaveCallback() {

                @Override
                public void done(ParseException e) {
                    if (e == null)
                    {
                        infoUpdated = true;
                        updateViewVisibility();
                    }
                    else
                    {
                        Log.d(ClarpApplication.TAG,"User Profile not saved!!!");
                        e.printStackTrace();
                    }
                }

            });
        } catch (IOException e) {
            Log.e("Error", "Error message is " + e.getMessage());
            e.printStackTrace();
        }
    }

    private  byte[] compressAndConvertImageToByteFrom(Bitmap bitmap) {
        // 4 for 32 bit images
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        // PNG is lossless and ignores quality setting parameter (0)
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayBitmapStream);
        byte[] data = byteArrayBitmapStream.toByteArray();
        return data;
    }


    private void updateViewsWithProfileInfo() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser.get("profile") != null) {
            JSONObject userProfile = currentUser.getJSONObject("profile");
            try {
                if (userProfile.getString("name") != null) {

                    userNameView.setText("Hello, " + userProfile.getString("name"));
                } else {
                    userNameView.setText("No user.");
                }
            } catch (JSONException e) {
                Log.d(ClarpApplication.TAG, "Error parsing saved user data.");
            }

        }
        updateViewVisibility();
    }

    private void updateViewVisibility()
    {
        if(ClarpApplication.IS_LOGGED_IN)
        {
            loginButton.setVisibility(View.GONE);

            if(infoUpdated)
            {
                newGameButton.setVisibility(View.VISIBLE);
            }
            else
            {
                newGameButton.setVisibility(View.VISIBLE);
            }


            if(gamesLoaded)
            {
                listLoadingView.setVisibility(View.GONE);
                gameListView.setVisibility(View.VISIBLE);


            }
            else
            {
                listLoadingView.setVisibility(View.VISIBLE);
                gameListView.setVisibility(View.GONE);
            }


        }
        else
        {
            gameListView.setVisibility(View.GONE);
            newGameButton.setVisibility(View.GONE);
            listLoadingView.setVisibility(View.GONE);

            loginButton.setVisibility(View.VISIBLE);
        }


    }

    // this is just here to test the picture taking/card adding
    // system, without having cards linked to games
    //    public void clickCardsList(View v) {
    //        Intent intent = new Intent(StartActivity.this, CardListActivity.class);
    //        startActivityForResult(intent, ClarpApplication.ADD_CARD);
    //
    //    }


    public class GameAdapter extends ArrayAdapter<ClarpGame> {

        private final Context context;
        private final ArrayList<ClarpGame> games;

        public GameAdapter(Context context, ArrayList<ClarpGame> games) {
            super(context, R.layout.game_item, games);
            this.context = context;
            this.games = games;
        }

        private class ViewHolder {
            TextView leftView;
            TextView rightView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;

            if (convertView == null)
            {

                LayoutInflater vi = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.game_item, null);

                holder = new ViewHolder();
                holder.leftView = (TextView) convertView.findViewById(R.id.gameName);
                holder.rightView = (TextView) convertView.findViewById(R.id.playerTurn);
                convertView.setTag(holder);


            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }




            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);



            holder.leftView.setText(games.get(position).getGameName());
            
            /*
             * Get whose turn it is
             */
            
            String turnName = null;
            String turnId = games.get(position).getString("whoseTurn");
            String myId = null;
            try {
				myId = ParseUser.getCurrentUser().getJSONObject("profile").getString("facebookId");
			} catch (JSONException e1) {
				Log.d(ClarpApplication.TAG, "SA: failed to get current player's facebookId");
				e1.printStackTrace();
			}
            if (turnId == null)
            {
            	Log.d(ClarpApplication.TAG, "SA: " + games.get(position).getGameName() + " whoseTurn is empty");
            	turnId = "";
            }
            JSONArray players = games.get(position).getJSONArray("players");
            Boolean isStarted = games.get(position).getBoolean("isStarted");
            
            for (int i = 0; i < players.length(); ++i)
            {
            	String id = null;
            	
            	try {
					id = ((JSONObject)players.get(i)).getString("facebookId");
				} catch (JSONException e) {
					Log.d(ClarpApplication.TAG, "SA: couldn't get game's player's facebookId");
					e.printStackTrace();
				}
            	
            	if (turnId.equals(id))
            	{
            		try {
						turnName = ((JSONObject)players.get(i)).getString("prefix") + " " + ((JSONObject)players.get(i)).getString("name");
					} catch (JSONException e) {
						Log.d(ClarpApplication.TAG, "SA: couldn't get game's player's prefix and name");
						e.printStackTrace();
					}
            	}
            }
            
            if(isStarted)
            {
            	if(turnId.equals(myId))
            	{
            		holder.rightView.setText("Your turn!");
            	}
            	else if (turnName != null)
                {
                	holder.rightView.setText(turnName + "'s turn");
                }
                else
                {
                	holder.rightView.setText("TURN ERROR");
                }
            }
            else
            {
            	holder.rightView.setText("Gathering Evidence");
            }
            
            



            return convertView;
        }
    }

}
