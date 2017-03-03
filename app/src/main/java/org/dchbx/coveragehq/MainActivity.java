package org.dchbx.coveragehq;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import com.microsoft.azure.mobile.analytics.Analytics;

import org.dchbx.coveragehq.models.brokeragency.BrokerAgency;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends BrokerActivity {
    private final String TAG = "MainActivity";

    private RelativeLayout contentView;
    private DrawerLayout mDrawerLayout;
    private ListView listViewEmployers;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private WebView webViewWelcome;

    private int scrollPosition = -1;
    private BrokerAgency brokerAgency;
    private LocalDate coverageYear;
    private MenuItem searchMenuItem;
    private SearchView searchView;

    EmployerAdapter employerAdapter = null;

    public MainActivity(){
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        try {
            setContentView(R.layout.activity_main);
        } catch (Exception e){
            Log.d(TAG, "exception in setContentView: " + e.getClass().getName());
            throw e;
        }

        //Intent intent = new Intent(this, BrokerWorker.class);
        //intent.setData(Uri.parse("http://dc.gov"));
        //this.startService(intent);

        this.webViewWelcome = (WebView)findViewById(R.id.webViewWelcome);
        this.mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        this.navigationView = (NavigationView)findViewById(R.id.navigation);
        this.contentView = (RelativeLayout)mDrawerLayout.findViewById(R.id.relative_layout);
        this.listViewEmployers = (ListView)contentView.findViewById(R.id.listViewEmployers);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.app_header);


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.drawer_open, R.string.drawer_close){

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

        final MainActivity mainActivity = this;
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                mDrawerLayout.closeDrawers();
                return HamburgerHelper.handleHamburgerListener(item, mainActivity, getMessages());
            }
        });

        //Setting the actionbarToggle to drawer layout
        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);

        //MenuItem carriersMenuItem=navigationView.getMenu().findItem(R.id.nav_call_healthlink);
        MenuItem callMenuItem=navigationView.getMenu().findItem(R.id.nav_call_healthlink);
        callMenuItem.setIcon(R.drawable.call_color);
        //MenuItem logoutMenuItem=navigationView.getMenu().findItem(R.id.nav_logout);


        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MainActivity _this = this;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_activity, menu);

        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.search);
        View view = MenuItemCompat.getActionView(searchMenuItem);
        searchView = (SearchView)view;

        ComponentName componentName = getComponentName();
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(componentName);
        searchView.setSearchableInfo(searchableInfo);
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                employerAdapter.getFilter().filter(newText);
                return true;
            }
        });

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        scrollPosition = listViewEmployers.getFirstVisiblePosition();
    }

    @Override

    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getMessages().getBrokerAgency();
    }

    private void showEmployer() {
        Intent intent = new Intent(this, EmployerDetailsActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.LoggedOutResult loggedOutResult){
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Error error) {
        alertDialog("Error! What should I be showing? " + error.getMessage(), new DialogClosed() {
            @Override
            public void closed() {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetBrokerAgencyResult getBrokerAgencyResult) throws Exception {
        if (getBrokerAgencyResult == null
            || getBrokerAgencyResult.getBrokerAgency() == null){
            alertDialog("Should be logging out now.", new DialogClosed() {
                @Override
                public void closed() {
                    Intents.restartApp(MainActivity.this);
                    finish();
                }
            });
            return;
        }

        brokerAgency = getBrokerAgencyResult.getBrokerAgency();

        Map<String,String> properties=new HashMap<String,String>();
        if (brokerAgency.brokerClients == null){
            properties.put("Employer Count", "0");
        } else {
            properties.put("Employer Count", Integer.toString(brokerAgency.brokerClients.size()));
        }
        Analytics.trackEvent("Broker Details", properties);

        if (brokerAgency.brokerClients.size() == 0){
            coverageYear = null;
        } else {
            coverageYear = brokerAgency.brokerClients.get(0).planYearBegins;
        }
        employerAdapter = new EmployerAdapter(this, this.getBaseContext(), brokerAgency.brokerClients, coverageYear);
        listViewEmployers.setAdapter(employerAdapter);
        listViewEmployers.setSelectionFromTop(scrollPosition, 0);
        //swipeActionAdapter = new SwipeActionAdapter(employerAdapter);
        //swipeActionAdapter.setListView(listViewEmployers);
        //swipeActionAdapter.addBackground(SwipeDirection.DIRECTION_FAR_LEFT, R.layout.employer_swipe_layout);

        Resources resources = getResources();
        String welcomeMessageFormat = resources.getString(R.string.welcome_html);
        String welcomeMessage = String.format(welcomeMessageFormat, brokerAgency.brokerName,
                Integer.toString(brokerAgency.brokerClients.size()),
                Integer.toString(BrokerUtilities.getBrokerClientsInOpenEnrollment(brokerAgency, LocalDate.now()).size()));
        webViewWelcome.loadDataWithBaseURL("", welcomeMessage, "text/html", "UTF-8", "");
        //webViewWelcome.setText(Html.fromHtml(welcomeMessage));
    }
}
