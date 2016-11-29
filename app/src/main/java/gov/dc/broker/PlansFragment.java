package gov.dc.broker;

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

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import gov.dc.broker.models.brokerclient.Active;
import gov.dc.broker.models.brokerclient.BrokerClientDetails;
import gov.dc.broker.models.brokerclient.ElectedDentalPlan;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by plast on 10/21/2016.
 */

public class PlansFragment extends BrokerFragment {
    private BrokerClientDetails brokerClientDetails = null;
    private String coverageYear;
    private View view;
    private int brokerClientId;

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

        if (brokerClientDetails == null) {
            brokerClientId = getBrokerActivity().getIntent().getIntExtra(Intents.BROKER_CLIENT_ID, -1);
            if (brokerClientId == -1) {
                // If we get here the employer id in the intent wasn't initialized and
                // we are in a bad state.
                Log.e(TAG, "onCreate: no client id found in intent");
                return view;
            }
            getMessages().getEmployer(brokerClientId);
        } else {
            populate();
        }

        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.CoverageYear coverageYear) {
        this.coverageYear = coverageYear.getYear();
        populate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.BrokerClient brokerClient) {
        brokerClientDetails = brokerClient.getBrokerClientDetails();
        EmployerDetailsActivity activity = (EmployerDetailsActivity) getActivity();
        this.coverageYear = activity.getCoverageYear();
        populate();

    }

    private void populate() {
        List<Active> planOfferings;
        if (coverageYear.compareToIgnoreCase("active") == 0){
            planOfferings = brokerClientDetails.planOfferings.active;
        } else {
            planOfferings = brokerClientDetails.planOfferings.renewal;
        }


        FragmentActivity activity = this.getActivity();
        LinearLayout linearLayoutPlans = (LinearLayout) view.findViewById(R.id.linearLayoutPlans);
        linearLayoutPlans.removeAllViews();
        for (Active planOffering : planOfferings) {
            final View planRoot = LayoutInflater.from(activity).inflate(R.layout.plan_item, (ViewGroup)view, false);

            TextView textViewDrawerLabel = (TextView) planRoot.findViewById(R.id.textViewDrawerLabel);
            textViewDrawerLabel.setText(planOffering.benefitGroupName);
            final RelativeLayout planDetailsLayout = (RelativeLayout) planRoot.findViewById(R.id.relativeLayoutPlanDetails);
            planDetailsLayout.setVisibility(View.GONE);
            ImageView imageView = (ImageView) planRoot.findViewById(R.id.imageView);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (planDetailsLayout.getVisibility() == View.VISIBLE){
                        planDetailsLayout.setVisibility(View.GONE);
                    } else {
                        planDetailsLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
            TextView textViewPlanOffered = (TextView) planRoot.findViewById(R.id.textViewPlanOffered);
            if (planOffering.health.planOptionKind.compareToIgnoreCase("single_carrier") == 0){
                textViewPlanOffered.setText(planOffering.health.referencePlanName);

            } else {
                textViewPlanOffered.setText(planOffering.health.plansBySummaryText);
            }
            TextView textViewEligibility = (TextView) planRoot.findViewById(R.id.textViewEligibility);
            textViewEligibility.setText(planOffering.eligibilityRule);
            TextView textViewEmployeeContribution = (TextView) planRoot.findViewById(R.id.textViewEmployeeContribution);
            textViewEmployeeContribution.setText(Integer.toString(planOffering.health.employerContributionByRelationship.employee));
            TextView textViewSpouseContribution = (TextView) planRoot.findViewById(R.id.textViewSpouseContribution);
            textViewSpouseContribution.setText(Integer.toString(planOffering.health.employerContributionByRelationship.spouse));
            TextView textViewDomesticPartnerLevel = (TextView) planRoot.findViewById(R.id.textViewDomesticPartnerLevel);
            textViewDomesticPartnerLevel.setText(Integer.toString(planOffering.health.employerContributionByRelationship.domesticPartner));
            TextView textViewChildContributionLevel = (TextView) planRoot.findViewById(R.id.textViewChildContributionLevel);
            textViewChildContributionLevel.setText(Integer.toString(planOffering.health.employerContributionByRelationship.childUnder26));
            TextView textViewReferencePlan = (TextView) planRoot.findViewById(R.id.textViewReferencePlan);
            if (planOffering.health.planOptionKind.compareToIgnoreCase("single_carrier") == 0) {
                planRoot.findViewById(R.id.textViewReferencePlanLabel).setVisibility(View.GONE);
                textViewReferencePlan.setVisibility(View.GONE);
            } else {
                textViewReferencePlan.setText(planOffering.health.referencePlanName);
            }

            TextView textViewNoPlanAvailableLabel = (TextView) planRoot.findViewById(R.id.textViewNoPlanAvailableLabel);
            RelativeLayout relativeLayoutDentalPlan = (RelativeLayout) planRoot.findViewById(R.id.relativeLayoutDentalPlan);

            if (planOffering.dental == null){
                relativeLayoutDentalPlan.setVisibility(View.GONE);
            } else {
                textViewNoPlanAvailableLabel.setVisibility(View.GONE);

                RelativeLayout relativeLayoutElectedDentalPlans = (RelativeLayout) planRoot.findViewById(R.id.relativeLayoutElectedDentalPlans);
                TextView textViewNoDentalPlansLabel = (TextView)planRoot.findViewById(R.id.textViewNoDentalPlansLabel);
                if (planOffering.dental.electedDentalPlans != null){
                    relativeLayoutElectedDentalPlans.setVisibility(View.VISIBLE);
                    textViewNoDentalPlansLabel.setVisibility(View.GONE);
                    TextView textViewDentalEligibility = (TextView) planRoot.findViewById(R.id.textViewDentalEligibility);
                    textViewDentalEligibility.setText(planOffering.eligibilityRule);
                    int previousId = R.id.textViewElectedDentalPlansLabel;
                    for (ElectedDentalPlan electedDentalPlan : planOffering.dental.electedDentalPlans) {
                        View electedPlanRoot = LayoutInflater.from(activity).inflate(R.layout.elected_dental_plan_item, (ViewGroup)view, false);
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
                textViewDentalEmployeeContribution.setText(Integer.toString(planOffering.dental.employerContributionByRelationship.employee));
                TextView textViewDentalSpouseContribution = (TextView) planRoot.findViewById(R.id.textViewDentalSpouseContribution);
                textViewDentalSpouseContribution.setText(Integer.toString(planOffering.dental.employerContributionByRelationship.spouse));
                TextView textViewDentalDomesticPartnerLevel = (TextView) planRoot.findViewById(R.id.textViewDentalDomesticPartnerLevel);
                textViewDentalDomesticPartnerLevel.setText(Integer.toString(planOffering.dental.employerContributionByRelationship.domesticPartner));
                TextView textViewDentalChildContributionLevel = (TextView) planRoot.findViewById(R.id.textViewDentalChildContributionLevel);
                textViewDentalChildContributionLevel.setText(Integer.toString(planOffering.dental.employerContributionByRelationship.childUnder26));
                TextView textViewDentalReferencePlan = (TextView) planRoot.findViewById(R.id.textViewDentalReferencePlan);
                textViewDentalReferencePlan.setText(planOffering.dental.referencePlanName);
            }

            linearLayoutPlans.addView(planRoot);
            int id = findId(view);


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
