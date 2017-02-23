package gov.dc.broker;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.azure.mobile.analytics.Analytics;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import gov.dc.broker.models.brokeragency.BrokerClient;
import gov.dc.broker.models.brokeragency.ContactInfo;
import gov.dc.broker.models.employer.Employer;
import gov.dc.broker.models.employer.PlanYear;

/**
 * Created by plast on 10/21/2016.
 */

public class EmployerDetailsActivity extends BrokerActivity {
    private static final String TAG = "EmployerDetailsActivity";
    public static final String BROKER_CLIENT_ID = "BrokerClientId";

    private final String INFO_TAB = "info_tab";
    private final String ROSTER_TAB = "roster_tab";
    private final String COSTS_TAB = "costs_tab";
    private final String PLANS_TAB = "plans_tab";


    private Toolbar toolbar;
    private NavigationView navigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;


    private String clientId;
    private FragmentTabHost tabHost;
    private PlanYear planYear;
    private LocalDate coverageYear;
    private String rosterFilter = null;
    private Employer employer;
    private BrokerClient brokerClient;
    private boolean haveBroker;
    private ArrayList<BrokerFragment> fragments = new ArrayList<>(4);

    public EmployerDetailsActivity(){
        Log.d(TAG, "In EmployerDetailsActivity Ctor");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Error error){
        Toast toast = Toast.makeText(this, "EmployerDetails: Error retrieving employer data.", Toast.LENGTH_LONG);
        toast.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.BrokerClient  brokerClientEvent) {
        brokerClient = brokerClientEvent.getBrokerClient();
        employer = brokerClientEvent.getEmployer();
        populate();

        // This is for analytics
        Map<String,String> properties=new HashMap<String,String>();
        if (brokerClient != null) {
            if (brokerClient.planYears.size() > 0) {
                gov.dc.broker.models.brokeragency.PlanYear planYear = BrokerUtilities.getMostRecentPlanYear(brokerClient);
                properties.put("Status", BrokerUtilities.getBrokerClientStatus(planYear, planYear.planYearBegins).name());
            }
        } else {
            if (employer.planYears.size() > 0) {
                PlanYear planYear = BrokerUtilities.getMostRecentPlanYear(employer);
                properties.put("Status", BrokerUtilities.getBrokerClientStatus(planYear, planYear.planYearBegins).name());
            }
        }
        Analytics.trackEvent("Employer Details", properties);
    }

    private void populate(){
        TextView textViewCompanyName = (TextView) findViewById(R.id.textViewCompanyName);
        textViewCompanyName.setText(employer.employerName);

        Spinner spinnerCoverageYear = (Spinner) findViewById(R.id.spinnerCoverageYear);
        TextView textViewCoverageYear = (TextView) findViewById(R.id.textViewCoverageYear);
        TextView textViewCoverageYearLabel = (TextView) findViewById(R.id.textViewCoverageYearLabel);
        TextView textViewNoPlansLablel = (TextView) findViewById(R.id.textViewNoPlansLablel);

        ArrayList<String> list = new ArrayList<>();

        // set coverage year to the lowsest playYearBegins in the planYears
        LocalDate initialCoverageYear = new LocalDate(2000, 1, 1);
        if (employer.planYears != null
            && employer.planYears.size() > 1) {
            spinnerCoverageYear.setVisibility(View.VISIBLE);
            textViewCoverageYear.setVisibility(View.INVISIBLE);
            textViewCoverageYearLabel.setVisibility(View.VISIBLE);
            textViewNoPlansLablel.setVisibility(View.INVISIBLE);
            int i = 0;
            int selectedIndex = 0;
            for (gov.dc.broker.models.employer.PlanYear curPlanYear : employer.planYears) {
                if (curPlanYear.planYearBegins != null
                        && curPlanYear.planYearBegins.compareTo(initialCoverageYear) > 0) {
                    planYear = curPlanYear;
                    initialCoverageYear = curPlanYear.planYearBegins;
                    selectedIndex = i;
                }
                LocalDate endDate = curPlanYear.planYearBegins.plusYears(1).minusDays(1);
                list.add(String.format("%s - %s (%s)", Utilities.DateAsMonthDayYear(curPlanYear.planYearBegins), Utilities.DateAsMonthDayYear(endDate), curPlanYear.state));
                i++;

                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, list);
                spinnerCoverageYear.setAdapter(dataAdapter);
                spinnerCoverageYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                        planYear = employer.planYears.get(pos);
                        coverageYear = planYear.planYearBegins;
                        populateStatus();
                        getMessages().coverageYearChanged(coverageYear);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                spinnerCoverageYear.setSelection(selectedIndex);
            }
            coverageYear = initialCoverageYear;
        } else {
            if (employer.planYears != null
                && employer.planYears.size() == 1) {
                spinnerCoverageYear.setVisibility(View.INVISIBLE);
                textViewCoverageYear.setVisibility(View.VISIBLE);
                textViewCoverageYearLabel.setVisibility(View.VISIBLE);
                textViewNoPlansLablel.setVisibility(View.INVISIBLE);
                planYear = employer.planYears.get(0);
                coverageYear = planYear.planYearBegins;
                textViewCoverageYear.setText(String.format("%s (%s)", Utilities.DateAsMonthDayYear(planYear.planYearBegins), planYear.state));
            } else {
                textViewNoPlansLablel.setVisibility(View.VISIBLE);
                spinnerCoverageYear.setVisibility(View.INVISIBLE);
                textViewCoverageYearLabel.setVisibility(View.INVISIBLE);
                textViewCoverageYear.setVisibility(View.INVISIBLE);
                planYear = null;
                coverageYear = null;
            }
        }


