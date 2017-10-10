package org.dchbx.coveragehq;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.dchbx.coveragehq.models.roster.Address;
import org.dchbx.coveragehq.models.roster.Dependent;
import org.dchbx.coveragehq.models.roster.Enrollment;
import org.dchbx.coveragehq.models.roster.Health;
import org.dchbx.coveragehq.models.roster.RosterEntry;
import org.dchbx.coveragehq.planshopping.PlanUtilities;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

/**
 * Created by plast on 3/19/2017.
 */

public class InsuredInfoFragment extends BrokerFragment {
    private static String TAG = "InsuredInfoFragment";


    private RosterEntry insured;
    private LocalDate coverageYear;

    private static boolean detailsVisibleInitialState = true;
    private static boolean healthPlanVisibleInitialState = true;
    private static boolean dentalPlanVisibleInitialState = false;
    private static boolean dependentsVisibleInitialState = false;

    private boolean detailsVisible = detailsVisibleInitialState;
    private boolean healthPlanVisible = healthPlanVisibleInitialState;
    private boolean dentalPlanVisible = dentalPlanVisibleInitialState;
    private boolean dependentsVisible = dependentsVisibleInitialState;

    private View view;
    private TextView textViewDetailsDrawer;
    private RelativeLayout relativeLayoutDetailsWrapper;
    private TextView textViewHealthPlanDrawer;
    private TextView textViewDentalPlanDrawer;
    private TextView textViewDependentsDrawer;
    private TextView dependentsCount;
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
    private Spinner spinnerCoverageYear;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            view = LayoutInflater.from(getActivity()).inflate(R.layout.insured_info_fragment, null);
        } catch (Exception e) {
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
        dependentsCount = (TextView) view.findViewById(R.id.dependentsCount);
        relativeLayoutDependentsWrapper = (RelativeLayout) view.findViewById(R.id.relativeLayoutDependentsWrapper);
        spinnerCoverageYear = (Spinner)view.findViewById(R.id.spinnerCoverageYear);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Employee getUserEmployeeResults) throws Exception {
        this.insured = getUserEmployeeResults.getEmployee();
        currentDate = BrokerUtilities.getMostRecentPlanYear(insured);
        this.currentEnrollment = BrokerUtilities.getEnrollment(insured, currentDate);
        coverageYear = BrokerUtilities.getMostRecentPlanYear(insured);
        dependentsCount.setText(Integer.toString(insured.dependents.size()));

        populate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.EmployeeFragmentUpdate employeeFragmentUpdate) throws Exception {
        this.currentDate = employeeFragmentUpdate.currentEnrollmentStartDate;
        this.currentEnrollment = BrokerUtilities.getEnrollment(insured, currentDate);

        if (coverageYear == null) {
            coverageYear = BrokerUtilities.getMostRecentPlanYear(insured);
        }

        try {
            populate();
        } catch (Exception e) {
            Log.e(TAG, "exception populating activity", e);
        }
    }

    private void configDrawer(int drawerLabelId, int drawerImageId, int drawerWrapper, boolean initialState, final DrawerState drawerState)  {
        TextView drawerLabel = (TextView) view.findViewById(drawerLabelId);
        final ImageView drawerImage = (ImageView) view.findViewById(drawerImageId);
        final RelativeLayout wrapper = (RelativeLayout) view.findViewById(drawerWrapper);

        if (initialState){
            wrapper.setVisibility(View.VISIBLE);
            drawerImage.setImageResource(R.drawable.blue_uparrow);
        } else {
            wrapper.setVisibility(View.GONE);
            drawerImage.setImageResource(R.drawable.blue_circle_plus);
        }

        drawerLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invertDrawer(wrapper, drawerState, drawerImage);
            }
        });
        drawerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invertDrawer(wrapper, drawerState, drawerImage);
            }
        });
    }

    private void configDependentsDrawer(int drawerLabelId, int drawerImageId, int drawerWrapper, boolean initialState, final DrawerState drawerState)  {
        TextView drawerLabel = (TextView) view.findViewById(drawerLabelId);
        final ImageView drawerImage = (ImageView) view.findViewById(drawerImageId);
        final RelativeLayout wrapper = (RelativeLayout) view.findViewById(drawerWrapper);

        if (initialState){
            wrapper.setVisibility(View.VISIBLE);
            drawerImage.setImageResource(R.drawable.blue_uparrow);
        } else {
            wrapper.setVisibility(View.GONE);
            drawerImage.setImageResource(R.drawable.blue_circle_vector);
        }

        drawerLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invertDependentsDrawer(wrapper, drawerState, drawerImage);
            }
        });
        drawerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invertDependentsDrawer(wrapper, drawerState, drawerImage);
            }
        });
    }

    private void invertDrawer(RelativeLayout wrapper, DrawerState drawerState, ImageView drawerImage) {
        boolean currentState = drawerState.drawerState();

        if (currentState) {
            wrapper.setVisibility(View.GONE);
            drawerState.setState(false);
            drawerImage.setImageResource(R.drawable.blue_circle_plus);
        } else {
            wrapper.setVisibility(View.VISIBLE);
            drawerState.setState(true);
            drawerImage.setImageResource(R.drawable.blue_uparrow);
        }
    }

    private void invertDependentsDrawer(RelativeLayout wrapper, DrawerState drawerState, ImageView drawerImage) {
        boolean currentState = drawerState.drawerState();

        if (currentState) {
            wrapper.setVisibility(View.GONE);
            drawerState.setState(false);
            drawerImage.setImageResource(R.drawable.blue_circle_vector);
            dependentsCount.setVisibility(View.VISIBLE);
        } else {
            wrapper.setVisibility(View.VISIBLE);
            drawerState.setState(true);
            drawerImage.setImageResource(R.drawable.blue_uparrow);
            dependentsCount.setVisibility(View.GONE);
        }
    }

    private void populate() throws Exception {
        if (insured == null) {
            return;
        }

        resources = getResources();
        LocalDate initialCoverageYear = new LocalDate(2000, 1, 1);

        configDrawer(R.id.textViewDetailsDrawer, R.id.imageViewDetailsDrawer, R.id.relativeLayoutDetailsWrapper, detailsVisibleInitialState, new DrawerState() {
            public boolean drawerState() {
                return InsuredInfoFragment.this.detailsVisible;
            }

            @Override
            public void setState(boolean newState) {
                InsuredInfoFragment.this.detailsVisible = newState;
            }
        });

        configDrawer(R.id.textViewHealthPlanDrawer, R.id.imageViewHealthPlanDrawer, R.id.healthPlanInfo, healthPlanVisibleInitialState, new DrawerState() {
            public boolean drawerState() {
                return InsuredInfoFragment.this.healthPlanVisible;
            }

            @Override
            public void setState(boolean newState) {
                InsuredInfoFragment.this.healthPlanVisible = newState;
            }
        });

        configDrawer(R.id.textViewDentalPlanDrawer, R.id.imageViewDentalPlanDrawer, R.id.dentalPlanInfo, dentalPlanVisibleInitialState, new DrawerState() {
            public boolean drawerState() {
                return InsuredInfoFragment.this.dentalPlanVisible;
            }

            @Override
            public void setState(boolean newState) {
                InsuredInfoFragment.this.dentalPlanVisible = newState;
            }
        });

        configDependentsDrawer(R.id.textViewDependentsDrawer, R.id.imageViewDependentsDrawer, R.id.relativeLayoutDependentsWrapper, dependentsVisibleInitialState, new DrawerState() {
            public boolean drawerState() {
                return InsuredInfoFragment.this.detailsVisible;
            }

            @Override
            public void setState(boolean newState) {
                InsuredInfoFragment.this.detailsVisible = newState;
            }
        });

        populateHealthPlan();
        populateDentalPlan();

        TextView textViewDobField = (TextView) view.findViewById(R.id.textViewDobField);
        textViewDobField.setText(String.format(resources.getString(R.string.dob_field_format), Utilities.DateAsString(insured.dateOfBirth)));

        TextView textViewSsnField = (TextView) view.findViewById(R.id.textViewSsnField);
        textViewSsnField.setText(String.format(resources.getString(R.string.ssn_field_format), insured.ssnMasked));


        TextView address1 = (TextView) view.findViewById(R.id.firstName);
        TextView address2 = (TextView) view.findViewById(R.id.address2);
        TextView cityStateZip = (TextView) view.findViewById(R.id.cityStateZip);
        TextView phoneNumber = (TextView) view.findViewById(R.id.phoneNumber);
        TextView emailAddress = (TextView) view.findViewById(R.id.city);

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

        if (currentEnrollment.dental != null
            && currentEnrollment.dental.status != null
            && currentEnrollment.dental.status.compareTo("Enrolled") == 0) {
            textViewDentalPlanDrawer.setVisibility(View.VISIBLE);

            textViewDentalPlanDrawer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dentalPlanVisible) {
                        dentalPlanVisible = false;
                    } else {
                        dentalPlanVisible = true;
                    }
                }
            });
            populatePlan(currentEnrollment.dental, R.id.dentalPlanInfo, false);
        } else {
            textViewDentalPlanDrawer.setVisibility(View.GONE);
            ImageView imageViewDentalPlanDrawer = (ImageView) this.view.findViewById(R.id.imageViewDentalPlanDrawer);
            imageViewDentalPlanDrawer.setVisibility(View.GONE);
            ViewGroup includeView = (ViewGroup) this.view.findViewById(R.id.dentalPlanInfo);
            includeView.setVisibility(View.GONE);
            includeView.setVisibility(View.GONE);
        }
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
        populatePlan(currentEnrollment.health, R.id.healthPlanInfo, true);
    }

    private void populatePlan(Health plan, int groupId, final Boolean health){
        ViewGroup includeView = (ViewGroup) this.view.findViewById(groupId);
        includeView.setVisibility(View.VISIBLE);
        carrier = plan.carrierName;
        final String extraCarrier = carrier;
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
        if (health){
            planSelected.setText(currentEnrollment.health.planName);
        } else {
            planSelected.setText(currentEnrollment.dental.planName);
        }
        TextView dcHealthLinkIdField = (TextView)includeView.findViewById(R.id.dcHealthLinkIdField);
        dcHealthLinkIdField.setText(plan.healthLinkId);
        TextView planTypeField = (TextView)includeView.findViewById(R.id.planTypeField);
        ImageView planMetalRing = (ImageView) includeView.findViewById(R.id.planMetalRing);
        TextView planMetalField = (TextView) includeView.findViewById(R.id.planMetalField);
        if (plan.planType != null) {
            planTypeField.setVisibility(View.VISIBLE);
            planTypeField.setText(String.format(resources.getString(R.string.plan_type_field),plan.planType));
        } else {
            planTypeField.setText(String.format(resources.getString(R.string.plan_type_field), resources.getString(R.string.not_available)));
        }

        if (plan.metalLevel != null){
            planMetalRing.setVisibility(View.VISIBLE);
            planMetalField.setVisibility(View.VISIBLE);
            planMetalField.setText(plan.metalLevel);
            planMetalRing.setImageResource(getMetalRingResource(plan.metalLevel));
        } else {
            planMetalRing.setVisibility(View.GONE);
            planMetalField.setVisibility(View.GONE);
        }

        TextView textViewAnnualPremium = (TextView) includeView.findViewById(R.id.textViewAnnualPremium);
        String totalPremiumString = "";
        String monthlyPremium = "";
        if (plan.totalPremium != null) {
            totalPremiumString = PlanUtilities.getFormattedAnnualPremium(plan);
            monthlyPremium = PlanUtilities.getFormattedMonthlyPremium(plan);
        }
        textViewAnnualPremium.setText(totalPremiumString);

        TextView textViewPremium = (TextView) includeView.findViewById(R.id.textViewPremium);
        textViewPremium.setText(monthlyPremium);

        LinearLayout aptcLayout = (LinearLayout) includeView.findViewById(R.id.aptcLayout);
        if (plan.applied_aptc_amount_in_cent != null
            && plan.applied_aptc_amount_in_cent > 0) {
            aptcLayout.setVisibility(View.VISIBLE);
            TextView textViewAptcCredit = (TextView) includeView.findViewById(R.id.textViewAptcCredit);
            String aptcCreditString = null;
            aptcCreditString = String.format("%.0f%%", plan.applied_aptc_amount_in_cent * 100);
            textViewAptcCredit.setText(aptcCreditString);
        } else {
            aptcLayout.setVisibility(View.GONE);
        }

        TextView textViewYearlyDeductible = (TextView) includeView.findViewById(R.id.textViewYearlyDeductible);
        String yearlyDeductibleString = PlanUtilities.getFormattedDeductible(plan);
        textViewYearlyDeductible.setText(yearlyDeductibleString);



        Button planContactInfo = (Button)includeView.findViewById(R.id.planContactInfo);
        if (carrier != null) {
            planContactInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PlanContactInfoDialog dialog = PlanContactInfoDialog.build(InsuredInfoFragment.this.getActivity(), extraCarrier);
                }
            });
        }
        Button summaryButton = (Button)includeView.findViewById(R.id.summary);
        summaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intents.launchSummaryOfBenefitsActivity(InsuredInfoFragment.this.getActivity(), currentDate, health);
            }
        });
    }

    private int getMetalRingResource(String metalLevel) {
        if (metalLevel.equalsIgnoreCase("silver")){
            return R.drawable.metal_silver;
        }
        if (metalLevel.equalsIgnoreCase("gold")){
            return R.drawable.metal_gold;
        }
        if (metalLevel.equalsIgnoreCase("platinum")){
            return R.drawable.metal_platinum;
        }
        return R.drawable.metal_bronze;
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

interface DrawerState {
    boolean drawerState ();
    void setState(boolean newState);
}