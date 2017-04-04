package org.dchbx.coveragehq;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import com.microsoft.azure.mobile.analytics.Analytics;

import org.dchbx.coveragehq.models.roster.Dependent;
import org.dchbx.coveragehq.models.roster.Enrollment;
import org.dchbx.coveragehq.models.roster.Health;
import org.dchbx.coveragehq.models.roster.RosterEntry;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeDetailsActivity extends BrokerActivity {
    private static final String INFO_TAB = "InfoTab";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.employee_details_activity);

        Intent intent = getIntent();
        employeeId = intent.getStringExtra(Intents.EMPLOYEE_ID);
        employerId = intent.getStringExtra(Intents.BROKER_CLIENT_ID);
        String coverageYearString = intent.getStringExtra(Intents.COVERAGE_YEAR);
        if (coverageYearString == null){
            currentDate = null;
        } else {
            currentDate = LocalDate.parse(intent.getStringExtra(Intents.COVERAGE_YEAR));
        }
        getMessages().getEmployee(employeeId, employerId);

        configToolbar();
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
                                                                                    R.drawable.info_tab_states,
                                                                                    true)), EmployeeInfoFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec(CARD_TAB).setIndicator(createTabIndicator(inflater, tabHost,
                R.string.card_tab_name,
                R.drawable.info_tab_states,
                true)), InsuranceCardFragment.class, null);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                selectedTabChanged(tabId);
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        getMessages().testTimeOut();

        if (cameraUri != null){
            getMessages().moveImageToData(front, cameraUri);
            cameraUri = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.TestTimeoutResult testTimeoutResult) {
        if (testTimeoutResult.timedOut){
            Intents.restartApp(this);
            finish();
        }
    }

    private void configToolbar() {
        // Initializing Toolbar and setting it as the actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
                            populateCoverageYearDependencies(enrollment, EmployeeDetailsActivity.this.getResources());
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


    private void selectedTabChanged(String tabId) {

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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.error_login_again)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intents.restartApp(EmployeeDetailsActivity.this);
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.CapturePhoto capturePhoto) throws Exception {
        front = capturePhoto.isFront();
        Intents.launchCamera(this, ++ currentPhotoRequestId);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == currentPhotoRequestId) {
            // Make sure the request was succcessful
            if (resultCode == RESULT_OK) {
                cameraUri = data.getData();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.MoveImageToDataResult moveImageToDataResult) {
        getMessages().updateInsurancyCard();
    }
}
