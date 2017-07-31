package org.dchbx.coveragehq;
/*
    This file is part of DC.

    DC Health Link SmallBiz is free software:you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation,either version 3of the License,or
    (at your option)any later version.

    DC Health Link SmallBiz is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY;without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DC Health Link SmallBiz.If not,see<http://www.gnu.org/licenses/>.
    This statement should go near the beginning of every source file,close to the copyright notices.When using the Lesser GPL,insert the word “Lesser” before “General” in all three places.When using the GNU AGPL,insert the word “Affero” before “General” in all three places.
*/

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
import android.widget.Button;
import android.widget.ListView;

import org.dchbx.coveragehq.models.planshopping.Plan;
import org.dchbx.coveragehq.models.services.Service;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class PlanDetailsActivity extends BaseActivity {
    private static String TAG = "PlanDetailsActivity";
    public static StateManager.UiActivity uiActivity;

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private Plan plan;
    private PlanDetailsAdapter planCardAdapter;
    private ListView detailsList;
    private List<Service> services;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        setContentView(R.layout.plan_details);
        String planId = intent.getExtras().getCharSequence(Intents.PLAN_ID).toString();
        configToolbar();
        getMessages().getPlan(planId, true);
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
        final PlanDetailsActivity planDetailsActivity = this;
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                //Closing drawer on item click
                drawerLayout.closeDrawers();


                switch (item.getItemId()){
                    case R.id.nav_call_healthlink:
                        if (((TelephonyManager)BrokerApplication.getBrokerApplication().getSystemService(Context.TELEPHONY_SERVICE)).getPhoneType()
                                == TelephonyManager.PHONE_TYPE_NONE) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(PlanDetailsActivity.this);
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
                        Intent i = new Intent(PlanDetailsActivity.this, RootActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        return true;
                    case R.id.nav_carriers:
                        Intent carrierIntent = new Intent(planDetailsActivity, CarriersActivity.class);
                        Log.d(TAG, "onClick: launching carriers activitiy");
                        planDetailsActivity.startActivity(carrierIntent);
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
    public void doThis(Events.GetPlanResult getPlanResult) throws Exception {
        plan = getPlanResult.getPlan();
        services = getPlanResult.getServices();

        populate();
    }

    private void populate() {
        if (plan == null){
            Log.d(TAG, "plan item is NULL!");
            return;
        }

        planCardAdapter = new PlanDetailsAdapter(this, plan, services);
        detailsList = (ListView) findViewById(R.id.detailsList);
        detailsList.setAdapter(planCardAdapter);

        Button enroll = (Button) findViewById(R.id.enroll);
        enroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }
}
