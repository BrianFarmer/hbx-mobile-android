package gov.dc.broker;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wdullaer.swipeactionadapter.SwipeActionAdapter;
import com.wdullaer.swipeactionadapter.SwipeDirection;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import static gov.dc.broker.Events.GetEmployerList;


public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    public EventBus eventBus;

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

    private Broker broker = new Broker("Test-001 User-001");
    private Account account = new Account(broker);


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
        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayUseLogoEnabled(true);
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
                        return true;
                    case R.id.nav_logout:
                        return true;
                    case R.id.nav_carriers:
                        Intent intent = new Intent(mainActivity, CarriersActivity.class);
                        Log.d(TAG, "onClick: launching carriers activitiy");
                        mainActivity.startActivity(intent);
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
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus = EventBus.getDefault();
        eventBus.register(this);
        if (BrokerManager.getDefault().isLoggedIn()){
            Log.d(TAG, "requesting employer list");
            eventBus.post(new GetEmployerList());
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        webViewWelcome = (WebView)findViewById(R.id.webViewWelcome);
        //textViewStatus = (TextView)findViewById(R.id.textViewStatus);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(BrokerResult brokerResult) {
        Toast.makeText(this, (CharSequence) brokerResult.getBroker().getName(), Toast.LENGTH_SHORT).show();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.EmployerList employerList) {
        EmployerList employerList1 = employerList.getEmployerList();
        final EmployerAdapter employerAdapter = new EmployerAdapter(this, this.getBaseContext(), employerList1);
        swipeActionAdapter = new SwipeActionAdapter(employerAdapter);
        swipeActionAdapter.setListView(listViewEmployers);
        listViewEmployers.setAdapter(swipeActionAdapter);
        swipeActionAdapter.addBackground(SwipeDirection.DIRECTION_NORMAL_LEFT, R.layout.employer_swipe_layout);
        swipeActionAdapter.setSwipeActionListener(new SwipeActionAdapter.SwipeActionListener() {
            @Override
            public boolean hasActions(int position, SwipeDirection direction) {
                if (employerAdapter.isItemSwipeable(position)
                    && direction == SwipeDirection.DIRECTION_NORMAL_LEFT){
                    return true;
                }
                return false;
            }

            @Override
            public boolean shouldDismiss(int position, SwipeDirection direction) {
                return false;
            }

            @Override
            public void onSwipe(int[] position, SwipeDirection[] direction) {

            }
        });

        Resources resources = getResources();
        String welcomeMessageFormat = resources.getString(R.string.welcome_html);
        String welcomeMessage = String.format(welcomeMessageFormat, account.getBroker().getName(), 15, 15);
        webViewWelcome.loadDataWithBaseURL("", welcomeMessage, "text/html", "UTF-8", "");
        //webViewWelcome.setText(Html.fromHtml(welcomeMessage));
    }
}
