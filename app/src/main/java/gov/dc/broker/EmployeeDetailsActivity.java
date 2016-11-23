package gov.dc.broker;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import gov.dc.broker.models.brokerclient.BrokerClientDetails;
import gov.dc.broker.models.roster.Dependent;
import gov.dc.broker.models.roster.Employee;
import gov.dc.broker.models.roster.Health;

public class EmployeeDetailsActivity extends BrokerActivity {

    private int employeeId;
    private int employerId;
    private Events.Employee employeeEvent;
    private Events.BrokerClient brokerClient;
    private String coverageYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.employee_details_activity);

        Intent intent = getIntent();
        employeeId = intent.getIntExtra(Intents.EMPLOYEE_ID, -1);
        employerId = intent.getIntExtra(Intents.BROKER_CLIENT_ID, -1);
        getMessages().getEmployee(employeeId, employerId);
        getMessages().getEmployer(employerId);

        configToolbar();

        ImageView imageViewDetailsDrawer = (ImageView)findViewById(R.id.imageViewDetailsDrawer);
        imageViewDetailsDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invertGroup(R.string.details_group, R.id.imageViewDetailsDrawer, R.drawable.blue_uparrow, R.drawable.blue_circle_plus);
            }
        });
        ImageView imageViewHealthPlanDrawer = (ImageView)findViewById(R.id.imageViewHealthPlanDrawer);
        imageViewHealthPlanDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invertGroup(R.string.health_plan_group_tag, R.id.imageViewHealthPlanDrawer, R.drawable.blue_uparrow, R.drawable.blue_circle_plus);
            }
        });
        ImageView imageViewDependentsDrawer = (ImageView)findViewById(R.id.imageViewDependentsDrawer);
        imageViewDependentsDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invertGroup(R.string.dependents_group_tag, R.id.imageViewDependentsDrawer, R.drawable.blue_uparrow, R.drawable.blue_circle_plus);
            }
        });

        setVisibility(R.string.details_group, true, R.id.imageViewDetailsDrawer, R.drawable.blue_uparrow, R.drawable.blue_circle_plus);
        setVisibility(R.string.health_plan_group_tag, false, R.id.imageViewHealthPlanDrawer, R.drawable.blue_uparrow, R.drawable.blue_circle_plus);
        setVisibility(R.string.dependents_group_tag, false, R.id.imageViewDependentsDrawer, R.drawable.blue_uparrow, R.drawable.blue_circle_plus);
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
    public void doThis(Events.Employee  employeeEvent) {
        this.employeeEvent = employeeEvent;
        populate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.BrokerClient brokerClient){
        this.brokerClient = brokerClient;
        populate();
    }

    private void populate() {
        if (employeeEvent == null
            || brokerClient == null){
            return;
        }

        BrokerClient brokerClient = this.brokerClient.getBrokerClient();
        BrokerClientDetails brokerClientDetails = this.brokerClient.getBrokerClientDetails();
        final Employee employee = employeeEvent.getEmployee();

        Resources resources = getResources();

        TextView textViewEmployeeName = (TextView) findViewById(R.id.textViewEmployeeName);
        textViewEmployeeName.setText(employee.getFullName());
        coverageYear = "active";
        Spinner spinnerCoverageYear = (Spinner) findViewById(R.id.spinnerCoverageYear);
        spinnerCoverageYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                if (pos == 0){
                    coverageYear = "active";
                } else {
                    coverageYear = "renewal";
                }
                populateCoverageYearDependencies(employee, EmployeeDetailsActivity.this.getResources());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        DateTime planYearBegins = brokerClientDetails.planYearBegins;
        DateTime oneYearOut = new DateTime(planYearBegins.getYear() + 1, planYearBegins.getMonthOfYear(), planYearBegins.getDayOfMonth(), planYearBegins.getHourOfDay(), planYearBegins.getMinuteOfHour());

        String thisYear = String.format("%s - %s", Utilities.DateAsMonthYear(brokerClientDetails.planYearBegins), Utilities.DateAsMonthYear(Utilities.calculateOneYearOut(brokerClientDetails.planYearBegins)));
        String nextYear = String.format("%s - %s", Utilities.DateAsMonthYear(oneYearOut), Utilities.DateAsMonthYear(Utilities.calculateOneYearOut(oneYearOut)));

        List<String> list = new ArrayList<>();
        list.add(thisYear);
        list.add(nextYear);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        spinnerCoverageYear.setAdapter(dataAdapter);

        TextView textViewEnrollmentStatus = (TextView) findViewById(R.id.textViewEnrollmentStatus);
        textViewEnrollmentStatus.setText(employee.enrollments.active.health.status);
        textViewEnrollmentStatus.setTextColor(ContextCompat.getColor(this, Utilities.colorFromEmployeeStatus(employee.enrollments.active.health.status)));

        if (employee.enrollments.renewal != null){
            TextView textViewEnrollmentStatusNextYear = (TextView) findViewById(R.id.textViewEnrollmentStatusNextYear);
            textViewEnrollmentStatusNextYear.setText(employee.enrollments.renewal.health.status);
            textViewEnrollmentStatus.setTextColor(ContextCompat.getColor(this, Utilities.colorFromEmployeeStatus(employee.enrollments.renewal.health.status)));
        }

        TextView textViewDobField = (TextView) findViewById(R.id.textViewDobField);
        textViewDobField.setText(String.format(resources.getString(R.string.dob_field_format), Utilities.DateAsString(employee.dateOfBirth)));

        TextView textViewSsnField = (TextView) findViewById(R.id.textViewSsnField);
        textViewSsnField.setText(String.format(resources.getString(R.string.ssn_field_format), employee.ssnMasked));

        TextView textViewHiredOn = (TextView) findViewById(R.id.textViewHiredOn);
        textViewHiredOn.setText(String.format(resources.getString(R.string.hired_on_field_format), Utilities.DateAsString(employee.hiredOn)));

        populateCoverageYearDependencies(employee, resources);
        // Populate the employee's dependants.
        RelativeLayout parent = (RelativeLayout) findViewById(R.id.relativeLayoutEmpoyeeDetails);
        int aboveId = R.id.textViewDependentsDrawer;
        for (Dependent dependent : employee.dependents) {
            View viewDependantRoot = LayoutInflater.from(this).inflate(R.layout.dependent_info, parent, false);
            TextView textViewName = (TextView) viewDependantRoot.findViewById(R.id.textViewName);
            textViewName.setText(Utilities.getFullName(dependent));

            TextView textViewGender = (TextView) viewDependantRoot.findViewById(R.id.textViewGender);
            textViewGender.setText(dependent.gender);

            TextView textViewDob = (TextView) viewDependantRoot.findViewById(R.id.textViewDob);
            textViewDob.setText(Utilities.DateAsMonthYear(dependent.dateOfBirth));

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

    private void populateCoverageYearDependencies(Employee employee, Resources resources) {
        Health health;
        if (coverageYear.compareToIgnoreCase("active") == 0){
            health = employee.enrollments.active.health;
        } else {
            health = employee.enrollments.renewal.health;
        }

        TextView textViewBenefitGroupField = (TextView) findViewById(R.id.textViewBenefitGroupField);
        if (health.benefitGroupName != null) {
            textViewBenefitGroupField.setText(String.format(resources.getString(R.string.benefit_group_field), health.benefitGroupName));
        } else {
            textViewBenefitGroupField.setText(String.format(resources.getString(R.string.benefit_group_field), health.status));
        }

        TextView textViewPlanNameField = (TextView) findViewById(R.id.textViewPlanNameField);
        if (health.planName != null) {
            textViewPlanNameField.setText(String.format(resources.getString(R.string.plan_name_field), health.planName));
        } else {
            textViewPlanNameField.setText(String.format(resources.getString(R.string.plan_name_field), health.status));
        }
        TextView textViewPlanStartField = (TextView) findViewById(R.id.textViewPlanStartField);
        if (health.terminatedOn != null){
            textViewPlanStartField.setText(String.format(resources.getString(R.string.plan_start_field), health.terminatedOn));
        } else {
            textViewPlanStartField.setText(String.format(resources.getString(R.string.plan_start_field), health.status));
        }
        TextView textViewMetalLevelField = (TextView) findViewById(R.id.textViewMetalLevelField);
        if (health.metalLevel != null){
            textViewMetalLevelField.setText(String.format(resources.getString(R.string.metal_level_field), health.metalLevel));
        } else {
            textViewMetalLevelField.setText(String.format(resources.getString(R.string.metal_level_field), health.status));
        }
        TextView textViewPremiums = (TextView) findViewById(R.id.textViewPremiums);
        textViewPremiums.setText(String.format("%.2f", health.totalPremium));

        TextView textViewEmployerContribution = (TextView) findViewById(R.id.textViewEmployerContribution);
        textViewEmployerContribution.setText(String.format("%.2f", health.employerContribution));

        TextView textViewEmployeeCost = (TextView) findViewById(R.id.textViewEmployeeCost);
        textViewEmployeeCost.setText(String.format("%.2f", health.employeeCost));
    }

    public int findId(){
        int id = R.id.relativeLayoutEmpoyeeDetails;
        View v = findViewById(id);
        while (v != null){
            v = findViewById(++id);
        }
        return id;
    }
}
