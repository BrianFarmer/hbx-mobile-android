package org.dchbx.coveragehq;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.dchbx.coveragehq.models.roster.Address;
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


    private RosterEntry insured;
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
    private String carrier;
    private Carriers carriers;

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
        relativeLayoutDependentsWrapper = (RelativeLayout) view.findViewById(R.id.relativeLayoutDependentsWrapper);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Employee getUserEmployeeResults) throws Exception {
        this.insured = getUserEmployeeResults.getEmployee();
        currentDate = BrokerUtilities.getMostRecentPlanYear(insured);
        this.currentEnrollment = BrokerUtilities.getEnrollment(insured, currentDate);
        coverageYear = BrokerUtilities.getMostRecentPlanYear(insured);

        populate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.EmployeeFragmentUpdate  employeeFragmentUpdate) throws Exception {
        this.insured = employeeFragmentUpdate.employee;
        this.currentDate = employeeFragmentUpdate.currentEnrollmentStartDate;
        this.currentEnrollment = BrokerUtilities.getEnrollment(insured, currentDate);

        if (coverageYear == null){
            coverageYear = BrokerUtilities.getMostRecentPlanYear(insured);
        }

        try {
            populate();
        } catch (Exception e){
            Log.e(TAG, "exception populating activity", e);
        }
    }

    private void populate() throws Exception {
        if (insured == null) {
            return;
        }

        resources = getResources();
        LocalDate initialCoverageYear = new LocalDate(2000, 1, 1);

        populateHealthPlan();
        populateDentalPlan();

        TextView textViewDobField = (TextView) view.findViewById(R.id.textViewDobField);
        textViewDobField.setText(String.format(resources.getString(R.string.dob_field_format), Utilities.DateAsString(insured.dateOfBirth)));

        TextView textViewSsnField = (TextView) view.findViewById(R.id.textViewSsnField);
        textViewSsnField.setText(String.format(resources.getString(R.string.ssn_field_format), insured.ssnMasked));


        TextView address1 = (TextView) view.findViewById(R.id.address1);
        TextView address2 = (TextView) view.findViewById(R.id.address2);
        TextView cityStateZip = (TextView) view.findViewById(R.id.cityStateZip);
        TextView phoneNumber = (TextView) view.findViewById(R.id.phoneNumber);
        TextView emailAddress = (TextView) view.findViewById(R.id.emailAddress);

        Address displayAddress = BrokerUtilities.getDisplayAddress(insured);
        if (displayAddress == null){
            address1.setVisibility(View.GONE);
            address2.setVisibility(View.GONE);
            cityStateZip.setVisibility(View.GONE);
        }

        if (displayAddress.address1 == null
            || displayAddress.address1.trim().length() == 0){
            address1.setVisibility(View.GONE);
        } else {
            address1.setVisibility(View.VISIBLE);
            address1.setText(displayAddress.address1);
        }
        if (displayAddress.address2 == null
            || displayAddress.address2.trim().length() == 0){
            address2.setVisibility(View.GONE);
        } else {
            address2.setVisibility(View.VISIBLE);
            address2.setText(displayAddress.address2);
        }
        if ((displayAddress.city == null
             || displayAddress.city.trim().length() == 0)
            && (displayAddress.state == null
                || displayAddress.state.trim().length() == 0)
            && (displayAddress.zip == null
             || displayAddress.zip.trim().length() == 0)){
            cityStateZip.setVisibility(View.GONE);
        } else {
            String cityStateZipString = "";
            if (displayAddress.city != null
                && displayAddress.city.trim().length() > 0) {
                cityStateZipString = displayAddress.city;
            }
            if (displayAddress.state != null
                    && displayAddress.state.trim().length() > 0) {
                if (cityStateZipString.length() > 0){
                    cityStateZipString += ", ";
                }
                cityStateZipString += displayAddress.state;
            }
            if (displayAddress.zip != null
                    && displayAddress.zip.trim().length() > 0) {
                if (cityStateZipString.length() > 0){
                    cityStateZipString += " ";
                }
                cityStateZipString += displayAddress.zip;
            }
            cityStateZip.setVisibility(View.VISIBLE);
            cityStateZip.setText(cityStateZipString);
        }
        if (insured.phone == null
                || insured.phone.trim().length() == 0){
            phoneNumber.setVisibility(View.GONE);
        } else {
            phoneNumber.setVisibility(View.VISIBLE);
            phoneNumber.setText(insured.phone);
        }
        if (insured.email == null
                || insured.email.trim().length() == 0){
            emailAddress.setVisibility(View.GONE);
        } else {
            emailAddress.setVisibility(View.VISIBLE);
            emailAddress.setText(insured.email);
        }

        // Populate the insured's dependants.
        RelativeLayout parent = (RelativeLayout) view.findViewById(R.id.relativeLayoutDependentsWrapper);
        int aboveId = R.id.textViewDependentsDrawer;
        for (Dependent dependent : insured.dependents) {
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
        ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.dentalPlanInfo);
        populatePlan(currentEnrollment.dental, viewGroup, false);
    }


    private void populateHealthPlan() throws Exception {
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
        ViewGroup view = (ViewGroup) this.view.findViewById(R.id.healthPlanInfo);
        populatePlan(currentEnrollment.health, (ViewGroup)view, true);
    }

    private void populatePlan(Health plan, ViewGroup includeView, final Boolean health){

        carrier = plan.carrierName;
        TextView textViewNotEnrolled = (TextView) includeView.findViewById(R.id.notEnrolled);
        RelativeLayout relativeLayoutPlanWrapper = (RelativeLayout) includeView.findViewById(R.id.wrapper);
        if (currentEnrollment == null
            || plan == null){
            textViewNotEnrolled.setVisibility(View.VISIBLE);
            relativeLayoutPlanWrapper.setVisibility(View.GONE);
            return;
        }
        textViewNotEnrolled.setVisibility(View.GONE);
        relativeLayoutPlanWrapper.setVisibility(View.VISIBLE);


        TextView planSelected = (TextView) includeView.findViewById(R.id.planSelected);
        planSelected.setText(String.format(resources.getString(R.string.plan_selected_field), Utilities.DateAsMonthDayYear(currentEnrollment.startOn)));

        TextView planIdField = (TextView) includeView.findViewById(R.id.planIdField);
        planIdField.setText(String.format(resources.getString(R.string.dc_health_link_id_field), insured.id));

        TextView planTypeField = (TextView)includeView.findViewById(R.id.planTypeField);
        planTypeField.setText(String.format(resources.getString(R.string.plan_type_field), plan.planType, plan.metalLevel));

        TextView textViewPremiums = (TextView) includeView.findViewById(R.id.textViewPremium);
        String totalPremiumString = "";
        if (plan.totalPremium != null) {
            totalPremiumString = String.format("$%.2f", plan.totalPremium);
        }
        textViewPremiums.setText(totalPremiumString);

        TextView textViewPremiumLabel = (TextView) includeView.findViewById(R.id.textViewPremiumLabel);
        if (plan.totalPremium != null) {
            totalPremiumString = String.format("$%.2f", plan.totalPremium/12);
        }
        textViewPremiums.setText(totalPremiumString);

        TextView textViewAptcCredit = (TextView) includeView.findViewById(R.id.textViewAptcCredit);
        String aptcCreditString = null;
        aptcCreditString = String.format("$%.2f", plan.totalPremium/12);
        textViewAptcCredit.setText(aptcCreditString);

        TextView textViewYearlyDeductable = (TextView) includeView.findViewById(R.id.textViewYearlyDeductable);
        String yearlyDeductableString = null;
        textViewYearlyDeductable.setText("$XXX.XX");



        Button planContactInfo = (Button)includeView.findViewById(R.id.planContactInfo);
        planContactInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlanContactInfoDialog dialog = PlanContactInfoDialog.build(InsuredInfoFragment.this.getActivity(), carrier);
            }
        });
        Button summaryButton = (Button)includeView.findViewById(R.id.summary);
        summaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intents.launchSummaryOfBenefitsActivity(InsuredInfoFragment.this.getActivity(), currentDate, health);
            }
        });
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