        populateStatus();
        configButtons();
        if (haveBroker) {
            configToolbar();
        }
    }

    private void populateStatus() {
        if (planYear == null) {
            return;
        }

        LocalDate today = new LocalDate();
        TextView textViewEnrollmentStatus = (TextView) findViewById(R.id.textViewEnrollmentStatus);
        BrokerUtilities.PlanStatus planStatus = BrokerUtilities.getPlanStatus(planYear, today);
        textViewEnrollmentStatus.setText(planStatus.statusStringId);
        textViewEnrollmentStatus.setTextColor(ContextCompat.getColor(this, planStatus.statusColorId));
    }

    private void configToolbar() {
        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.app_header);
        toolbar.setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void configButtons(){
        if (employer == null){
            return;
        }

        if (brokerClient == null){
            View viewById = findViewById(R.id.action_buttons_row);
            viewById.setVisibility(View.GONE);
            return;
        }
        ContactInfo curContactInfo = brokerClient.contactInfo.get(0);

        ImageButton emailButton = (ImageButton)findViewById(R.id.imageButtonEmail);
        if (BrokerUtilities.anyEmailAddresses(brokerClient)) {
            emailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactDialog contactDialog = ContactDialog.build(brokerClient, ContactListAdapter.ListType.Email);
                    contactDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
                    contactDialog.show(getSupportFragmentManager(), "ContactDialog");
                }
            });
        } else {
            emailButton.setEnabled(false);
        }
        ImageButton chatButton = (ImageButton) findViewById(R.id.imageButtonChat);
        if (BrokerUtilities.anyMobileNumbers(brokerClient)) {
            chatButton.setEnabled(true);
            chatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactDialog contactDialog = ContactDialog.build(brokerClient, ContactListAdapter.ListType.Chat);
                    contactDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
                    contactDialog.show(getSupportFragmentManager(), "ContactDialog");
                }
            });
        } else {
            chatButton.setEnabled(false);
        }

        ImageButton phoneButton = (ImageButton) findViewById(R.id.imageButtonPhone);
        if (BrokerUtilities.anyPhoneNumbers(brokerClient)) {
            phoneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactDialog contactDialog = ContactDialog.build(brokerClient, ContactListAdapter.ListType.Phone);
                    contactDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
                    contactDialog.show(getSupportFragmentManager(), "ContactDialog");
                }
            });
        } else {
            phoneButton.setEnabled(false);
        }
        ImageButton locationButton = (ImageButton) findViewById(R.id.imageButtonLocation);
        if (BrokerUtilities.anyAddresses(brokerClient)) {
            locationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactDialog contactDialog = ContactDialog.build(brokerClient, ContactListAdapter.ListType.Directions);
                    contactDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
                    contactDialog.show(getSupportFragmentManager(), "ContactDialog");
                }
            });
        } else {
            locationButton.setEnabled(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int viewId;


        Intent intent = getIntent();
        clientId = intent.getStringExtra(BROKER_CLIENT_ID);
        if (clientId == null) {
            Log.e(TAG, "onCreate: logged in as client since no client id found in intent");
            getMessages().getEmployer();
            viewId = R.layout.employer_details_with_drawer;
            haveBroker = false;
        } else {
            getMessages().getEmployer(clientId);
            viewId = R.layout.employer_details;
            haveBroker = true;
        }


        LayoutInflater inflater = getLayoutInflater();
        setContentView(viewId);


        if (!haveBroker) {
            this.mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            this.navigationView = (NavigationView) findViewById(R.id.navigation);


            // Initializing Toolbar and setting it as the actionbar
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setLogo(R.drawable.app_header);


            ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

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


            final EmployerDetailsActivity employerDetailsActivity = this;
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {
                    //Closing drawer on item click
                    mDrawerLayout.closeDrawers();

                    switch (item.getItemId()) {
                        case R.id.nav_call_healthlink:
                            Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                            phoneIntent.setData(Uri.parse("tel:" + Constants.HbxPhoneNumber));
                            startActivity(phoneIntent);
                            return true;
                        case R.id.nav_email_healthlink:
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                            emailIntent.setData(Uri.parse("mailto:" + Constants.HbxEmail));
                            startActivity(emailIntent);
                            return true;
                        case R.id.nav_logout:
                            getMessages().logoutRequest();
                            Intent i = new Intent(EmployerDetailsActivity.this, RootActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            return true;
                        case R.id.nav_carriers:
                            Intent carrierIntent = new Intent(employerDetailsActivity, CarriersActivity.class);
                            Log.d(TAG, "onClick: launching carriers activitiy");
                            employerDetailsActivity.startActivity(carrierIntent);
                            return true;
                    }
                    return false;
                }
            });

            //Setting the actionbarToggle to drawer layout
            mDrawerLayout.setDrawerListener(actionBarDrawerToggle);

            MenuItem carriersMenuItem=navigationView.getMenu().findItem(R.id.nav_carriers);
            MenuItem callMenuItem=navigationView.getMenu().findItem(R.id.nav_call_healthlink);
            callMenuItem.setIcon(R.drawable.call_color);
            MenuItem logoutMenuItem=navigationView.getMenu().findItem(R.id.nav_logout);


            //calling sync state is necessay or else your hamburger icon wont show up
            actionBarDrawerToggle.syncState();
        }



        tabHost = (FragmentTabHost)findViewById(R.id.tabhost);
        tabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.info_tab_normal);
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(INFO_TAB)
                .setIndicator(createTabIndicator(inflater, tabHost, R.string.info_tab_name, R.drawable.info_tab_states, true));

        try {
            tabHost.addTab(
                    tabSpec,
                    InfoFragment.class, null);
        } catch (Throwable e){
            Log.e(TAG, "exception", e);
        }
        tabHost.addTab(
                tabHost.newTabSpec(ROSTER_TAB)
                .setIndicator(createTabIndicator(inflater, tabHost, R.string.roster_tab_name, R.drawable.roster_tab_states, false)),
                RosterFragment.class, null);
        tabHost.addTab(
                tabHost.newTabSpec(COSTS_TAB)
                    .setIndicator(createTabIndicator(inflater, tabHost, R.string.costs_tab_name, R.drawable.costs_tab_states, false)),
                CostsFragment.class, null);
        tabHost.addTab(
                tabHost.newTabSpec(PLANS_TAB)
                    .setIndicator(createTabIndicator(inflater, tabHost, R.string.plans_tab_name, R.drawable.plans_tab_states, false)),
                PlansFragment.class, null);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                selectedTabChanged(tabId);
            }
        });
    }
