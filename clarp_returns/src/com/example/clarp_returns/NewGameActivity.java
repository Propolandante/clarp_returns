package com.example.clarp_returns;

import org.json.JSONException;

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
import android.widget.EditText;
import android.widget.LinearLayout;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class NewGameActivity extends ActionBarActivity {



    static EditText gameNameTextField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        //View addCardsButton = findViewById(R.id.add_cards_button);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
            .add(R.id.container, new PlaceholderFragment()).commit();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_game, menu);
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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            if (container == null) {
                return null;
            }

            LinearLayout ll = (LinearLayout )inflater.inflate(R.layout.fragment_new_game, container, false);
            gameNameTextField = (EditText) ll.findViewById(R.id.editTitle);

            return ll;
        }
    }

    public void clickStart(View v) throws JSONException, ParseException {


        ParseUser user = ParseUser.getCurrentUser();

        Log.d(ClarpApplication.TAG, "Start clicked");

        final ClarpGame game = new ClarpGame();

        //set game name
        String gameName = gameNameTextField.getText().toString();
        if (gameName.matches(""))
        {
            game.setGameName("Untitled Game");
            Log.d(ClarpApplication.TAG, "gameName set to " + "Untitled Game");
        } else
        {
            game.setGameName(gameName);
            Log.d(ClarpApplication.TAG, "gameName set to " + gameName);
        }

        game.setOwner(user);

        //create necessary data structures
        game.initialize();
        Log.d(ClarpApplication.TAG, "initialization complete");

        //Add user to game's player list
        game.addPlayer(user);
        Log.d(ClarpApplication.TAG, "player added");

        //save this information to the Parse Object online
        game.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null)
                {
                    Log.d(ClarpApplication.TAG, "Game saved");

                    Intent intent = new Intent(NewGameActivity.this, InviteActivity.class);
                    intent.putExtra("game_id", game.getObjectId());
                    startActivity(intent);
                    // finishes the activity so that user cannot get back
                    finish();
                }
                else
                {
                    Log.d(ClarpApplication.TAG, "Error saving new ClarpGame");
                    finish();
                }
            }
        });
    }
}
