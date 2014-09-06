package com.example.clarp_returns;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;


public class CardListActivity extends Activity {

    // result codes
    static final int NEW_CARD = 0;

    private ListView cardListView;
    private CardQueryAdapter cardAdapter;
    private ProgressBar listLoadingView;
    private boolean cardsLoaded = false;
    private String gameId;
    private int requestCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_card_list);

        Intent mainIntent = getIntent();

        Bundle extras = mainIntent.getExtras();
        if(extras != null) {
            gameId = extras.getString("game_id");
            requestCode = extras.getInt("requestCode");
            cardAdapter = new CardQueryAdapter(CardListActivity.this, requestCode, gameId);
        }



        cardListView = (ListView) findViewById(R.id.card_list_view);
        listLoadingView = (ProgressBar) findViewById(R.id.progressBar1);

        cardListView.setAdapter(cardAdapter);
        updateCardList();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.card_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id) {

            case R.id.action_settings: {
                break;
            }

            case R.id.action_refresh: {
                updateCardList();
                break;
            }

            case R.id.action_new: {
                newClarpCard();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateCardList() {
        updateViewVisibility();
        cardsLoaded = false;
        cardAdapter.loadObjects();
        Log.v(ClarpApplication.TAG, "Card objects loaded");
        cardListView.setAdapter(cardAdapter);
        cardsLoaded = true;
        updateViewVisibility();
    }

    private void newClarpCard() {
        Intent i = new Intent(this, NewClarpCardActivity.class);
        startActivityForResult(i, NEW_CARD);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == Activity.RESULT_OK) {
            // if there's a new card,
            // update the list of current cards
            updateCardList();
            Log.v(ClarpApplication.TAG, "Returned from NewCard");
            Log.v(ClarpApplication.TAG, "Card List updated");
        }
    }

    private void updateViewVisibility()
    {
        if(cardsLoaded)
        {
            listLoadingView.setVisibility(View.GONE);
            cardListView.setVisibility(View.VISIBLE);


        }
        else
        {
            listLoadingView.setVisibility(View.VISIBLE);
            cardListView.setVisibility(View.GONE);
        }
    }
}





