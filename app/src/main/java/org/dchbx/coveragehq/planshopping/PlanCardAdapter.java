package org.dchbx.coveragehq.planshopping;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.dchbx.coveragehq.AdapterItemWrapperBase;
import org.dchbx.coveragehq.PlanCardPopulation;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.models.planshopping.Plan;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.StateManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by plast on 5/18/2017.
 */

public class PlanCardAdapter extends BaseAdapter {
    private static String TAG = "PlanCardAdapter";

    private final ArrayList<Plan> filteredPlans;
    private final ArrayList<AdapterItemWrapperBase> originalWrapperBaseList;
    private final EndCardWrapper endCard;
    private ArrayList<AdapterItemWrapperBase> currentWrapperBaseList;
    private ArrayList<AdapterItemWrapperBase> favorites;
    private ArrayList<AdapterItemWrapperBase> removed;
    private final PlanSelector activity;
    private final LayoutInflater inflater;
    private ArrayList<Integer> removedIndexes;
    private HashMap<String, Plan> removedPlans;

    public PlanCardAdapter(ArrayList<Plan> filteredPlans, PlanSelector planSelector){
        if (filteredPlans.size() == 0){
            Log.d(TAG, "filteredPlan size is 0!!!");
        }

        this.favorites = new ArrayList<>();
        this.removed = new ArrayList<>();
        this.removedPlans = new HashMap<>();
        this.filteredPlans = filteredPlans;
        this.activity = planSelector;

        currentWrapperBaseList = new ArrayList<AdapterItemWrapperBase>();
        originalWrapperBaseList = currentWrapperBaseList;
        for (Plan filteredPlan : this.filteredPlans) {
            CardWrapper cardWrapper = new CardWrapper(filteredPlan);
            currentWrapperBaseList.add(cardWrapper);
        }

        endCard = new EndCardWrapper(this, activity);
        currentWrapperBaseList.add(endCard);
        this.inflater = (LayoutInflater) planSelector.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean hasStableIds(){
        return false;
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
        AdapterItemWrapperBase wrapper = currentWrapperBaseList.get((int) positionInAdapter);
        removed.add(wrapper);
        Plan plan = ((CardWrapper) wrapper).getPlan();
        removedPlans.put(plan.id, plan);
        endCard.populate(null);
    }

    public void cardSwipedRight(long positionInAdapter) {
        favorites.add(currentWrapperBaseList.get((int) positionInAdapter));
        endCard.populate(null);
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

    public class CardWrapper extends AdapterItemWrapperBase {

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
                activity.getMessages().appEvent(StateManager.AppEvents.ShowPlanDetails, EventParameters.build().add("Plan", plan));
                }
            });

            Button enroll = (Button) view.findViewById(R.id.enroll);
            enroll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.getMessages().appEvent(StateManager.AppEvents.BuyPlan, EventParameters.build().add(PlanShoppingService.Plan, plan));
                }
            });
        }

        @Override
        public String getViewType() {
            return "CardWrapper";
        }

        public Plan getPlan(){
            return plan;
        }
    }

    private class EndCardWrapper extends AdapterItemWrapperBase {

        private final PlanCardAdapter planCardAdapter;
        private final PlanSelector planSelector;
        private View myProbablyView;

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

            // There should only be one of these cards so this should work.
            if (view != null){
                myProbablyView = view;
            } else {
                view = myProbablyView;
            }

            if (view == null){
                return;
            }

            int favoriteePlanCount = planCardAdapter.activity.getFavoriteedPlans().size();
            switch (favoriteePlanCount) {
                case 0:
                    populateZeroFavorited(view);
                    break;
                case 1:
                    populateOneFavorited(view);
                    break;
                default:
                    populateMoreThanOneFavorited(view);
            }
        }

        private void populateZeroFavorited(View view) {
            show(R.id.zeroFavorited, view);
            hide(R.id.youFavorited, view);
            hide(R.id.seeThemAgain, view);
            hide(R.id.seeItNow, view);
            hide(R.id.keepSwiping, view);
            hide(R.id.tapReset, view);
            hide(R.id.tapResetFromOne, view);
            TextView tapResetFromZero = (TextView)show(R.id.tapResetFromZero, view);
            tapResetFromZero.setText(Html.fromHtml(planSelector.getResources().getString(R.string.tap_reset_from_zero)));
        }

        private void populateMoreThanOneFavorited(View view) {
            hide(R.id.zeroFavorited, view);
            TextView youFavorited = (TextView) show(R.id.youFavorited, view);
            show(R.id.seeThemAgain, view);
            hide(R.id.seeItNow, view);
            View keepSwiping = show(R.id.keepSwiping, view);
            TextView tapReset = (TextView)show(R.id.tapReset, view);
            hide(R.id.tapResetFromZero, view);
            hide(R.id.tapResetFromOne, view);

            String format = planSelector.getResources().getString(R.string.you_favorited_d_plans);
            String favoritedMessage = String.format(format, planCardAdapter.activity.getFavoriteedPlans().size());
            populateNonZeroFavorited(favoritedMessage, youFavorited, keepSwiping, tapReset);
        }

        private void populateOneFavorited(View view) {
            hide(R.id.zeroFavorited, view);
            TextView youFavorited = (TextView) show(R.id.youFavorited, view);
            hide(R.id.seeThemAgain, view);
            View seeItNow = show(R.id.seeItNow, view);
            hide(R.id.keepSwiping, view);
            hide(R.id.tapReset, view);
            TextView tapReset = (TextView) show(R.id.tapResetFromOne, view);
            hide(R.id.tapResetFromZero, view);
            String favorited = planSelector.getResources().getString(R.string.you_favorited_one_plan);
            populateNonZeroFavorited(favorited, youFavorited, seeItNow, tapReset);
        }

        private void populateNonZeroFavorited(String favoritedMessage, TextView youFavorited, View returnToPlans, TextView tapReset) {
            youFavorited.setText(Html.fromHtml(favoritedMessage));

            tapReset.setText(Html.fromHtml(planSelector.getResources().getString(R.string.tap_reset)));

            returnToPlans.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<Plan> selectedPlans = new ArrayList<Plan>();
                    for (AdapterItemWrapperBase favorite : planCardAdapter.favorites) {
                        selectedPlans.add(((CardWrapper) favorite).plan);
                    }
                    planCardAdapter.activity.showFavorites(selectedPlans);
                }
            });
        }

        private View show(int elementId, View view) {
            return setVisibility(elementId, view, View.VISIBLE);
        }

        private View hide(int elementId, View view) {
            return setVisibility(elementId, view, View.GONE);
        }

        private View setVisibility(int elementId, View view, int visibility) {
            View element = view.findViewById(elementId);
            element.setVisibility(visibility);
            return element;
        }

        @Override
        public String getViewType() {
            return "EndCardWrapper";
        }
    }

    public void showFavorites() {
        Iterator<AdapterItemWrapperBase> iterator = currentWrapperBaseList.iterator();
        while (iterator.hasNext()) {
            AdapterItemWrapperBase current = iterator.next();
            if (current instanceof CardWrapper){
                Plan plan = ((CardWrapper)current).plan;
                if (removedPlans.containsKey(plan.id)){
                    iterator.remove();
                }
            }
        }
    }
}
