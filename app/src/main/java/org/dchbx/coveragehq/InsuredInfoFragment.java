package org.dchbx.coveragehq;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.dchbx.coveragehq.models.roster.Dependent;
import org.dchbx.coveragehq.models.roster.Enrollment;
import org.dchbx.coveragehq.models.roster.Health;
import org.dchbx.coveragehq.models.roster.RosterEntry;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

/**
 * Created by plast on 3/19/2017.
 */

public class InsuredInfoFragment extends BrokerFragment {
    private static String TAG = "EmployeeInfoFragment";


    private String employeeId;
    private String employerId;
    private RosterEntry employee;
    private LocalDate coverageYear;

    private boolean detailsVisible = true;
    private boolean healthPlanVisible = false;
    private boolean dentalPlanVisible = false;
    private boolean dependentsVisible = false;

    private View view;
    private TextView textViewDetailsDrawer;
    private RelativeLayout relativeLayoutDetailsWrapper;
    private TextView textViewHealthPlanDrawer;
    private TextView textViewDentalPlanDrawer;
    private TextView textViewDependentsDrawer;
    private TextView textViewDentalPlanNotEnrolled;
    private RelativeLayout relativeLayoutDentalPlanWrapper;
    private TextView textViewNotEnrolled;
    private RelativeLayout relativeLayoutHealthPlanWrapper;
    private RelativeLayout relativeLayoutDependentsWrapper;


