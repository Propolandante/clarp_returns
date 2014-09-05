package com.example.clarp_returns;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.PushService;

public class ClarpApplication extends Application {

    /* Static Attributes */

    // Tag used when logging all messages with the same tag (e.g. for demoing purposes)
    static final String TAG = "Clarp";
    static final String PGA = "Clarp_PGA";
    static final String GA = "Clarp_GA";
    static final String CF = "Clarp_CF";

    /* Will probably need some game constants here */

    public static boolean IS_LOGGED_IN = false;

    public static int MIN_REQ_USERS = 3;

    // result codes for activities that return with a result
    public static final int NEW_GAME = 10;
    public static final int ADD_CARD = 11;
    public static final int VIEW_ALL_GAME_CARDS = 20;
    public static final int VIEW_USER_CARDS = 22;

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(ClarpGame.class);
        ParseObject.registerSubclass(ClarpCard.class);
        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));

        PushService.setDefaultPushCallback(this, StartActivity.class);

        // Set your Facebook App Id in strings.xml
        ParseFacebookUtils.initialize(getString(R.string.app_id));

        PushService.setDefaultPushCallback(this, PreGameActivity.class);

        //Log.v(TAG, "Application Class works...?");
    }


}
