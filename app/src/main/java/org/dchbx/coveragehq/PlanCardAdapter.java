package org.dchbx.coveragehq;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.dchbx.coveragehq.models.planshopping.Plan;

import java.util.ArrayList;

/**
 * Created by plast on 5/18/2017.
 */

public class PlanCardAdapter extends BaseAdapter {
    private static String TAG = "PlanCardAdapter";

    private final ArrayList<Plan> filteredPlans;
    private final ArrayList<AdapterItemWrapperBase> originalWrapperBaseList;
    private ArrayList<AdapterItemWrapperBase> currentWrapperBaseList;
    private ArrayList<AdapterItemWrapperBase> favorites;
    private ArrayList<AdapterItemWrapperBase> removed;
    private final PlanSelector activity;
    private final LayoutInflater inflater;

    public PlanCardAdapter(ArrayList<Plan> filteredPlans, PlanSelector planSelector){
        if (filteredPlans.size() == 0){
            Log.d(TAG, "filteredPlan size is 0!!!");
        }

        this.favorites = new ArrayList<>();
        this.removed = new ArrayList<>();
        this.filteredPlans = filteredPlans;
        this.activity = planSelector;

        currentWrapperBaseList = new ArrayList<AdapterItemWrapperBase>();
        originalWrapperBaseList = currentWrapperBaseList;
        for (Plan filteredPlan : this.filteredPlans) {
            currentWrapperBaseList.add(new CardWrapper(filteredPlan));
        }
        currentWrapperBaseList.add(new EndCardWrapper(this, activity));
        this.inflater = (LayoutInflater) planSelector.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return currentWrapperBaseList.size();
    }

    @Override
    public Object getItem(int i) {
        return currentWrapperBaseList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AdapterItemWrapperBase item = currentWrapperBaseList.get(position);
        String  viewType = item.getViewType();
        View view;
        boolean createNewView = true;

        if (convertView != null){
            Object tagObject = convertView.getTag(R.id.view_type);
            if (tagObject != null){
                String tagString = (String) tagObject;
                if (tagString.equals(viewType)){
                    createNewView = false;
                }
            }
        }
        if (createNewView) {
            view = inflater.inflate(item.getLayout(), parent, false);
        } else {
            view = convertView;
        }
        item.populate(view);
        return view;
    }

    public void cardSwipedLeft(long positionInAdapter) {
        removed.add(currentWrapperBaseList.get((int) positionInAdapter));
    }

    public void cardSwipedRight(long positionInAdapter) {
        favorites.add(currentWrapperBaseList.get((int) positionInAdapter));
    }

    public int getFavoriteCount(){
        return favorites.size();
    }

    public int getPlanCount() {
        return filteredPlans.size() - removed.size();
    }

    public boolean isLastCard(long cardId) {
        return (currentWrapperBaseList.size() - 1) == cardId;
    }

    private class CardWrapper extends AdapterItemWrapperBase {

        private final Plan plan;

        public CardWrapper(Plan plan) {
            this.plan = plan;
        }

        @Override
        public int getLayout() {
            return R.layout.plan_overview_small;
        }

        @Override
        public void populate(View view) {
            PlanCardPopulation.populateFromHealth(view, plan, activity);

            Button details = (Button) view.findViewById(R.id.details);
            details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                Intents.launchPlanDetails(PlanCardAdapter.this.activity, plan);
                }
            });

            Button enroll = (Button) view.findViewById(R.id.enroll);
            enroll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        @Override
        public String getViewType() {
            return "CardWrapper";
        }
    }

    private class EndCardWrapper extends AdapterItemWrapperBase {

        private final PlanCardAdapter planCardAdapter;
        private final PlanSelector planSelector;

        public EndCardWrapper(PlanCardAdapter planCardAdapter, PlanSelector planSelector){
            this.planCardAdapter = planCardAdapter;
            this.planSelector = planSelector;
        }

        @Override
        public int getLayout() {
            return R.layout.plan_card_end;
        }

        @Override
        public void populate(View view) {
            TextView youFavorited = (TextView) view.findViewById(R.id.youFavorited);
            Button keepSwiping = (Button) view.findViewById(R.id.keepSwiping);
            TextView tapReset = (TextView)view.findViewById(R.id.tapReset);

            String favorited = planSelector.getResources().getString(R.string.you_favorited_d_plans);
            youFavorited.setText(Html.fromHtml(String.format(favorited, 36)));

            tapReset.setText(Html.fromHtml(planSelector.getResources().getString(R.string.tap_reset)));

            keepSwiping.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    planCardAdapter.showFavorites();
                }
            });
        }

        @Override
        public String getViewType() {
            return "EndCardWrapper";
        }
    }

    private void showFavorites() {
        currentWrapperBaseList = favorites;
    }
}
