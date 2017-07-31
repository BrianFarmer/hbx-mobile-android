package org.dchbx.coveragehq;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.microsoft.azure.mobile.analytics.Analytics;

import org.dchbx.coveragehq.models.roster.Dependent;
import org.dchbx.coveragehq.models.roster.Enrollment;
import org.dchbx.coveragehq.models.roster.Health;
import org.dchbx.coveragehq.models.roster.RosterEntry;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsuredDetailsActivity extends BrokerActivity {
    private static final String INFO_TAB = "InfoTab";
    private static final String GLOSSARY_TAB = "GlossaryTab";
    private static final String LIFE_EVENT_TAB = "LifeEventTab";
    private static final String CARD_TAB = "CardTab";
    private static String TAG = "EmployeeDetailsActivity";

    private String employeeId;
    private String employerId;
    private RosterEntry employee;


    private boolean detailsVisible = true;
    private boolean healthPlanVisible = false;
    private boolean dependentsVisible = false;

    private LocalDate currentDate = null;
    private Enrollment currentEnrollment;
    private FragmentTabHost tabHost;

    private static int currentPhotoRequestId = 1;
    private boolean front;
    private Uri cameraUri = null;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private boolean frontPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.insured_user_details_activity);
        getMessages().getInsured();
        configHamburgerToolbar();
        configTabs();
    }

    private void configTabs() {
        // only show tabs when logged in as employee
        if (employeeId != null){
            return;
        }

        LayoutInflater inflater = getLayoutInflater();

        tabHost = (FragmentTabHost)findViewById(R.id.tabhost);
        tabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        tabHost.addTab(tabHost.newTabSpec(INFO_TAB).setIndicator(createTabIndicator(inflater, tabHost,
                                                                                    R.string.info_tab_name,
                                                                                    R.drawable.ivl_tab_home_states,
                                                                                    true, R.color.home_color, R.color.transparent)), InsuredInfoFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec(GLOSSARY_TAB).setIndicator(createTabIndicator(inflater, tabHost,
                R.string.glossary_tab_name,
                R.drawable.ivl_tab_glossary_states,
                false, R.color.glossary_color, R.color.transparent)), GlossaryFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec(LIFE_EVENT_TAB).setIndicator(createTabIndicator(inflater, tabHost,
                R.string.life_event_tab_name,
                R.drawable.ivl_tab_life_events_states,
                false, R.color.life_event_color, R.color.transparent)), LifeEventFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec(CARD_TAB).setIndicator(createTabIndicator(inflater, tabHost,
                R.string.card_tab_name,
                R.drawable.ivl_tab_id_card_states,
                false, R.color.id_card_color, R.color.transparent)), InsuranceCardFragment.class, null);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (tabId.compareToIgnoreCase(INFO_TAB) == 0){
                    selectedTabChanged(tabId, R.color.home_color, R.color.transparent);
                    return;
                }
                if (tabId.compareToIgnoreCase(GLOSSARY_TAB) == 0){
                    selectedTabChanged(tabId, R.color.glossary_color, R.color.transparent);
                    return;
                }
                if (tabId.compareToIgnoreCase(LIFE_EVENT_TAB) == 0){
                    selectedTabChanged(tabId, R.color.life_event_color, R.color.transparent);
                    return;
                }
                if (tabId.compareToIgnoreCase(CARD_TAB) == 0){
                    selectedTabChanged(tabId, R.color.id_card_color, R.color.transparent);
                    return;
                }

            }
        });
        tabHost.setCurrentTab(0);
    }

    @Override
    protected void onResume(){
        super.onResume();
        getMessages().testTimeOut();

        if (cameraUri != null){
            getMessages().moveImageToData(front, cameraUri);
            //cameraUri = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.TestTimeoutResult testTimeoutResult) {
        if (testTimeoutResult.timedOut){
            Intents.restartApp(this);
            finish();
        }
    }

    private void configHamburgerToolbar() {
        this.drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        this.navigationView = (NavigationView)findViewById(R.id.navigation);

        // Initializing Toolbar and setting it as the actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.app_header_vector);
        toolbar.setTitle("");

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


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


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                //Closing drawer on item click
                drawerLayout.closeDrawers();

                switch (item.getItemId()){
                    case R.id.nav_call_healthlink:
                        if (((TelephonyManager)BrokerApplication.getBrokerApplication().getSystemService(Context.TELEPHONY_SERVICE)).getPhoneType()
                                == TelephonyManager.PHONE_TYPE_NONE) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(InsuredDetailsActivity.this);
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
                        Intent i = new Intent(InsuredDetailsActivity.this, RootActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
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

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Employee  employeeEvent) throws Exception {
        this.employee = employeeEvent.getEmployee();

        Map<String,String> properties=new HashMap<String,String>();
        Analytics.trackEvent("Employee Details", properties);

        if (currentDate == null){
            currentDate = BrokerUtilities.getMostRecentPlanYear(employee);
        }

        try {
            populate();
        } catch (Exception e){
            Log.e(TAG, "exception populating activity", e);
        }
    }

    private void populate() throws Exception {
        if (employee == null) {
            return;
        }

        Resources resources = getResources();

        if (employee.enrollments != null
            && employee.enrollments.size() > 0) {
            currentEnrollment = BrokerUtilities.getEnrollment(employee, currentDate);

            TextView textViewEmployeeName = (TextView) findViewById(R.id.textViewEmployeeName);
            textViewEmployeeName.setText(BrokerUtilities.getFullName(employee));

            Enrollment enrollment = null;
            if (employee.enrollments.size() > 1) {

                TextView textViewCoverageYear = (TextView) findViewById(R.id.textViewCoverageYear);
                textViewCoverageYear.setVisibility(View.INVISIBLE);


                if (currentDate == null) {
                    for (Enrollment curEnrollment : employee.enrollments) {
                        if (curEnrollment.startOn.compareTo(currentDate) > 0) {
                            currentDate = curEnrollment.startOn;
                            enrollment = curEnrollment;
                        }
                    }
                } else {
                    enrollment = BrokerUtilities.getPlanYearForCoverageYear(employee, currentDate);
                }

                Spinner spinnerCoverageYear = (Spinner) findViewById(R.id.spinnerCoverageYear);
                spinnerCoverageYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                        currentDate = employee.enrollments.get(pos).startOn;
                        try {
                            Enrollment enrollment = BrokerUtilities.getEnrollmentForCoverageYear(employee, currentDate);
                            populateCoverageYearDependencies(enrollment, InsuredDetailsActivity.this.getResources());
                            getMessages().updateInsuredFragment(currentDate);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                List<String> list = new ArrayList<>();
                int i = 0;
                int coverageYearIndex = 0;
                for (Enrollment curEnrollment : employee.enrollments) {
                    String thisYear = String.format("%s - %s", Utilities.DateAsMonthDayYear(curEnrollment.startOn), Utilities.DateAsMonthDayYear(Utilities.calculateOneYearOut(curEnrollment.startOn)));
                    list.add(thisYear);
                    if (curEnrollment.startOn.compareTo(currentDate) == 0) {
                        coverageYearIndex = i;
                    }
                    i++;
                }

                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, list);
                spinnerCoverageYear.setAdapter(dataAdapter);
                spinnerCoverageYear.setSelection(coverageYearIndex);
            } else {
                enrollment = employee.enrollments.get(0);
                currentDate = enrollment.startOn;
                Spinner spinnerCoverageYear = (Spinner) findViewById(R.id.spinnerCoverageYear);
                spinnerCoverageYear.setVisibility(View.INVISIBLE);
                TextView textViewCoverageYear = (TextView) findViewById(R.id.textViewCoverageYear);
                textViewCoverageYear.setVisibility(View.VISIBLE);
                String thisYear = String.format("%s - %s", Utilities.DateAsMonthDayYear(enrollment.startOn), Utilities.DateAsMonthDayYear(Utilities.calculateOneYearOut(enrollment.startOn)));
                textViewCoverageYear.setText(thisYear);
            }

            populateCoverageYearDependencies(enrollment, resources);
        }

        TextView textViewDobField = (TextView) findViewById(R.id.textViewDobField);
        textViewDobField.setText(String.format(resources.getString(R.string.dob_field_format), Utilities.DateAsString(employee.dateOfBirth)));

        TextView textViewSsnField = (TextView) findViewById(R.id.textViewSsnField);
        textViewSsnField.setText(String.format(resources.getString(R.string.ssn_field_format), employee.ssnMasked));

        if (employee.hiredOn != null) {
            TextView textViewHiredOn = (TextView) findViewById(R.id.textViewHiredOn);
            textViewHiredOn.setText(String.format(resources.getString(R.string.hired_on_field_format), Utilities.DateAsString(employee.hiredOn)));
        }

        // Populate the employee's dependants.
        RelativeLayout parent = (RelativeLayout) findViewById(R.id.relativeLayoutEmpoyeeDetails);
        int aboveId = R.id.textViewDependentsDrawer;
        for (Dependent dependent : employee.dependents) {
            View viewDependantRoot = LayoutInflater.from(this).inflate(R.layout.dependent_info, parent, false);
            TextView textViewName = (TextView) viewDependantRoot.findViewById(R.id.textViewName);
            textViewName.setText(BrokerUtilities.getFullName(dependent));

            TextView textViewGender = (TextView) viewDependantRoot.findViewById(R.id.textViewGender);
            textViewGender.setText(dependent.gender);

            TextView textViewDob = (TextView) viewDependantRoot.findViewById(R.id.textViewDob);
            textViewDob.setText(Utilities.DateAsMonthDayYear(dependent.dateOfBirth));

            parent.addView(viewDependantRoot);
            int id = findId();
            viewDependantRoot.setId(id);
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            p.addRule(RelativeLayout.BELOW, aboveId);
            p.addRule(RelativeLayout.ALIGN_PARENT_LEFT, aboveId);
            viewDependantRoot.setLayoutParams(p);
            aboveId = id;
        }

        setVisibility(R.string.dependents_group_tag, false, R.id.imageViewDependentsDrawer, R.drawable.blue_uparrow, R.drawable.blue_circle_plus);
    }

    private void populateCoverageYearDependencies(Enrollment enrollment, Resources resources) throws Exception {
        Health health = enrollment.health;

        TextView textViewEnrollmentStatus = (TextView) findViewById(R.id.textViewEnrollmentStatus);
        textViewEnrollmentStatus.setText(enrollment.health.status);
        textViewEnrollmentStatus.setTextColor(ContextCompat.getColor(this, Utilities.colorFromEmployeeStatus(enrollment.health.status)));


    }


    private void selectedTabChanged(String tabId, int selectedColor, int unselectedColor) {
        int currentTab = tabHost.getCurrentTab();
        TabWidget tabWidget = tabHost.getTabWidget();
        int unselectedTabColor = ContextCompat.getColor(this, unselectedColor);
        int selectedTabColor = ContextCompat.getColor(this, selectedColor);
        for(int i = 0; i < 4; i ++){
            View tab = tabWidget.getChildTabViewAt(i);
            TextView tabTitle = (TextView) tab.findViewById(R.id.tabtitle);
            if (i != currentTab) {
                tab.setBackgroundColor(unselectedTabColor);
            } else {
                tab.setBackgroundColor(selectedTabColor);
            }
        }
    }

    public View createTabIndicator(LayoutInflater inflater, FragmentTabHost tabHost, int textResource, int iconResource, boolean selected,
                                   int selectedColor, int unselectedColor) {
        View tabIndicator = inflater.inflate(R.layout.ivl_tab_indicator, tabHost.getTabWidget(), false);
        ImageView tabImage = (ImageView) tabIndicator.findViewById(R.id.tabicon);
        tabImage.setImageResource(iconResource);

        if (selected){
            tabIndicator.setBackgroundColor(ContextCompat.getColor(this, selectedColor));
        }else {
            tabIndicator.setBackgroundColor(ContextCompat.getColor(this, unselectedColor));
        }
        return tabIndicator;
    }

    public int findId(){
        int id = R.id.relativeLayoutEmpoyeeDetails;
        View v = findViewById(id);
        while (v != null){
            v = findViewById(++id);
        }
        return id;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Error error) {
        Log.d(TAG, "InsuredDetailsActivity.doThis(Error)");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.error_login_again)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intents.restartApp(InsuredDetailsActivity.this);
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.CapturePhoto capturePhoto) throws Exception {
        front = capturePhoto.isFront();

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        frontPicture = capturePhoto.isFront();
        Uri outputFileUri = getPhotoFileUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        if (intent.resolveActivity(BrokerApplication.getBrokerApplication().getPackageManager()) != null) {
            startActivityForResult(intent, ++ currentPhotoRequestId);
        }
    }




    public Uri getPhotoFileUri() {
        // Only continue if the SD Card is mounted
        if (isExternalStorageAvailable()) {
            // Get safe storage directory for photos
            // Use `getExternalFilesDir` on Context to access package-specific directories.
            // This way, we don't need to request external read/write runtime permissions.
            File mediaStorageDir = new File(
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
            }

            // Return the file target for the photo based on filename
            File file = new File(mediaStorageDir.getPath() + File.separator + (frontPicture?"front_image.jpg":"rear_image.jpg"));

            // wrap File object into a content provider
            // required for API >= 24
            // See https://guides.codepath.com/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
            return FileProvider.getUriForFile(this, "org.dchbx.fileprovider", file);
        }
        return null;
    }






    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == currentPhotoRequestId) {
            // Make sure the request was succcessful
            if (resultCode == RESULT_OK) {
                cameraUri = getPhotoFileUri();
            }
        }
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.MoveImageToDataResult moveImageToDataResult) {
        getMessages().updateInsurancyCard();
        cameraUri = null;
    }
}
