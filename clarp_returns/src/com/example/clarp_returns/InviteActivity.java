package com.example.clarp_returns;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

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

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SendCallback;

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
    private ArrayList<Friend> friendList;
    private FriendAdapter friendAdapter;
    private Button inviteButton;

    ParseUser user;
    String userName;
    ClarpGame game;
    String gameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        friendListView = (ListView) findViewById(R.id.friend_list_view);
        inviteButton = (Button) findViewById(R.id.inviteButton);

        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser.get("profile") != null) {
            JSONObject userProfile = currentUser.getJSONObject("profile");
            try {
                if (userProfile.getString("name") != null) {
                    //Log.v(ClarpApplication.TAG, "IF");
                    userName = userProfile.getString("name");
                }
            } catch (JSONException e) {
                Log.d(ClarpApplication.TAG, "Error getting user's profile name");
                Log.e("Error", "Error message: " + e.getMessage());
                e.printStackTrace();
            }
        }

        /*
         * We can't set the button's OnClickListener until game is downloaded
         */

        Intent mainIntent = getIntent();
        gameId = mainIntent.getStringExtra("game_id");
        ParseQuery<ClarpGame> query = ParseQuery.getQuery("ClarpGame");
        query.getInBackground(gameId, new GetCallback<ClarpGame>() {
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
                             * Send push notifications to the invited users
                             */

                            for (int i = 0; i < friendList.size(); i++)
                            {
                                /*
                                 * If the friend's box was checked when the button was pressed...
                                 */

                                if(friendList.get(i).isChecked())
                                {
                                    // Create our Installation query
                                    ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
                                    pushQuery.whereEqualTo("facebookId", friendList.get(i).getId());

                                    JSONObject data = new JSONObject();
                                    try{
                                        data.put("action", "com.example.clarp_returns.INVITE");
                                        data.put("title", "ARE YOU READY TO CLARP???");
                                        data.put("alert", userName + " has invited you to a game of Clarp!");
                                        data.put("gameId", gameId);
                                    } catch (JSONException e) {
                                        Log.d(ClarpApplication.TAG, "Error putting JSON data");
                                        Log.e("Error", "Error Message: " + e.getMessage());
                                        e.printStackTrace();
                                    }


                                    // Send push notification to query
                                    ParsePush push = new ParsePush();
                                    push.setQuery(pushQuery); // Set our Installation query
                                    push.setMessage("ARE YOU READY TO CLARP???");
                                    push.setData(data);

                                    PushService.setDefaultPushCallback(InviteActivity.this, CardListActivity.class);
                                    push.sendInBackground(new SendCallback() {

                                        @Override
                                        public void done(ParseException e)
                                        {
                                            if (e == null)
                                            {
                                                Log.d(ClarpApplication.TAG, "Sent push notification");
                                            }
                                            else
                                            {
                                                Log.d(ClarpApplication.TAG, "Push notification NOT sent");
                                            }
                                        }
                                    });
                                }

                            }



                            /*
                             * Takes user to the pregame activity.
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
                 *First, load the List of Graph users into the ArrayList of Freinds
                 *NOTE: Friend is a custom object type for this Activity (see class below)
                 *Then, do that... weird... ArrayAdapter thingy and make the ListView work
                 */
                friendList = new ArrayList<Friend>();

                for(int i = 0; i < userList.size(); ++i)
                {

                    /*
                     * Copy each GraphUser as a Friend object into the Array
                     */

                    friendList.add(new Friend(userList.get(i)));
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

    public class FriendAdapter extends ArrayAdapter<Friend> {

        /*
         * Sources:
         * http://www.vogella.com/tutorials/AndroidListView/article.html#adapterown_custom
         * http://www.mysamplecode.com/2012/07/android-listview-checkbox-example.html
         */

        private final Context context;
        private final ArrayList<Friend> friends;

        public FriendAdapter(Context context, ArrayList<Friend> friends) {
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

                    @Override
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;

                        /*
                         * If the checkbox is clicked, select/deselect the Friend
                         */

                        if (((Friend) cb.getTag()).isChecked())
                        {
                            ((Friend) cb.getTag()).deselect();
                        }
                        else
                        {
                            ((Friend) cb.getTag()).select();
                        }

                        Log.d(ClarpApplication.TAG, ((Friend) cb.getTag()).getName() + " checked: " + ((Friend) cb.getTag()).isChecked());

                    }

                });
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }




            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


            Friend friend = friends.get(position);

            // holder.image.setImageResource(something?);
            holder.text.setText(friends.get(position).getName());
            holder.box.setTag(friend);



            return convertView;
        }
    }

    private class Friend
    {
        private String facebookId;
        private String name;
        private Boolean isChecked;

        Friend(GraphUser user)
        {
            this.facebookId = user.getId();
            this.name = user.getName();
            this.isChecked = false;
        }

        String getId()
        {
            return this.facebookId;
        }

        String getName()
        {
            return this.name;
        }

        Boolean isChecked()
        {
            return this.isChecked;
        }

        void select()
        {
            this.isChecked = true;
        }

        void deselect()
        {
            this.isChecked = false;
        }
    }


















}