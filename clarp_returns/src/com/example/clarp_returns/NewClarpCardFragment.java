package com.example.clarp_returns;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.SaveCallback;


/*
 * This fragment manages the data entry for a
 * new ClarpCard object. It lets the user input a
 * card name, give it a type, and take a
 * photo. If there is already a photo associated
 * with this card, it will be displayed in the
 * preview at the bottom, which is a standalone
 * ParseImageView. (we'll probably have to change
 * that, as well as the user giving the card a type,
 * but that seemed easiest for now)
 */
public class NewClarpCardFragment extends Fragment {

    private ImageButton photoButton;
    private Button saveButton;
    private Button cancelButton;
    private TextView cardName;
    private Spinner cardType;
    private ParseImageView cardPreview;

    ClarpGame game;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        ParseQuery<ClarpGame> query = ParseQuery.getQuery("ClarpGame");
        query.getInBackground(((NewClarpCardActivity) getActivity()).getGameId(), new GetCallback<ClarpGame>() {
            @Override
            public void done(ClarpGame object, ParseException e) {
                if (e == null)
                {
                    game = object;
                }
                else
                {
                    Log.d(ClarpApplication.CF, "Something went wrong when querying the ClarpGame in NCCF");
                }
            }
        });
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle SavedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_clarp_card_fragment, parent, false);

        cardName = ((EditText) v.findViewById(R.id.card_name));


        // i commented this out because i thought it would solve my problems
        // but alas
        // it did nothing of the sort

        cardType = ((Spinner) v.findViewById(R.id.type_spinner));
        ArrayAdapter<ClarpCard.CardType> spinnerAdapter = new ArrayAdapter<ClarpCard.CardType>
        (getActivity(), android.R.layout.simple_spinner_dropdown_item,
                ClarpCard.CardType.values());
        cardType.setAdapter(spinnerAdapter);

        photoButton = ((ImageButton) v.findViewById(R.id.photo_button));
        photoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(cardName.getWindowToken(), 0);
                startCamera();
            }
        });

        saveButton = ((Button) v.findViewById(R.id.save_button));
        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final ClarpCard card = ((NewClarpCardActivity) getActivity()).getCurrentClarpCard();
                
                /*
                 * Check to see if the user has provided a picture and title for this card first!
                 */
                
                if (card.getPhotoFile() == null)
                {
                	Toast.makeText((NewClarpCardActivity) getActivity(), "Please provide a picture for this " + cardType.getSelectedItem().toString(), Toast.LENGTH_LONG).show();
                	return;
                }
                
                if (cardName.getText().toString().equals(""))
                {
                	Toast.makeText((NewClarpCardActivity) getActivity(), "Please provide a name for this " + cardType.getSelectedItem().toString(), Toast.LENGTH_LONG).show();
                	return;
                }

                // When the user clicks "Save," upload the card to Parse
                // Add data to the card object:


                // Add the type
                String selectedString = cardType.getSelectedItem().toString();
                Log.d(ClarpApplication.CF, "Type of selected cardType is " + selectedString);
                card.setCardType(ClarpCard.CardType.fromString(selectedString));

                // must set name after type in case type is suspect
                if(card.getCardType() == ClarpCard.CardType.SUSPECT){
                    card.setCardName(game.getSuspectPrefix() + " " + cardName.getText().toString());
                } else {
                    card.setCardName(cardName.getText().toString());
                }

                // Add the gameId so we know to query it in Game Activity
                card.setCardGame(((NewClarpCardActivity) getActivity()).getGameId());

                // If the user added a photo, that data will be
                // added in the CameraFragment

                // Save the card and return
                card.saveInBackground(new SaveCallback() {

                    @Override
                    public void done(ParseException e) {
                        if (e == null)
                        {

                            Log.d(ClarpApplication.CF, "Created new card: " + card.getObjectId());

                            ClarpCard.CardType type = card.getCardType();

                            switch(type) {
                                case SUSPECT:
                                    game.increment("numSuspects");
                                    break;
                                case WEAPON:
                                    game.increment("numWeapons");
                                    break;
                                case LOCATION:
                                    game.increment("numLocations");
                                    break;
                                default:
                                    Log.d(ClarpApplication.TAG, "Card is not of a valid type, ERROR ERROR ERROR!");
                                    break;
                            }

                            game.saveInBackground(new SaveCallback() {

                                @Override
                                public void done(ParseException e) {
                                    Log.d(ClarpApplication.CF, "Updated cardcount saved to parse");

                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("cardId", card.getObjectId());

                                    getActivity().setResult(Activity.RESULT_OK, resultIntent);
                                    getActivity().finish();

                                }

                            });


                        }
                        else
                        {
                            Toast.makeText( getActivity().getApplicationContext(), "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT ).show();
                        }
                    }

                });

            }
        });

        cancelButton = ((Button) v.findViewById(R.id.cancel_button));
        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivity().setResult(Activity.RESULT_CANCELED);
                getActivity().finish();
            }
        });

        // Until the user has taken a photo, hide the preview
        cardPreview = (ParseImageView) v.findViewById(R.id.card_preview_image);
        cardPreview.setVisibility(View.INVISIBLE);

        return v;
    }

    /*
     * All data entry about a ClarpCard object is managed from the NewClarpCardActivity.
     * When the user wants to add a photo, we'll start up a custom
     * CameraFragment that will let them take the photo and save it to the ClarpCard
     * object owned by the NewClarpCardActivity. Create a new CameraFragment, swap
     * the contents of the fragmentContainer (see activity_new_clarpcard.xml), then
     * add the NewClarpCardFragment to the back stack so we can return to it when the
     * camera is finished.
     */
    public void startCamera() {
        Fragment cameraFragment = new CameraFragment();
        FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, cameraFragment);
        transaction.addToBackStack("NewClarpCardFragment");
        transaction.commit();
    }

    /*
     * On resume, check and see if a card photo has been set from the
     * CameraFragment. If it has, load the image in this fragment and make the
     * preview image visible.
     */
    @Override
    public void onResume() {
        super.onResume();
        ParseFile photoFile = ((NewClarpCardActivity) getActivity()).getCurrentClarpCard().getPhotoFile();
        if (photoFile != null) 
        {
            cardPreview.setParseFile(photoFile);
            cardPreview.loadInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    cardPreview.setVisibility(View.VISIBLE);
                }
            });
        }
    }

}
