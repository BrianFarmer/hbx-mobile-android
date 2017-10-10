package org.dchbx.coveragehq.planshopping;

import android.content.Context;
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

import static org.dchbx.coveragehq.Intents.PLAN_ID;

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
                activity.getMessages().appEvent(StateManager.AppEvents.ShowPlanDetails, EventParameters.build().add(PLAN_ID, plan.id));
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
                    populateZeroFavoriteed(view);
                    break;
                case 1:
                    populateOneFavoriteed(view);
                    break;
                default:
                    populateMoreThanOneFavoriteed(view);
            }
        }

        private void populateZeroFavoriteed(View view) {
            TextView zeroFavoriteed = (TextView) view.findViewById(R.id.zeroFavoriteed);
            zeroFavoriteed.setVisibility(View.VISIBLE);
            TextView youFavorited = (TextView) view.findViewById(R.id.youFavorited);
            youFavorited.setVisibility(View.GONE);
            TextView seeThemAgain = (TextView) view.findViewById(R.id.seeThemAgain);
            seeThemAgain.setVisibility(View.GONE);
            Button keepSwiping = (Button) view.findViewById(R.id.keepSwiping);
            keepSwiping.setVisibility(View.GONE);
            TextView tapReset = (TextView) view.findViewById(R.id.tapReset);
            TextView tapResetFromZero = (TextView) view.findViewById(R.id.tapResetFromZero);

            tapReset.setVisibility(View.GONE);
            tapResetFromZero.setVisibility(View.VISIBLE);

            String favorited = planSelector.getResources().getString(R.string.you_favorited_d_plans);
            youFavorited.setText(Html.fromHtml(String.format(favorited, planCardAdapter.activity.getFavoriteedPlans().size())));

            tapResetFromZero.setText(Html.fromHtml(planSelector.getResources().getString(R.string.tap_reset_from_zero)));
        }
        private void populateMoreThanOneFavoriteed(View view) {
            TextView zeroFavoriteed = (TextView) view.findViewById(R.id.zeroFavoriteed);
            zeroFavoriteed.setVisibility(View.GONE);
            TextView youFavoriteed = (TextView) view.findViewById(R.id.youFavorited);
            youFavoriteed.setVisibility(View.VISIBLE);
            TextView seeThemAgain = (TextView) view.findViewById(R.id.seeThemAgain);
            seeThemAgain.setVisibility(View.VISIBLE);
            Button keepSwiping = (Button) view.findViewById(R.id.keepSwiping);
            keepSwiping.setVisibility(View.VISIBLE);
            TextView tapReset = (TextView) view.findViewById(R.id.tapReset);
            tapReset.setVisibility(View.VISIBLE);
            TextView tapResetFromZero = (TextView) view.findViewById(R.id.tapResetFromZero);
            tapResetFromZero.setVisibility(View.GONE);

            String favorited = planSelector.getResources().getString(R.string.you_favorited_d_plans);
            youFavoriteed.setText(Html.fromHtml(String.format(favorited, planCardAdapter.activity.getFavoriteedPlans().size())));

            tapReset.setText(Html.fromHtml(planSelector.getResources().getString(R.string.tap_reset)));

            keepSwiping.setOnClickListener(new View.OnClickListener() {
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
        private void populateOneFavoriteed(View view) {
            TextView zeroFavoriteed = (TextView) view.findViewById(R.id.zeroFavoriteed);
            zeroFavoriteed.setVisibility(View.GONE);
            TextView youFavorited = (TextView) view.findViewById(R.id.youFavorited);
            youFavorited.setVisibility(View.VISIBLE);
            TextView seeThemAgain = (TextView) view.findViewById(R.id.seeThemAgain);
            seeThemAgain.setVisibility(View.VISIBLE);
            Button keepSwiping = (Button) view.findViewById(R.id.keepSwiping);
            keepSwiping.setVisibility(View.VISIBLE);
            TextView tapReset = (TextView) view.findViewById(R.id.tapReset);
            tapReset.setVisibility(View.VISIBLE);
            TextView tapResetFromZero = (TextView) view.findViewById(R.id.tapResetFromZero);
            tapResetFromZero.setVisibility(View.GONE);

            String favorited = planSelector.getResources().getString(R.string.you_favorited_d_plans);
            youFavorited.setText(Html.fromHtml(String.format(favorited, planCardAdapter.activity.getFavoriteedPlans().size())));

            tapReset.setText(Html.fromHtml(planSelector.getResources().getString(R.string.tap_reset)));

            keepSwiping.setOnClickListener(new View.OnClickListener() {
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
