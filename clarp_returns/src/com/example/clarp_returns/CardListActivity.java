package com.example.clarp_returns;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;


public class CardListActivity extends Activity {

    // result codes
    static final int NEW_CARD = 0;

    private ListView cardListView;
    private List<ClarpCard> cardList;
    private CardQueryAdapter cardAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_card_list);

        cardListView = (ListView) findViewById(R.id.card_list_view);

        cardList = new ArrayList<ClarpCard>();

        cardAdapter = new CardQueryAdapter(CardListActivity.this);
        cardListView.setAdapter(cardAdapter); // this apparently causes a NPE
        //cardAdapter.loadObjects();
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
        cardAdapter.loadObjects();
        Log.v(ClarpApplication.TAG, "Card objects loaded");
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
}





