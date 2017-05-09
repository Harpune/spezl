package com.example.lukas.spezl.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lukas.spezl.R;
import com.example.lukas.spezl.model.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class DecisionActivity extends AppCompatActivity {

    private final String TAG_CATEGORY = "TAG_CATEGORY";
    private final String TAG_REGISTER = "TAG_REGISTER";

    private FirebaseUser fireUser;

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decision);

        fireUser = FirebaseAuth.getInstance().getCurrentUser();

        ImageButton owl = (ImageButton) findViewById(R.id.owl_button);
        owl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DecisionActivity.this, MyEventsActivity.class);
                startActivity(intent);
            }
        });

        ImageButton relaxButton = (ImageButton) findViewById(R.id.pic_relax);
        ImageButton partyButton = (ImageButton) findViewById(R.id.pic_party);
        ImageButton sportButton = (ImageButton) findViewById(R.id.pic_sport);
        ImageButton cookButton = (ImageButton) findViewById(R.id.pic_cook);
        ImageButton discussionButton = (ImageButton) findViewById(R.id.pic_discussion);
        ImageButton cultureButton = (ImageButton) findViewById(R.id.pic_culture);

        try {
            relaxButton.setBackground(getAssetImage(this, "entspannt"));
            partyButton.setBackground(getAssetImage(this, "feiern"));
            sportButton.setBackground(getAssetImage(this, "sport"));
            cookButton.setBackground(getAssetImage(this, "kochen"));
            discussionButton.setBackground(getAssetImage(this, "diskussion"));
            cultureButton.setBackground(getAssetImage(this, "kultur"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        initDrawerLayout();
    }

    public static Drawable getAssetImage(Context context, String filename) throws IOException {
        AssetManager assets = context.getResources().getAssets();
        InputStream buffer = new BufferedInputStream((assets.open("drawable/" + filename + ".png")));
        Bitmap bitmap = BitmapFactory.decodeStream(buffer);
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    public void showRelaxEvents(View view) {
        Intent intent = new Intent(DecisionActivity.this, CategoryActivity.class);
        intent.putExtra(TAG_CATEGORY, "Entspannt");
        startActivity(intent);
    }

    public void showPartyEvents(View view) {
        Intent intent = new Intent(DecisionActivity.this, CategoryActivity.class);
        intent.putExtra(TAG_CATEGORY, "Feiern");
        startActivity(intent);
    }

    public void showSportEvents(View view) {
        Intent intent = new Intent(DecisionActivity.this, CategoryActivity.class);
        intent.putExtra(TAG_CATEGORY, "Sport");
        startActivity(intent);
    }

    public void showCookEvents(View view) {
        Intent intent = new Intent(DecisionActivity.this, CategoryActivity.class);
        intent.putExtra(TAG_CATEGORY, "Kochen");
        startActivity(intent);
    }

    public void showDiscussionEvents(View view) {
        Intent intent = new Intent(DecisionActivity.this, CategoryActivity.class);
        intent.putExtra(TAG_CATEGORY, "Diskussion");
        startActivity(intent);
    }

    public void showCultureEvents(View view) {
        Intent intent = new Intent(DecisionActivity.this, CategoryActivity.class);
        intent.putExtra(TAG_CATEGORY, "Kultur");
        startActivity(intent);
    }

    private void initDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        toolbar.setTitle(getResources().getString(R.string.app_name));

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
                /** Called when a drawer has settled in a completely closed state. */
                public void onDrawerClosed(View view) {
                    supportInvalidateOptionsMenu();
                }

                /** Called when a drawer has settled in a completely open state. */
                public void onDrawerOpened(View drawerView) {
                    supportInvalidateOptionsMenu();
                }
            };
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_menu);
            mDrawerLayout.addDrawerListener(mDrawerToggle);
            mDrawerToggle.syncState();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.profile:
                        Intent profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                        profileIntent.putExtra(TAG_REGISTER, "TAG_REGISTER");
                        startActivity(profileIntent);
                        break;
                    case R.id.change_password:
                        Intent registerIntent = new Intent(getApplicationContext(), ResetPasswordActivity.class);
                        startActivity(registerIntent);
                        break;
                    case R.id.myEvents:
                        Intent myEventsIntent = new Intent(getApplicationContext(), MyEventsActivity.class);
                        startActivity(myEventsIntent);
                        break;
                    case R.id.settings:
                        Intent settingsIntent = new Intent(getApplicationContext(), AGBActivity.class);
                        settingsIntent.putExtra("SETUP_TOOLBAR", true);
                        startActivity(settingsIntent);
                        break;
                    case R.id.createEvent:
                        Intent createIntent = new Intent(getApplicationContext(), CreateActivity.class);
                        startActivity(createIntent);
                        mDrawerLayout.closeDrawers();
                        break;
                    case R.id.logout:
                        Toast.makeText(getApplicationContext(), "Logout", Toast.LENGTH_SHORT).show();
                        mDrawerLayout.closeDrawers();
                        FirebaseAuth.getInstance().signOut();
                        Intent logoutIntent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(logoutIntent);
                        finish();
                        break;
                    default:
                }
                return true;
            }
        });

        View header = navigationView.getHeaderView(0);
        TextView mUsernameTextField = (TextView) header.findViewById(R.id.user_name);
        mUsernameTextField.setText(fireUser.getDisplayName());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
