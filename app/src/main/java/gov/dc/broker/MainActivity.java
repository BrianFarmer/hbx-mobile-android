package gov.dc.broker;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wdullaer.swipeactionadapter.SwipeActionAdapter;


import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

import gov.dc.broker.models.brokeragency.BrokerAgency;


public class MainActivity extends BrokerActivity {
    private final String TAG = "MainActivity";

    private RelativeLayout contentView;
    private DrawerLayout mDrawerLayout;
    private ListView menuView;
    private ListView listViewEmployers;
    private NavigationView navigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;
    private Toolbar toolbar;
    private WebView webViewWelcome;
    private TextView textViewStatus;
    private SwipeActionAdapter swipeActionAdapter;

    private Broker broker;
    private Account account = new Account(broker);
    private int scrollPosition = -1;
    private BrokerAgency brokerAgency;
    private LocalDate coverageYear;


    public MainActivity(){
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BuildConfig2.initMobileCenter();


        try {
            setContentView(R.layout.activity_main);
        } catch (Exception e){
            Log.d(TAG, "exception in setContentView: " + e.getClass().getName());
            throw e;
        }


        Intent intent = new Intent(this, BrokerWorker.class);
        intent.setData(Uri.parse("http://dc.gov"));
        this.startService(intent);

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
                //Closing drawer on item click
                mDrawerLayout.closeDrawers();


                switch (item.getItemId()){
                    case R.id.nav_call_healthlink:
                        Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                        phoneIntent.setData(Uri.parse("tel:" + Constants.HbxPhoneNumber));
                        startActivity(phoneIntent);
                        return true;
                    case R.id.nav_logout:
                        getMessages().logoutRequest();
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
        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);

        MenuItem carriersMenuItem=navigationView.getMenu().findItem(R.id.nav_call_healthlink);
        MenuItem callMenuItem=navigationView.getMenu().findItem(R.id.nav_call_healthlink);
        callMenuItem.setIcon(R.drawable.call_color);
        MenuItem logoutMenuItem=navigationView.getMenu().findItem(R.id.nav_logout);


        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
            }
        };
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

        getMessages().getLogin();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetLoginResult getLoginResult){
        if (getLoginResult.isLoggedIn()) {
            if (getLoginResult.getUserType() == Events.GetLoginResult.UserType.Broker) {
                Log.d(TAG, "requesting employer list");
                getMessages().getEmployerList();
            } else {
                showEmployer();
            }
        } else {
            showLogin();
            return;
        }
        webViewWelcome = (WebView)findViewById(R.id.webViewWelcome);
    }

    private void showLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void showEmployer() {
        Intent intent = new Intent(this, EmployerDetailsActivity.class);
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(BrokerResult brokerResult) {
        //Toast.makeText(this, (CharSequence) brokerResult.getBroker().getName(), Toast.LENGTH_SHORT).show();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.LoggedOutResult loggedOutResult){
        BrokerManager.getDefault().setLoggedIn(false);
        if (!BrokerManager.getDefault().isLoggedIn()) {
            showLogin();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Error error) {
        showLogin();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.EmployerList employerListEvent) throws Exception {
        if (employerListEvent == null
            || employerListEvent.getBrokerAgency() == null){
            showLogin();
            return;
        }
        brokerAgency = employerListEvent.getBrokerAgency();

        coverageYear = brokerAgency.brokerClients.get(0).planYearBegins;
        final EmployerAdapter employerAdapter = new EmployerAdapter(this, this.getBaseContext(), brokerAgency.brokerClients, coverageYear);
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

    /*private void checkForCrashes() {
        CrashManager.register(this);
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register(this);
    }

    private void unregisterManagers() {
        UpdateManager.unregister();
    }*/
}