    private LocalDate currentDate;
    private Enrollment currentEnrollment;
    private Resources resources = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            view = LayoutInflater.from(getActivity()).inflate(R.layout.insured_info_fragment, null);
        } catch (Exception e){
            Log.e(TAG, "Exception infloating view", e);
            throw e;
        }
        init();
        getViews();
        getMessages().getInsured();
        return view;
    }

    private void getViews() {
        textViewDetailsDrawer = (TextView) view.findViewById(R.id.textViewDetailsDrawer);
        relativeLayoutDetailsWrapper = (RelativeLayout) view.findViewById(R.id.relativeLayoutDetailsWrapper);
        textViewHealthPlanDrawer = (TextView) view.findViewById(R.id.textViewHealthPlanDrawer);
        textViewDentalPlanDrawer = (TextView) view.findViewById(R.id.textViewDentalPlanDrawer);
        textViewDependentsDrawer = (TextView) view.findViewById(R.id.textViewDependentsDrawer);
        textViewNotEnrolled = (TextView) view.findViewById(R.id.textViewHealthPlanNotEnrolled);
        relativeLayoutHealthPlanWrapper = (RelativeLayout) view.findViewById(R.id.relativeLayoutHealthPlanWrapper);
        textViewDentalPlanNotEnrolled = (TextView) view.findViewById(R.id.textViewDentalPlanNotEnrolled);
        relativeLayoutDentalPlanWrapper = (RelativeLayout) view.findViewById(R.id.relativeLayoutDentalPlanWrapper);
        textViewDentalPlanNotEnrolled = (TextView) view.findViewById(R.id.textViewDentalPlanNotEnrolled);
        relativeLayoutDependentsWrapper = (RelativeLayout) view.findViewById(R.id.relativeLayoutDependentsWrapper);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Employee getUserEmployeeResults) throws Exception {
        employee = this.employee = getUserEmployeeResults.getEmployee();
        currentDate = BrokerUtilities.getMostRecentPlanYear(employee);
        this.currentEnrollment = BrokerUtilities.getEnrollment(employee, currentDate);
        coverageYear = BrokerUtilities.getMostRecentPlanYear(employee);

        populate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.EmployeeFragmentUpdate  employeeFragmentUpdate) throws Exception {
        this.employee = employeeFragmentUpdate.employee;
        this.currentDate = employeeFragmentUpdate.currentEnrollmentStartDate;
        this.currentEnrollment = BrokerUtilities.getEnrollment(employee, currentDate);

        if (coverageYear == null){
            coverageYear = BrokerUtilities.getMostRecentPlanYear(employee);
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

        resources = getResources();
        LocalDate initialCoverageYear = new LocalDate(2000, 1, 1);

        populateHealthPlan();

        TextView textViewDobField = (TextView) view.findViewById(R.id.textViewDobField);
        textViewDobField.setText(String.format(resources.getString(R.string.dob_field_format), Utilities.DateAsString(employee.dateOfBirth)));

        TextView textViewSsnField = (TextView) view.findViewById(R.id.textViewSsnField);
        textViewSsnField.setText(String.format(resources.getString(R.string.ssn_field_format), employee.ssnMasked));

        // Populate the employee's dependants.
        RelativeLayout parent = (RelativeLayout) view.findViewById(R.id.relativeLayoutDependentsWrapper);
        int aboveId = R.id.textViewDependentsDrawer;
        for (Dependent dependent : employee.dependents) {
            View viewDependantRoot = LayoutInflater.from(this.getActivity()).inflate(R.layout.dependent_info, parent, false);
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

        configureDrawers();
    }

    private void configureDrawers() {

        textViewDetailsDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (detailsVisible){
                    detailsVisible = false;
                    relativeLayoutDetailsWrapper.setVisibility(View.GONE);
                } else {
                    detailsVisible = true;
                    relativeLayoutDetailsWrapper.setVisibility(View.VISIBLE);
                }
            }
        });

        textViewHealthPlanDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (healthPlanVisible){
                    healthPlanVisible = false;
                } else {
                    healthPlanVisible = true;
                }
            }
        });

        textViewDentalPlanDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dentalPlanVisible){
                    dentalPlanVisible = false;
                } else {
                    dentalPlanVisible = true;

                }
            }
        });

        textViewDependentsDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dependentsVisible){
                    dependentsVisible = false;
                    relativeLayoutDependentsWrapper.setVisibility(View.GONE);
                } else {
                    dependentsVisible = true;
                    relativeLayoutDependentsWrapper.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void populateDentalPlan() throws Exception {
        if (currentEnrollment == null
            || currentEnrollment.dental == null){
            textViewDentalPlanNotEnrolled.setVisibility(View.VISIBLE);
            relativeLayoutDentalPlanWrapper.setVisibility(View.GONE);
        }
        Health dental = currentEnrollment.dental;
        textViewDentalPlanNotEnrolled.setVisibility(View.GONE);
        relativeLayoutDentalPlanWrapper.setVisibility(View.VISIBLE);
    }

    private void populateHealthPlan() throws Exception {
        TextView textViewNotEnrolled = (TextView) view.findViewById(R.id.textViewHealthPlanNotEnrolled);
        RelativeLayout relativeLayoutPlanWrapper = (RelativeLayout) view.findViewById(R.id.relativeLayoutHealthPlanWrapper);
        if (currentEnrollment == null
            || currentEnrollment.health == null){
            textViewNotEnrolled.setVisibility(View.VISIBLE);
            relativeLayoutPlanWrapper.setVisibility(View.GONE);
            return;
        }
        Health health = currentEnrollment.health;
        textViewNotEnrolled.setVisibility(View.GONE);
        relativeLayoutPlanWrapper.setVisibility(View.VISIBLE);


        TextView textViewBenefitGroupField = (TextView) view.findViewById(R.id.textViewBenefitGroupField);
        if (health.benefitGroupName != null) {
            textViewBenefitGroupField.setText(String.format(resources.getString(R.string.benefit_group_field), health.benefitGroupName));
        } else {
            textViewBenefitGroupField.setText(String.format(resources.getString(R.string.benefit_group_field), health.status));
        }

        TextView textViewPlanNameField = (TextView) view.findViewById(R.id.textViewPlanNameField);
        if (health.planName != null) {
            textViewPlanNameField.setText(String.format(resources.getString(R.string.plan_name_field), health.planName));
        } else {
            textViewPlanNameField.setText(String.format(resources.getString(R.string.plan_name_field), health.status));
        }
        TextView textViewPlanStartField = (TextView) view.findViewById(R.id.textViewPlanStartField);
        if (health.terminatedOn != null){
            textViewPlanStartField.setText(String.format(resources.getString(R.string.plan_start_field), health.terminatedOn));
        } else {
            textViewPlanStartField.setText(String.format(resources.getString(R.string.plan_start_field), Utilities.DateAsString(coverageYear)));
        }
        TextView textViewMetalLevelField = (TextView) view.findViewById(R.id.textViewMetalLevelField);
        if (health.metalLevel != null){
            textViewMetalLevelField.setText(String.format(resources.getString(R.string.metal_level_field), health.metalLevel));
        } else {
            textViewMetalLevelField.setText(String.format(resources.getString(R.string.metal_level_field), health.status));
        }
        TextView textViewPremiums = (TextView) view.findViewById(R.id.textViewPremium);
        String totalPremiumString = "";
        if (health.totalPremium != null) {
            totalPremiumString = String.format("$%.2f", health.totalPremium);
        }
        textViewPremiums.setText(totalPremiumString);

    }

    public int findId(){
        int id = R.id.relativeLayoutEmpoyeeDetails;
        View v = view.findViewById(id);
        while (v != null){
            v = view.findViewById(++id);
        }
        return id;
    }
}
