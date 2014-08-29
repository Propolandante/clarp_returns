package com.example.clarp_returns;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class CardListActivity extends Activity {

    // result codes
    static final int NEW_CARD = 0;

    //private CardViewAdapter mainAdapter;
    //private ParseQueryAdapter<ClarpCard> mainAdapter;

    ListView cardListView;
    List<ParseObject> ob;
    ProgressDialog cardProgressDialog;
    CardListViewAdapter cardAdapter;
    private List<ClarpCard> cardList = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //mainAdapter = new CardViewAdapter(this);
        //mainAdapter = new ParseQueryAdapter<ClarpCard>(this, ClarpCard.class);
        //setListAdapter(mainAdapter);

        setContentView(R.layout.activity_card_list);

        //new RemoteDataTask().execute();

        ParseQuery<ClarpCard> query = ParseQuery.getQuery("ClarpCard");
        query.findInBackground(new FindCallback<ClarpCard>() {
            @Override
            public void done(List<ClarpCard> objects, ParseException e) {
                if (e == null) {
                    //objectsWereRetrievedSuccessfully(objects);
                    for(ClarpCard card: objects) {
                        ParseFile image = card.getPhotoFile();

                        ClarpCard clarpCard = new ClarpCard();
                        clarpCard.setCardType(Integer.toString(0)); // will need to change
                        clarpCard.setCardName(card.getCardName());
                        clarpCard.setPhotoFile(image);

                        cardList.add(clarpCard);
                    }

                } else {
                    //objectRetrievalFailed();
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                    Log.d(ClarpApplication.TAG, "Card retrieval failed");
                }
            }
        });


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
        //mainAdapter.loadObjects();
        Log.v(ClarpApplication.TAG, "Card objects loaded");
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
            Log.v(ClarpApplication.TAG, "Returned from NewCard");
            Log.v(ClarpApplication.TAG, "Card List updated");
        }
    }

    private void startTask() {
        cardProgressDialog = new ProgressDialog(CardListActivity.this);
        cardProgressDialog.setTitle("Card ListView");

        cardProgressDialog.setMessage("Loading...");
        cardProgressDialog.setIndeterminate(false);
        cardProgressDialog.show();
    }

    private void endTask() {
        cardListView = (ListView) findViewById(R.id.card_list_view);
        cardAdapter = new CardListViewAdapter(CardListActivity.this, cardList);
        cardListView.setAdapter(cardAdapter);
        cardProgressDialog.dismiss();
    }

    //    private class RemoteDataTask extends AsyncTask<Void, Void, Void> {
    //        @Override
    //        protected void onPreExecute() {
    //            super.onPreExecute();
    //            cardProgressDialog = new ProgressDialog(CardListActivity.this);
    //            cardProgressDialog.setTitle("Card ListView");
    //
    //            cardProgressDialog.setMessage("Loading...");
    //            cardProgressDialog.setIndeterminate(false);
    //            cardProgressDialog.show();
    //        }
    //
    //
    //        @Override
    //        protected Void doInBackground(Void... params) {
    //            // TODO Auto-generated method stub
    //            return null;
    //
    //
    //
    //        }
    //    }
}





