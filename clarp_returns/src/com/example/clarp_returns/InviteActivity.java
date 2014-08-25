package com.example.clarp_returns;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class InviteActivity extends ActionBarActivity
{
	
	/*
	 * This Activity happens after the user has named and created their game,
	 * and before they are taken to PreGameActivity. However, we should allow
	 * the user to go back to this screen to invite more players after the initial
	 * invitations are sent.
	 * 
	 * This screen will consist of a ListView of all the user's friends who use Clarp.
	 * Hopefully this will include each friend's profile picture as well as their name.
	 * The user can then check the users they wish to invite, and then press the Invite button
	 * Once the button is pressed, the user is taken to the PreGameActivity.
	 */
	
	private ListView friendListView;
    private ArrayList<GraphUser> friendList;
    private FriendAdapter friendAdapter;
    private Button inviteButton;
	
	ParseUser user;
	ClarpGame game;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invite);
		
		friendListView = (ListView) findViewById(R.id.friend_list_view);
		inviteButton = (Button) findViewById(R.id.inviteButton);
		
		/*
		 * We can't set the button's OnClickListener until game is downloaded
		 */
		
		Intent mainIntent = getIntent();
        ParseQuery<ClarpGame> query = ParseQuery.getQuery("ClarpGame");
        query.getInBackground(mainIntent.getStringExtra("game_id"), new GetCallback<ClarpGame>() {
            @Override
            public void done(ClarpGame object, ParseException e) {
                if (e == null)
                {
                    game = object;
                    
                    /*
                     * Now we can set the button's onClickListener
                     */
                    
                    inviteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        	
                        	/*
                        	 * Takes user to the game activity.
                        	 * 
                        	 */
                        	
                        	Intent intent = new Intent(InviteActivity.this, PreGameActivity.class);
                            intent.putExtra("game_id", game.getObjectId());
                            startActivity(intent);
                        }
                    });
                    
                }
                else
                {
                    Log.d(ClarpApplication.TAG, "Something went wrong when querying the ClarpGame in PGA");
                }
            }
        });
		
		
		user = ParseUser.getCurrentUser();
		
		if (user != null)
		{
			makeMyFriendsRequest();
		}
		else
		{
			Log.d(ClarpApplication.TAG, "Uhhh user is null. in InviteActivity. The implications of this are not good.");
		}
	}
	
	@Override
    public void onResume(){
		
		super.onResume();
	}
	
	private void makeMyFriendsRequest() {
		Request request = Request.newMyFriendsRequest(ParseFacebookUtils.getSession(), new Request.GraphUserListCallback() {
            @Override
            public void onCompleted(List<GraphUser> userList, Response response) {
            	
            	/*
            	 *First, load the List of Graph users into the ArrayList
            	 *Then, do that... weird... ArrayAdapter thingy and make the ListView work
            	 */
            	friendList = new ArrayList<GraphUser>();
            	
            	for(int i = 0; i < userList.size(); ++i)
            	{
            		friendList.add(userList.get(i));
            		Log.d(ClarpApplication.TAG, friendList.get(i).getName());
            	}
            	
            	
                friendAdapter = new FriendAdapter(getApplicationContext(), friendList);
                friendListView.setAdapter(friendAdapter);
                
            }
        });
        request.executeAsync();

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
    
    public class FriendAdapter extends ArrayAdapter<GraphUser> {
    	
    	/*
    	 * Sources:
    	 * http://www.vogella.com/tutorials/AndroidListView/article.html#adapterown_custom
    	 * http://www.mysamplecode.com/2012/07/android-listview-checkbox-example.html
    	 */
    	
    	private final Context context;
    	private final ArrayList<GraphUser> friends;
    	
    	public FriendAdapter(Context context, ArrayList<GraphUser> friends) {
    		super(context, R.layout.friend_item, friends);
    	    this.context = context;
    	    this.friends = friends;
    	  }
    	
    	private class ViewHolder {
    		ImageView image;
    		TextView text;
    		CheckBox box;
    		}
    	
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		
    		ViewHolder holder = null;
    		
    		if (convertView == null)
    		{
    			
    			LayoutInflater vi = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE);
    			convertView = vi.inflate(R.layout.friend_item, null);
    			
    			holder = new ViewHolder();
        		holder.image = (ImageView) convertView.findViewById(R.id.friendPic);
        		holder.text = (TextView) convertView.findViewById(R.id.friendName);
        		holder.box = (CheckBox) convertView.findViewById(R.id.checkBox);
        		convertView.setTag(holder);
        		
        		holder.box.setOnClickListener(new View.OnClickListener() {
        			
        			public void onClick(View v) {
        				CheckBox cb = (CheckBox) v;
        				
        				GraphUser user = (GraphUser) cb.getTag();
        				
        				Log.d(ClarpApplication.TAG, "Clicked on " + user.getName());
        				
        			}
        			
        		});
    		}
    		else
    		{
    			holder = (ViewHolder) convertView.getTag();
    		}
    		
    		
    		
    		
    		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	    
    	    
    	    GraphUser friend = friends.get(position);
    	    
    	    // holder.image.setImageResource(something?);
    	    holder.text.setText(friends.get(position).getName());
    	    holder.box.setTag(friend);
    	    
    	    

    	    return convertView;
    	  }
    	} 
}