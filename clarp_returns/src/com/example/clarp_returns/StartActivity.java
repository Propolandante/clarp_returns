package com.example.clarp_returns;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import com.facebook.*;
import com.facebook.model.*;

import android.widget.TextView;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;

public class StartActivity extends ActionBarActivity {
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		
		//changes
		
		
//		try {
//	        PackageInfo info = getPackageManager().getPackageInfo(
//	                "com.example.clarp_returns", 
//	                PackageManager.GET_SIGNATURES);
//	        for (Signature signature : info.signatures) {
//	            MessageDigest md = MessageDigest.getInstance("SHA");
//	            md.update(signature.toByteArray());
//	            Log.d("Your Tag", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//	            }
//	    } catch (NameNotFoundException e) {
//	    	
//	    	Log.d("namewhatever","test");
//
//	    } catch (NoSuchAlgorithmException e) {
//	    	
//	    	Log.d("algorithmwhatever","test");
//
//	    }

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		// start Facebook Login
		Session.openActiveSession(this, true, new Session.StatusCallback() {
			
			// callback when session changes state
		    @Override
		    public void call(Session session, SessionState state, Exception exception) {
		    	
		    	if (session.isOpened()) {
		    		
		    		// make request to the /me API
		    		Request.newMeRequest(session, new Request.GraphUserCallback() {
		    			
		    			// callback after Graph API response with user object
		    			@Override
		    			public void onCompleted(GraphUser user, Response response) {
		    				
		    				if (user != null) {
		    					
		    					//do something
		    					
		    				}
		    				
		    			}
		    		}).executeAsync();
		    		
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
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_start,
					container, false);
			return rootView;
		}
	}

}
