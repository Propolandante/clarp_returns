package com.example.clarp_returns;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;


/*
 * NewClarpCardActivity contains two fragments that handle
 * data entry and capturing a photo of a given card.
 * The Activity manages the overall card data.
 */
public class NewClarpCardActivity extends Activity {

    private ClarpCard card;
    private String gameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        card = new ClarpCard();
        Intent mainIntent = getIntent();
        gameId = mainIntent.getStringExtra("game_id");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        // begin with main data entry view,
        // NewClarpCardFragment
        setContentView(R.layout.activity_new_clarp_card);
        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = new NewClarpCardFragment();
            manager.beginTransaction()
            .add(R.id.fragmentContainer, fragment).commit();
        }
    }


    public ClarpCard getCurrentClarpCard() {
        return card;
    }

    public String getGameId() {
        return gameId;
    }

}
