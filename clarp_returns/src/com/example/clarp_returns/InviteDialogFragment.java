package com.example.clarp_returns;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParseUser;

public class InviteDialogFragment extends DialogFragment{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        Bundle args = getArguments();
        final String gameId = args.getString("game_id");
        final String userId = args.getString("user_id");

        ParseUser user = ParseUser.getCurrentUser();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_accept_invite)
        .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // FIRE ZE MISSILES!
                Log.d(ClarpApplication.TAG, "User " + userId + " accepted invitation to game " + gameId);
                //ClarpGame.addPlayer(user);
                // return a code and add player in PGA

            }
        })
        .setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                Log.d(ClarpApplication.TAG, "User " + userId + " declined invitation to game " + gameId);
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
