package com.example.clarp_returns;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;

public class ClarpApplication extends Application {

    /* Static Attributes */

    // Tag used when logging all messages with the same tag (e.g. for demoing purposes)
    static final String TAG = "Clarp";

    /* Will probably need some game constants here */

    public static boolean IS_LOGGED_IN = false;

    public static int MIN_REQ_USERS = 3;

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(ClarpGame.class);
        ParseObject.registerSubclass(ClarpCard.class);
        Parse.initialize(this, getString(R.string.parse_app_id),
                getString(R.string.parse_client_key));

        // Set your Facebook App Id in strings.xml
        ParseFacebookUtils.initialize(getString(R.string.app_id));

        //Log.v(TAG, "Application Class works...?");
    }


}
