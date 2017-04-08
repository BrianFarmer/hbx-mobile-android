package org.dchbx.coveragehq;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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

public class EmployeeInfoFragment extends BrokerFragment {
    private static String TAG = "EmployeeInfoFragment";


    private String employeeId;
    private String employerId;
    private RosterEntry employee;
    private LocalDate coverageYear;

    private boolean detailsVisible = true;
    private boolean healthPlanVisible = false;
    private boolean dependentsVisible = false;

    private View view;
    private LocalDate currentDate;
    private Enrollment currentEnrollment;
    private Resources resources = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            view = LayoutInflater.from(getActivity()).inflate(R.layout.employee_info_fragment, null);
        } catch (Exception e){
            Log.e(TAG, "Exception infloating view", e);
            throw e;
        }
        init();
        getMessages().getUserEmployee();
        return view;
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
        BrokerUtilities.getEnrollment(employee, currentDate);

        populateCoverageYearDependencies();

        TextView textViewDobField = (TextView) view.findViewById(R.id.textViewDobField);
        textViewDobField.setText(String.format(resources.getString(R.string.dob_field_format), Utilities.DateAsString(employee.dateOfBirth)));

        TextView textViewSsnField = (TextView) view.findViewById(R.id.textViewSsnField);
        textViewSsnField.setText(String.format(resources.getString(R.string.ssn_field_format), employee.ssnMasked));

        TextView textViewHiredOn = (TextView) view.findViewById(R.id.textViewHiredOn);
        textViewHiredOn.setText(String.format(resources.getString(R.string.hired_on_field_format), Utilities.DateAsString(employee.hiredOn)));

        // Populate the employee's dependants.
        RelativeLayout parent = (RelativeLayout) view.findViewById(R.id.relativeLayoutEmpoyeeDetails);
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

        //setVisibility(R.string.dependents_group_tag, false, R.id.imageViewDependentsDrawer, R.drawable.blue_uparrow, R.drawable.blue_circle_plus);
    }

    private void populateCoverageYearDependencies() throws Exception {
        Health health = currentEnrollment.health;

        TextView textViewEnrollmentStatus = (TextView) view.findViewById(R.id.textViewEnrollmentStatus);
        textViewEnrollmentStatus.setText(currentEnrollment.health.status);
        textViewEnrollmentStatus.setTextColor(ContextCompat.getColor(getActivity(), Utilities.colorFromEmployeeStatus(currentEnrollment.health.status)));


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
        TextView textViewPremiums = (TextView) view.findViewById(R.id.textViewPremiums);
        String totalPremiumString = "";
        if (health.totalPremium != null) {
            totalPremiumString = String.format("$%.2f", health.totalPremium);
        }
        textViewPremiums.setText(totalPremiumString);

        TextView textViewEmployerContribution = (TextView) view.findViewById(R.id.textViewEmployerContribution);
        String employerContributionString = "";
        if (health.employerContribution != null) {
            employerContributionString = String.format("$%.2f", health.employerContribution);
        }
        textViewEmployerContribution.setText(employerContributionString);

        TextView textViewEmployeeCost = (TextView) view.findViewById(R.id.textViewEmployeeCost);
        String employeeCostString = "";
        if (health.employeeCost != null){
            employeeCostString = String.format("$%.2f", health.employeeCost);
        }
        textViewEmployeeCost.setText(employeeCostString);
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
