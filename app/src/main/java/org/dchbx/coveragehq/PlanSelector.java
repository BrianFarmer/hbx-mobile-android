package org.dchbx.coveragehq;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.daprlabs.aaron.swipedeck.SwipeDeck;
import com.daprlabs.aaron.swipedeck.SwipeDeck.SwipeDeckCallback;

import org.dchbx.coveragehq.models.planshopping.Plan;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;


/**
 * Created by plast on 5/18/2017.
 */

public class PlanSelector extends BaseActivity {
    private static String TAG = "PlanSelector";

    private List<Plan> planList;
    private double currentPremium;
    private double currentDeductible;
    private List<Plan> filteredPlans;
    private SwipeDeck deckView;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.plan_selector);
        deckView = (SwipeDeck) findViewById(R.id.swipeDeck);
        getMessages().getPlans();
        initToolbar();
    }


    private void initToolbar(){
        this.drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        this.navigationView = (NavigationView)findViewById(R.id.navigation);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.app_header);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.drawer_open, R.string.drawer_close){

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };
        final PlanSelector mainActivity = this;
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                //Closing drawer on item click
                drawerLayout.closeDrawers();


                switch (item.getItemId()){
                    case R.id.nav_call_healthlink:
                        if (((TelephonyManager)BrokerApplication.getBrokerApplication().getSystemService(Context.TELEPHONY_SERVICE)).getPhoneType()
                                == TelephonyManager.PHONE_TYPE_NONE) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(PlanSelector.this);
                            builder.setMessage("DC Health Link: " + Constants.HbxPhoneNumber)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        } else {
                            Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                            phoneIntent.setData(Uri.parse("tel:" + Constants.HbxPhoneNumber));
                            startActivity(phoneIntent);
                        }
                        return true;
                    case R.id.nav_email_healthlink:
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        emailIntent.setData(Uri.parse(BrokerApplication.getBrokerApplication().getString(R.string.hbx_mail_url)));
                        startActivity(emailIntent);
                        return true;
                    case R.id.nav_logout:
                        getMessages().logoutRequest();
                        Intent i = new Intent(PlanSelector.this, RootActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        return true;
                    case R.id.nav_carriers:
                        Intent carrierIntent = new Intent(mainActivity, CarriersActivity.class);
                        Log.d(TAG, "onClick: launching carriers activitiy");
                        mainActivity.startActivity(carrierIntent);
                        return true;
                }
                return false;
            }
        });

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //MenuItem carriersMenuItem=navigationView.getMenu().findItem(R.id.nav_call_healthlink);
        MenuItem callMenuItem=navigationView.getMenu().findItem(R.id.nav_call_healthlink);
        callMenuItem.setIcon(R.drawable.call_color);
        //MenuItem logoutMenuItem=navigationView.getMenu().findItem(R.id.nav_logout);


        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetPlansResult getPlansResult) throws Exception {
        planList = getPlansResult.getPlanList();
        currentPremium = getPlansResult.getPremiumFilter();
        currentDeductible = getPlansResult.getDeductibleFilter();

        populate();
    }

    private void populate() {
        deckView.setCallback(new SwipeDeckCallback() {
            @Override
            public void cardSwipedLeft(long positionInAdapter) {
                Log.i("MainActivity", "card was swiped left, position in adapter: " + positionInAdapter);
            }

            @Override
            public void cardSwipedRight(long positoinInAdapter) {
                Log.i("MainActivity", "card was swiped right, position in adapter: " + positoinInAdapter);

            }
        });


        PlanCardAdapter planCardAdapter = new PlanCardAdapter(planList, this);
        deckView.setAdapter(planCardAdapter);

    }
}
