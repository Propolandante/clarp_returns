package com.example.clarp_returns;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class CardListActivity extends ListActivity {

    // result codes
    static final int NEW_CARD = 0;


    private CardViewAdapter mainAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_card_list);

        mainAdapter = new CardViewAdapter(this);
        setListAdapter(mainAdapter);

        //        if (savedInstanceState == null) {
        //            getSupportFragmentManager().beginTransaction()
        //            .add(R.id.container, new PlaceholderFragment()).commit();
        //        }
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
        mainAdapter.loadObjects();
        //setListAdapter(mainAdapter);
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
        }
    }

}
