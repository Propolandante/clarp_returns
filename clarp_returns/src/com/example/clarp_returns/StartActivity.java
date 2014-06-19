package com.example.clarp_returns;

import java.util.ArrayList;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class StartActivity extends ActionBarActivity {


    protected static final String TAG = "StartActivity";
    private ListView gamesListView;
    private ArrayList<Game> gamesList;
    private ArrayAdapter<Game> arrayAdapter;


    //Facebook Stuff
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);



        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
            .add(R.id.container, new PlaceholderFragment()).commit();
        }

        Parse.initialize(this, "tyhQ2ZPLI2zh7QTAncbsB0dPtjhUEqm4XhYu77ad", "BE6x59n7NymJUeBWxxIztCVkzLSnPxsSgQ2MecEE");



        // start Facebook Login
        Session.openActiveSession(this, true, new Session.StatusCallback() {

            // callback when session changes state
            @Override
            public void call(final Session session, SessionState state, Exception exception) {

                if (session.isOpened()) {

                    // make request to the /me API
                    Request.newMeRequest(session, new Request.GraphUserCallback() {

                        // callback after Graph API response with user object
                        @Override
                        public void onCompleted(GraphUser user, Response response) {

                            if (user != null) {
                                FacebookRequestError error = response.getError();
                                if (error != null) {
                                    Log.e(ClarpApplication.TAG, error.toString());
                                    //handleError(error, true);
                                } else if (session == Session.getActiveSession()) {
                                    // Set the currentFBUser attribute
                                    ((ClarpApplication)getApplication()).setCurrentFBUser(user);

                                    // Now save the user into Parse.
                                    saveUserToParse(user, session);
                                }
                            }

                        }
                    }).executeAsync();

                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        gamesListView = (ListView) findViewById(R.id.games_list_view);
        //gamesListView.setEmptyView(findViewById(R.id.empty_games_view));

        gamesListView.setOnItemClickListener(new OnItemClickListener() {

            // user clicks to go to a select screen where they can choose
            // to view any list
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                Intent intent = new Intent(StartActivity.this,
                        GameActivity.class);
                Game game = gamesList.get((int) id);
                intent.putExtra("game_id", game.getId());
                startActivity(intent);

            }
        });

        Game myGame = new Game("MyGame", 0, null, null);

        gamesList = new ArrayList<Game>();
        //gamesList.add(0, myGame);

        arrayAdapter = new ArrayAdapter<Game>(this,
                android.R.layout.simple_list_item_1, gamesList);
        gamesListView.setAdapter(arrayAdapter);
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
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_start,
                    container, false);
            return rootView;
        }
    }

    public void clickNewGame(View v) {
        Intent intent = new Intent(StartActivity.this, NewGameActivity.class);
        startActivity(intent);
    }


    private void saveUserToParse(GraphUser fbUser, Session session) {
        ParseFacebookUtils.logIn(fbUser.getId(), session.getAccessToken(),
                session.getExpirationDate(), new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user == null) {
                    // The user wasn't saved. Check the exception.
                    Log.d(TAG, "User was not saved to Parse: " + err.getMessage());
                } else {
                    // The user has been saved to Parse.
                    Log.d(TAG, "User has successfully been saved to Parse.");
                }
            }
        });
    }

}