/*
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }*/

    public void showRoster(String filter){
        tabHost.setCurrentTab(1);
        rosterFilter = filter;
        selectedTabChanged(ROSTER_TAB);
    }

    private void selectedTabChanged(String tabId) {
        int currentTab = tabHost.getCurrentTab();
        TabWidget tabWidget = tabHost.getTabWidget();
        int unselectedTabColor = ContextCompat.getColor(this, R.color.unselected_tab_color);
        int selectedTabColor = ContextCompat.getColor(this, R.color.selected_tab_color);
        for(int i = 0; i < 4; i ++){
            View tab = tabWidget.getChildTabViewAt(i);
            TextView tabTitle = (TextView) tab.findViewById(R.id.tabtitle);
            if (i != currentTab) {
                tab.setBackgroundColor(unselectedTabColor);
                tabTitle.setTextColor(selectedTabColor);
            } else {
                tab.setBackgroundColor(selectedTabColor);
                tabTitle.setTextColor(unselectedTabColor);
            }
        }
    }

    public View createTabIndicator(LayoutInflater inflater, FragmentTabHost tabHost, int textResource, int iconResource, boolean selected) {
        View tabIndicator = inflater.inflate(R.layout.tab_indicator, tabHost.getTabWidget(), false);
        TextView tabTitle = (TextView) tabIndicator.findViewById(R.id.tabtitle);
        tabTitle.setText(textResource);
        ImageView tabImage = (ImageView) tabIndicator.findViewById(R.id.tabicon);
        tabImage.setImageResource(iconResource);

        if (selected){
            tabIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.selected_tab_color));
            tabTitle.setTextColor(ContextCompat.getColor(this, R.color.unselected_tab_color));
        }else {
            tabIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.unselected_tab_color));
            tabTitle.setTextColor(ContextCompat.getColor(this, R.color.selected_tab_color));
        }
        return tabIndicator;
    }

    public LocalDate getCoverageYear() {
        return coverageYear;
    }

    public String getRosterFilter() {
        return rosterFilter;
    }
}
