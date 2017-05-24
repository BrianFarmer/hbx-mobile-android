package org.dchbx.coveragehq;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.microsoft.azure.mobile.analytics.Analytics;

import org.dchbx.coveragehq.models.employer.ElectedDentalPlan;
import org.dchbx.coveragehq.models.employer.Employer;
import org.dchbx.coveragehq.models.employer.PlanOffering;
import org.dchbx.coveragehq.models.employer.PlanYear;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by plast on 10/21/2016.
 */

public class PlansFragment extends BrokerFragment {
    private static String TAG = "PlansFragment";

    private Employer employer;
    private LocalDate coverageYear;
    private View view;
    private String brokerClientId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        init();

        view = LayoutInflater.from(getActivity()).inflate(R.layout.plans_fragment, null);

        if (employer == null) {
            brokerClientId = getBrokerActivity().getIntent().getStringExtra(Intents.BROKER_CLIENT_ID);
            if (brokerClientId == null) {
                Log.e(TAG, "onCreate: no client id found in intent");
                getMessages().getEmployer(null);
                return view;
            }
            getMessages().getEmployer(brokerClientId);
        } else {
            try {
                populate();
            } catch (Exception e) {
                Log.e(TAG, "exception populating PlansFragment", e);
            }
        }

        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.CoverageYear coverageYear) throws Exception {
        this.coverageYear = coverageYear.getYear();

        Map<String,String> properties=new HashMap<String,String>();
        properties.put("CurrentTab", "Plans");
        Analytics.trackEvent("Coverage Year Changed", properties);

        try{
            populate();
        } catch (Exception e){
            Log.e(TAG, "exception populating plans fragment in dothis(brokerclient)", e);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.BrokerClient brokerClient) throws Exception {

        employer = brokerClient.getEmployer();
        EmployerDetailsActivity activity = (EmployerDetailsActivity) getActivity();
        this.coverageYear = activity.getCoverageYear();
        try {
            populate();
        } catch (Exception e){
            Log.e(TAG, "exception populating plans fragment in dothis(brokerclient)", e);
        }
    }

    private void populate() throws Exception {
        PlanYear planYearForCoverageYear;
        planYearForCoverageYear = BrokerUtilities.getPlanYearForCoverageYear(employer, coverageYear);

        FragmentActivity activity = this.getActivity();
        LinearLayout linearLayoutPlans = (LinearLayout) view.findViewById(R.id.linearLayoutPlans);
        linearLayoutPlans.removeAllViews();
        if (planYearForCoverageYear != null) {
            for (PlanOffering planOffering : planYearForCoverageYear.planOfferings) {
                final View planRoot = LayoutInflater.from(activity).inflate(R.layout.plan_item, (ViewGroup) view, false);

                TextView textViewDrawerLabel = (TextView) planRoot.findViewById(R.id.textViewDrawerLabel);
                textViewDrawerLabel.setText(planOffering.benefitGroupName);
                final RelativeLayout planDetailsLayout = (RelativeLayout) planRoot.findViewById(R.id.relativeLayoutPlanDetails);
                planDetailsLayout.setVisibility(View.GONE);
                ImageView imageView = (ImageView) planRoot.findViewById(R.id.carrierLogo);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (planDetailsLayout.getVisibility() == View.VISIBLE) {
                            planDetailsLayout.setVisibility(View.GONE);
                        } else {
                            planDetailsLayout.setVisibility(View.VISIBLE);
                        }
                    }
                });
                TextView textViewPlanOffered = (TextView) planRoot.findViewById(R.id.textViewPlanOffered);
                if (planOffering.health.planOptionKind.compareToIgnoreCase("single_plan") == 0) {
                    textViewPlanOffered.setText(planOffering.health.referencePlanName);

                } else {
                    textViewPlanOffered.setText(planOffering.health.plansBySummaryText);
                }
                TextView textViewEligibility = (TextView) planRoot.findViewById(R.id.textViewEligibility);
                textViewEligibility.setText(planOffering.eligibilityRule);
                TextView textViewEmployeeContribution = (TextView) planRoot.findViewById(R.id.textViewEmployeeContribution);
                textViewEmployeeContribution.setText(Double.toString(planOffering.health.employerContributionByRelationship.employee));
                TextView textViewSpouseContribution = (TextView) planRoot.findViewById(R.id.textViewSpouseContribution);
                textViewSpouseContribution.setText(Double.toString(planOffering.health.employerContributionByRelationship.spouse));
                TextView textViewDomesticPartnerLevel = (TextView) planRoot.findViewById(R.id.textViewDomesticPartnerLevel);
                textViewDomesticPartnerLevel.setText(Double.toString(planOffering.health.employerContributionByRelationship.domesticPartner));
                TextView textViewChildContributionLevel = (TextView) planRoot.findViewById(R.id.textViewChildContributionLevel);
                textViewChildContributionLevel.setText(Double.toString(planOffering.health.employerContributionByRelationship.childUnder26));
                TextView textViewReferencePlan = (TextView) planRoot.findViewById(R.id.textViewReferencePlan);
                if (planOffering.health.planOptionKind.compareToIgnoreCase("single_plan") == 0) {
                    planRoot.findViewById(R.id.textViewReferencePlanLabel).setVisibility(View.GONE);
                    textViewReferencePlan.setVisibility(View.GONE);
                } else {
                    planRoot.findViewById(R.id.textViewReferencePlanLabel).setVisibility(View.VISIBLE);
                    textViewReferencePlan.setVisibility(View.VISIBLE);
                    textViewReferencePlan.setText(planOffering.health.referencePlanName);
                }

                TextView textViewNoPlanAvailableLabel = (TextView) planRoot.findViewById(R.id.textViewNoPlanAvailableLabel);
                RelativeLayout relativeLayoutDentalPlan = (RelativeLayout) planRoot.findViewById(R.id.relativeLayoutDentalPlan);

                if (planOffering.dental == null) {
                    relativeLayoutDentalPlan.setVisibility(View.GONE);
                } else {
                    textViewNoPlanAvailableLabel.setVisibility(View.GONE);

                    RelativeLayout relativeLayoutElectedDentalPlans = (RelativeLayout) planRoot.findViewById(R.id.relativeLayoutElectedDentalPlans);
                    TextView textViewNoDentalPlansLabel = (TextView) planRoot.findViewById(R.id.textViewNoDentalPlansLabel);
                    if (planOffering.dental.electedDentalPlans != null) {
                        relativeLayoutElectedDentalPlans.setVisibility(View.VISIBLE);
                        textViewNoDentalPlansLabel.setVisibility(View.GONE);
                        TextView textViewDentalEligibility = (TextView) planRoot.findViewById(R.id.textViewDentalEligibility);
                        textViewDentalEligibility.setText(planOffering.eligibilityRule);
                        int previousId = R.id.textViewElectedDentalPlansLabel;
                        for (ElectedDentalPlan electedDentalPlan : planOffering.dental.electedDentalPlans) {
                            View electedPlanRoot = LayoutInflater.from(activity).inflate(R.layout.elected_dental_plan_item, (ViewGroup) view, false);
                            TextView textViewElectedDentalName = (TextView) electedPlanRoot.findViewById(R.id.textViewElectedDentalName);
                            textViewElectedDentalName.setText(electedDentalPlan.planName);

                            relativeLayoutElectedDentalPlans.addView(electedPlanRoot);
                            int newId = findId(planRoot);
                            electedPlanRoot.setId(newId);

                            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                            p.addRule(RelativeLayout.BELOW, previousId);
                            p.addRule(RelativeLayout.ALIGN_PARENT_LEFT, previousId);
                            electedPlanRoot.setLayoutParams(p);
                            previousId = newId;
                        }
                    } else {
                        relativeLayoutElectedDentalPlans.setVisibility(View.GONE);
                        textViewNoDentalPlansLabel.setVisibility(View.VISIBLE);
                    }
                    TextView textViewDentalEmployeeContribution = (TextView) planRoot.findViewById(R.id.textViewDentalEmployeeContribution);
                    textViewDentalEmployeeContribution.setText(Double.toString(planOffering.dental.employerContributionByRelationship.employee));
                    TextView textViewDentalSpouseContribution = (TextView) planRoot.findViewById(R.id.textViewDentalSpouseContribution);
                    textViewDentalSpouseContribution.setText(Double.toString(planOffering.dental.employerContributionByRelationship.spouse));
                    TextView textViewDentalDomesticPartnerLevel = (TextView) planRoot.findViewById(R.id.textViewDentalDomesticPartnerLevel);
                    textViewDentalDomesticPartnerLevel.setText(Double.toString(planOffering.dental.employerContributionByRelationship.domesticPartner));
                    TextView textViewDentalChildContributionLevel = (TextView) planRoot.findViewById(R.id.textViewDentalChildContributionLevel);
                    textViewDentalChildContributionLevel.setText(Double.toString(planOffering.dental.employerContributionByRelationship.childUnder26));
                    TextView textViewDentalReferencePlan = (TextView) planRoot.findViewById(R.id.textViewDentalReferencePlan);
                    textViewDentalReferencePlan.setText(planOffering.dental.referencePlanName);
                }

                linearLayoutPlans.addView(planRoot);
                int id = findId(view);
            }
        }
    }
    public static int findId(View view){
        int id = R.id.relativeLayoutEmpoyeeDetails;
        View v = view.findViewById(id);
        while (v != null){
            v = view.findViewById(++id);
        }
        return id;
    }

}
