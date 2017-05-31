package org.dchbx.coveragehq;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.dchbx.coveragehq.models.planshopping.Plan;

import java.util.List;

/**
 * Created by plast on 5/18/2017.
 */

public class PlanCardAdapter extends BaseAdapter {
    private static String TAG = "PlanCardAdapter";

    private final List<Plan> filteredPlans;
    private final PlanSelector activity;

    public PlanCardAdapter(List<Plan> filteredPlans, PlanSelector planSelector){
        if (filteredPlans.size() == 0){
            Log.d(TAG, "filteredPlan size is 0!!!");
        }

        this.filteredPlans = filteredPlans;
        this.activity = planSelector;
    }

    @Override
    public int getCount() {
        return filteredPlans.size();
    }

    @Override
    public Object getItem(int i) {
        return filteredPlans.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        if (v == null){
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.plan_overview_small, viewGroup, false);
        }

        if (filteredPlans == null){
            Log.d(TAG, "filteredPlans is null");
        }
        populate(v, filteredPlans.get(i));
        return v;
    }

    private void populate(View v, final Plan plan) {
        if (plan == null){
            Log.d(TAG, "plan item is NULL!");
            return;
        }
        TextView planName = (TextView) v.findViewById(R.id.planName);
        planName.setText(plan.name);
        ImageView carrierLogo = (ImageView) v.findViewById(R.id.carrierLogo);
        Glide
            .with(activity)
            .load(plan.links.carrierLogo)
            .into(carrierLogo);

        TextView planType = (TextView) v.findViewById(R.id.planType);
        if (plan.planType != null){
            planType.setText(plan.planType);
        } else {
            planType.setText("");
        }
        TextView planLocation = (TextView) v.findViewById(R.id.planLocation);
        if (plan.nationwide != null
            && plan.nationwide) {
            planLocation.setText(R.string.nationwide);
        } else {
            planLocation.setText("nationwide false");
        }

        TextView metalLevel = (TextView) v.findViewById(R.id.metalType);
        if (plan.metalLevel != null){
            metalLevel.setText(PlanUtilities.getPlanMetalLevel(plan));
            ImageView planMetalRing = (ImageView) v.findViewById(R.id.planMetalRing);
            planMetalRing.setImageResource(PlanUtilities.getPlanMetalResource(plan));
        } else {
            metalLevel.setText("");
        }

        TextView monthlyPremium = (TextView) v.findViewById(R.id.monthlyPremium);
        monthlyPremium.setText(PlanUtilities.getFormattedMonthlyPremium(plan));
        TextView annualPremium = (TextView) v.findViewById(R.id.annualPremium);
        annualPremium.setText(PlanUtilities.getFormattedYearPremium(plan));
        TextView deductible = (TextView) v.findViewById(R.id.deductible);
        deductible.setText(PlanUtilities.getFormattedDeductible(plan));

        Button details = (Button) v.findViewById(R.id.details);
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intents.launchPlanDetails(PlanCardAdapter.this.activity, plan);
            }
        });
    }
}
