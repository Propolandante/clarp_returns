package com.example.clarp_returns;

import org.json.JSONException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParseUser;

public class AccuseDialogFragment extends DialogFragment{


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
    	
    	// no arguments needed for this

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.accuse_dialog_text)
        .setPositiveButton(R.string.accuse, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                GameActivity callingActivity = (GameActivity) getActivity();
                try {
					callingActivity.makeWinningAccusation();
				} catch (JSONException e) {
					Log.d(ClarpApplication.TAG, "ADF: Failed to call makeWinningAccusation()!");
					e.printStackTrace();
				}
                dialog.dismiss();
            }
        })
        .setNegativeButton(R.string.pass, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                GameActivity callingActivity = (GameActivity) getActivity();
                try {
					callingActivity.whoLikesWinningAnyways();
				} catch (JSONException e) {
					Log.d(ClarpApplication.TAG, "ADF: Failed to call whoLikesWinningAnyways()!");
					e.printStackTrace();
				}
                dialog.dismiss();
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }


}
