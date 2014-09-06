package com.example.clarp_returns;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class InviteReceiver extends BroadcastReceiver{
    private static final String TAG = "InviteReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String header;
            String message;
            String gameId;
            String action = intent.getAction();
            JSONObject data = new JSONObject(intent.getExtras().getString("com.parse.Data"));

            if(data.has("header") && data.has("message") && data.has("gameId") ) {
                header = data.getString("header");
                message = data.getString("message");
                gameId = data.getString("gameId");
                Log.d(ClarpApplication.TAG, "Notification data received: \n"
                        + "header: " + header
                        + "\nmessage: " + message
                        + "\ngameId: " + gameId);
                generateNotification(context, header, message, gameId);
            } else {
                Log.d(TAG, "JSON data doesn't have all keys");
            }

        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
    }

    public static void generateNotification(Context context, String header, String message, String gameId) {
        long when = System.currentTimeMillis();
        Intent notificationIntent = new Intent(context, PreGameActivity.class);
        notificationIntent.putExtra("game_id", gameId);
        // provides check for whether PGA was opened via notification or not
        notificationIntent.putExtra("notification", ClarpApplication.NOTIFICATION);
        // prevent intent from starting activity immediately
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.spoon, message, when);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notification.setLatestEventInfo(context, header, message, intent);
        // sets vibrate, sound, lights to default alert settings
        notification.defaults = Notification.DEFAULT_ALL;

        notification.flags =
                Notification.FLAG_AUTO_CANCEL | // dismiss upon user click
                Notification.FLAG_SHOW_LIGHTS;

        notificationManager.notify(0, notification);
    }
}
