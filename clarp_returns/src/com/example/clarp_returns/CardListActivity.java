package com.example.clarp_returns;

import java.util.ArrayList;
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
import com.parse.GetDataCallback;
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

        cardListView = (ListView) findViewById(R.id.card_list_view);

        //new RemoteDataTask().execute();
        cardList = new ArrayList<ClarpCard>();
        cardAdapter = new CardListViewAdapter(CardListActivity.this, cardList);
        cardListView.setAdapter(cardAdapter); // this apparently causes a NPE


        ParseQuery<ClarpCard> query = ParseQuery.getQuery("ClarpCard");
        query.findInBackground(new FindCallback<ClarpCard>() {
            @Override
            public void done(List<ClarpCard> objects, ParseException e) {
                Log.d(ClarpApplication.TAG, "Trying card retrieval");
                if (e == null) {

                    Log.d(ClarpApplication.TAG, "Card retrieval successful");

                    for(ClarpCard card: objects) {

                        ParseFile image = (ParseFile) card.get("photo");

                        ClarpCard clarpCard = new ClarpCard();
                        clarpCard.setCardType(Integer.toString(0)); // will need to change
                        clarpCard.setCardName((String) card.get("cardName"));

                        if(image != null) {

                            Log.d(ClarpApplication.TAG, "Image is not null");
                            image.getDataInBackground(new GetDataCallback() {
                                @Override
                                public void done(byte[] data, ParseException e) {

                                    if (e == null) {
                                        Log.d(ClarpApplication.TAG, "We've got data in data.");
                                    } else {
                                        Log.d(ClarpApplication.TAG, "There was a problem downloading data");
                                    }
                                }
                            });

                            clarpCard.setPhotoFile(image);
                            cardList.add(clarpCard);

                        } else {

                            Log.d(ClarpApplication.TAG, "Image is null"); // this happens a lot
                            //clarpCard.setPhotoFile((ParseFile) getResources().getDrawable(R.drawable.jordan));
                        }
                    }
                } else {

                    Log.d(ClarpApplication.TAG, "Card retrieval failed");
                    //objectRetrievalFailed();
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        cardAdapter.notifyDataSetChanged();

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





