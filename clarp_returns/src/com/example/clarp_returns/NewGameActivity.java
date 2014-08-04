package com.example.clarp_returns;

import org.json.JSONException;

import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ParseQuery;
import com.parse.ParseException;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class NewGameActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_game);

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

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_new_game,
					container, false);
			return rootView;
		}
	}
	
	public void clickStart(View v) throws JSONException {
		
		Log.d(ClarpApplication.TAG, "Start clicked");
		
		final clarpGame game = new clarpGame();
		
		Log.d(ClarpApplication.TAG, "clarpGame game created");
		
		// Since our Instruments are strongly-typed, we can provide mutators that only take
		// specific types, such as Strings, ParseUsers, or enum types.
		game.setGameName("Suspect Test");
		Log.d(ClarpApplication.TAG, "gameName set");
		game.initialize();
		Log.d(ClarpApplication.TAG, "initialization complete");
		game.addPlayer(ParseUser.getCurrentUser());
		Log.d(ClarpApplication.TAG, "addPlayer() ohgod did it work?");
		game.saveInBackground(new SaveCallback() {
			public void done(ParseException e) {
				game.play();
				Log.d(ClarpApplication.TAG, "play();");
				}
			});
		
		//Intent intent = new Intent(getBaseContext(), GameActivity.class);
        //startActivity(intent);
	}

}
