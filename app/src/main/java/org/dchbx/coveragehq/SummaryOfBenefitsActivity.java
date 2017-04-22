package org.dchbx.coveragehq;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.dchbx.coveragehq.models.roster.Enrollment;
import org.dchbx.coveragehq.models.roster.RosterEntry;
import org.dchbx.coveragehq.models.services.Service;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by plast on 4/13/2017.
 */

public class SummaryOfBenefitsActivity extends BrokerActivity {
    static private String TAG = "SummaryAndBenefitsAct";
    private SummaryAdapter summaryAdapter;
    private ListView summaryList;
    private LocalDate currentDate;
    private RosterEntry insured;
    private Enrollment currentEnrollment;
    private List<Service> servicesList;
    private boolean showHealth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "in SummaryAndBenefitsActivity.onCreate");
        Intent intent = getIntent();
        currentDate = LocalDate.parse(intent.getStringExtra(Intents.ENROLLMENT_DATE_ID));
        showHealth = intent.getBooleanExtra(Intents.SHOW_HEALTH_ID, true);


        setContentView(R.layout.summary_and_benefits_activity);
        summaryList = (ListView) findViewById(R.id.summaryList);
        getMessages().getInsuredAndServices(currentDate);
        configToolbar();
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

    private void populateHeader(){
        if (insured.enrollments != null
                && insured.enrollments.size() > 0) {
            currentEnrollment = BrokerUtilities.getEnrollment(insured, currentDate);

            TextView textViewEmployeeName = (TextView) findViewById(R.id.textViewEmployeeName);
            textViewEmployeeName.setText(BrokerUtilities.getFullName(insured));

            Enrollment enrollment = null;
            if (insured.enrollments.size() > 1) {

                TextView textViewCoverageYear = (TextView) findViewById(R.id.textViewCoverageYear);
                textViewCoverageYear.setVisibility(View.INVISIBLE);


                if (currentDate == null) {
                    for (Enrollment curEnrollment : insured.enrollments) {
                        if (curEnrollment.startOn.compareTo(currentDate) > 0) {
                            currentDate = curEnrollment.startOn;
                            enrollment = curEnrollment;
                        }
                    }
                } else {
                    enrollment = BrokerUtilities.getPlanYearForCoverageYear(insured, currentDate);
                }

                Spinner spinnerCoverageYear = (Spinner) findViewById(R.id.spinnerCoverageYear);
                spinnerCoverageYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                        currentDate = insured.enrollments.get(pos).startOn;
                        try {
                            Enrollment enrollment = BrokerUtilities.getEnrollmentForCoverageYear(insured, currentDate);
                            populateCoverageYearDependencies(enrollment, SummaryOfBenefitsActivity.this.getResources());
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
                for (Enrollment curEnrollment : insured.enrollments) {
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
                enrollment = insured.enrollments.get(0);
                currentDate = enrollment.startOn;
                Spinner spinnerCoverageYear = (Spinner) findViewById(R.id.spinnerCoverageYear);
                spinnerCoverageYear.setVisibility(View.INVISIBLE);
                TextView textViewCoverageYear = (TextView) findViewById(R.id.textViewCoverageYear);
                textViewCoverageYear.setVisibility(View.VISIBLE);
                String thisYear = String.format("%s - %s", Utilities.DateAsMonthDayYear(enrollment.startOn), Utilities.DateAsMonthDayYear(Utilities.calculateOneYearOut(enrollment.startOn)));
                textViewCoverageYear.setText(thisYear);
            }

            //populateCoverageYearDependencies(enrollment, resources);
        }
    }

    private void populateCoverageYearDependencies(Enrollment enrollment, Resources resources) {
        populateList();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    void doThis(Events.GetInsuredAndServicesResult getInsuredAndServicesResult){
        insured = getInsuredAndServicesResult.getInsured();
        servicesList = getInsuredAndServicesResult.getServicesList();
        populate();
    }

    private void populate(){
        Log.d(TAG, "In SummaryOfBenefitsActivity.populate()");
        populateHeader();
        populateList();
    }


    private void populateList(){
        this.summaryAdapter = new SummaryAdapter(this, servicesList, showHealth?currentEnrollment.health:currentEnrollment.dental);
        summaryList.setAdapter(summaryAdapter);
        Log.d(TAG, "populate complete");
    }
}
