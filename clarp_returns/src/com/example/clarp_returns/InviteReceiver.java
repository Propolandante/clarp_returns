package com.example.clarp_returns;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class InviteReceiver extends BroadcastReceiver{
    private static final String TAG = "InviteReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            //String channel = intent.getExtras().getString("com.parse.Channel");
            JSONObject data = new JSONObject(intent.getExtras().getString("com.parse.Data"));

            Log.d(TAG, "got action " + action + " with:");
            Iterator itr = data.keys();
            while (itr.hasNext()) {
                String key = (String) itr.next();
                Log.d(TAG, "..." + key + " => " + data.getString(key));
            }


            Intent pushIntent = new Intent();
            pushIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(data.has("gameId")) {
                pushIntent.setClass(context, PreGameActivity.class);
                pushIntent.putExtra("game_id", data.getString("gameId"));
                context.startActivity(pushIntent);
            } else {
                pushIntent.setClass(context, StartActivity.class);
                context.startActivity(pushIntent);
                Log.d(TAG, "Error: JSONObject contains no gameId");
            }

        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
    }
}
