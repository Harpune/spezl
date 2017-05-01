package com.example.lukas.spezl.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lukas.spezl.R;
import com.example.lukas.spezl.controller.UserAdapter;
import com.example.lukas.spezl.model.Event;
import com.example.lukas.spezl.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class EventActivity extends AppCompatActivity {

    //TAGS.
    private final String TAG_EVENT_ID = "TAG_EVENT_ID";
    private final String TAG_EVENT_NAME = "TAG_EVENT_NAME";
    private final String TAG_DESCRIPTION = "TAG_DESCRIPTION";
    private final String TAG_MAX_PARTICIPANTS = "TAG_PARTICIPANTS";
    private final String TAG_AMOUNT_PARTICIPANTS = "TAG_AMOUNT_PARTICIPANTS";
    private final String TAG_EVENT_TOWN = "TAG_EVENT_TOWN";
    private final String TAG_EVENT_ADDRESS = "TAG_EVENT_ADDRESS";
    private final String TAG_EVENT_CATEGORY = "TAG_EVENT_CATEGORY";
    private final String TAG_OWNER_ID = "TAG_OWNER_ID";

    private final int REQUEST_CODE = 1;

    //The EventData.
    private String eventId, ownerId, eventCategory;
    private Integer eventMaxParticipants;
    private int eventAmountPaticipants;

    // Views.
    private TextView mDescriptionText, mDateText, mPlaceText, mParticipantsText, mNotificationTextView;
    private FloatingActionButton fab;
    // The Adapter of the RecyclerView responsible for the Users.
    private UserAdapter mUserAdapter;

    // List of the Users.
    private List<User> users = new ArrayList<>();

    // Global Database.
    private FirebaseDatabase mDatabase;

    private FirebaseUser fireUser;

    // Current event.
    private Event event = new Event();

    // Owner of the Event.
    private User user = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        // Get the Intent information from the MainActivity
        Intent intent = getIntent();
        eventId = intent.getStringExtra(TAG_EVENT_ID);
        String eventDescription = intent.getStringExtra(TAG_DESCRIPTION);
        Double maxParticipants = intent.getDoubleExtra(TAG_MAX_PARTICIPANTS, 0);
        eventAmountPaticipants = intent.getIntExtra(TAG_AMOUNT_PARTICIPANTS, 0);
        String eventTown = intent.getStringExtra(TAG_EVENT_TOWN);
        String eventAddress = intent.getStringExtra(TAG_EVENT_ADDRESS);
        String eventName = intent.getStringExtra(TAG_EVENT_NAME);
        eventCategory = intent.getStringExtra(TAG_EVENT_CATEGORY);
        ownerId = intent.getStringExtra(TAG_OWNER_ID);

        eventMaxParticipants = maxParticipants.intValue();

        fireUser = FirebaseAuth.getInstance().getCurrentUser();

        // Read the Event
        readEvent();

        // Setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(eventName);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }

        // Initialize the Views.
        mDateText = (TextView) findViewById(R.id.text_event_date);
        mPlaceText = (TextView) findViewById(R.id.text_event_place);
        mParticipantsText = (TextView) findViewById(R.id.text_event_participants);
        mDescriptionText = (TextView) findViewById(R.id.text_event_description);
        mNotificationTextView = (TextView) findViewById(R.id.notificationTextView);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        // Set the title and the Views with intent-information.
        mDescriptionText.setText(eventDescription);
        mParticipantsText.setText(eventAmountPaticipants + "/" + eventMaxParticipants + " Teilnehmer");
        if (eventAddress.equals("")) {
            mPlaceText.setText(eventTown);
        } else {
            mPlaceText.setText(eventTown + ", " + eventAddress);
        }
    }

    /**
     * Triggered when fab is clicked.
     *
     * @param view The fab-view.
     */
    public void joinEvent(final View view) {
        if (event.getParticipantIds().size() <= 1
                && event.getParticipantIds().values().toArray()[0].equals(fireUser.getUid())) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.pic_owl_active)
                    .setTitle("Event absagen")
                    .setMessage("Willst du wirklich das Event löschen?")
                    .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String key = getKeyByValue(event.getParticipantIds(), fireUser.getUid());
                            if (key != null) {
                                DatabaseReference mDatabaseRef = mDatabase.getReference("events")
                                        .child(eventCategory)
                                        .child(eventId);
                                mDatabaseRef.removeValue();
                                onBackPressed();
                            }
                        }
                    })
                    .setNegativeButton("Nein", null)
                    .show();
        } else if (userAlreadyParticipates()) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.pic_owl_active)
                    .setTitle("Event absagen")
                    .setMessage("Willst du wirklich dem Event nicht mehr teilnehemen?")
                    .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String key = getKeyByValue(event.getParticipantIds(), fireUser.getUid());
                            if (key != null) {
                                DatabaseReference mDatabaseRef = mDatabase.getReference("events")
                                        .child(eventCategory)
                                        .child(eventId)
                                        .child("participantIds")
                                        .child(key);
                                mDatabaseRef.removeValue();
                                onBackPressed();
                            }
                        }
                    })
                    .setNegativeButton("Nein", null)
                    .show();


        } else {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.pic_owl_active)
                    .setTitle("Du nimmst teil!")
                    .setMessage("Bist du dir sicher?")
                    .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            DatabaseReference mDatabaseRef = mDatabase.getReference("events")
                                    .child(eventCategory)
                                    .child(eventId)
                                    .child("participantIds")
                                    .push();
                            mDatabaseRef.setValue(fireUser.getUid());
                            onBackPressed();
                        }
                    })
                    .setNegativeButton("Nein", null)
                    .show();
        }

    }

    public static <T, E> T getKeyByValue(HashMap<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Check if user should be able to join the event.
     *
     * @return true if current user is already taking part.
     */
    private boolean userAlreadyParticipates() {
        return event.getParticipantIds() != null && event.getParticipantIds().containsValue(fireUser.getUid());
    }

    /**
     * Read the participant ids deposited in the event object.
     */
    public void readEvent() {
        mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mDatabaseRef = mDatabase.getReference("events").child(eventCategory).child(eventId);
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                event = dataSnapshot.getValue(Event.class);
                event.setuId(dataSnapshot.getKey());
                Log.d("EVENT_FROM_SERVER", event.toString());

                //Check if user should be able to participate.
                if (fireUser.getUid().equals(ownerId)) {
                    mNotificationTextView.setVisibility(View.VISIBLE);
                    mNotificationTextView.setText("Das ist dein Event! \n\nKlicke auf das Icon um das Event in den Sand zu setzen.");
                    fab.setImageResource(R.drawable.ic_delete_forever_white);
                } else if (userAlreadyParticipates()) {
                    mNotificationTextView.setVisibility(View.VISIBLE);
                    mNotificationTextView.setText("Du nimmst schon teil. Klicke auf das X um auszutreten.");
                    fab.setImageResource(R.drawable.ic_close_white_24);
                } else if (eventMaxParticipants == 0) {
                    // unendlich Viele Teilnhemer zulassen
                } else if (eventMaxParticipants <= eventAmountPaticipants) {
                    mNotificationTextView.setVisibility(View.VISIBLE);
                    mNotificationTextView.setText("Dieses Event ist leider voll.");
                    fab.setVisibility(View.GONE);
                } else {
                    fab.setImageResource(R.drawable.ic_check_white_24);
                    mNotificationTextView.setVisibility(View.GONE);
                }

                DateFormat dfDate = android.text.format.DateFormat.getDateFormat(EventActivity.this);
                DateFormat dfTime = android.text.format.DateFormat.getTimeFormat(EventActivity.this);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, dd.MM.yyyy", Locale.getDefault());
                SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());


                // Setup the views.
                mDateText.setText(simpleDateFormat.format(event.getDate()) + " um " + simpleTimeFormat.format(event.getDate()));
                mDescriptionText.setText(event.getDescription());

                if (event.getMaxParticipants() == 0) {
                    mParticipantsText.setText(event.getParticipantIds().size() + " Teilnehmer");
                } else {
                    mParticipantsText.setText(event.getParticipantIds().size() + "/" + event.getMaxParticipants().intValue() + " Teilnehmer");
                }

                if (event.getAddress().equals("")) {
                    mPlaceText.setText(event.getTown() + ", " + event.getAddress() + ". " + event.getPlace());
                } else {
                    mPlaceText.setText(event.getTown() + ". " + event.getPlace());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Da ist etwas falsch gelaufen", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Read the User information from the given ids.
     *
     * @param participants The ids of the participants in a list.
     */
    public void readParticipants(List<String> participants) {
        for (int i = 0; i < participants.size(); i++) {
            DatabaseReference mDatabaseRef = mDatabase.getReference("users").child(participants.get(i));
            final int finalI = i; //for final sake.
            mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class); // read the user.
                    if (user != null) {
                        user.setUserId(dataSnapshot.getKey()); // add the userId to the user itself.
                        Log.d("GET_USERS-user", user.toString());
                        if (finalI == 0) { // the owner of the event.
                            //TODO wohin mit dem besitzer?
                        } else {
                            users.add(user); // add user to the list.
                            mUserAdapter.notifyItemChanged(finalI); // notify the adapter the new user.
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), "Da ist etwas falsch gelaufen", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


}
