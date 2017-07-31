package org.dchbx.coveragehq;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.daprlabs.aaron.swipedeck.SwipeDeck;
import com.daprlabs.aaron.swipedeck.SwipeDeck.SwipeDeckCallback;
import com.daprlabs.aaron.swipedeck.layouts.SwipeFrameLayout;

import org.dchbx.coveragehq.models.planshopping.Plan;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by plast on 5/18/2017.
 */

public class PlanSelector extends BaseActivity {
    private static String TAG = "PlanSelector";
    public static StateManager.UiActivity uiActivity;

    private List<Plan> planList;
    private double currentPremium;
    private double currentDeductible;
    private ArrayList<Plan> currentFilteredPlans;
    private ArrayList<Plan> filteredPlans;
    private ArrayList<Plan> favoriteedPlans;
    private SwipeFrameLayout deckLayout;
    private SwipeDeck deckView;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    TextView plansAndFavorites;
    TextView discard;
    TextView reset;
    TextView keep;

    PlanCardAdapter planCardAdapter;
    private FrameLayout placeHolder;
    private TextView plansAndFavoritesPlans;
    private TextView plansAndFavoritesFavorites;
    private TextView plansAndFavoritesSlash;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.plan_selector);

        inflateSwipeDeck();

        deckLayout = (SwipeFrameLayout)findViewById(R.id.swipeLayout);
        deckView = (SwipeDeck) findViewById(R.id.swipeDeck);
        plansAndFavoritesPlans = (TextView) findViewById(R.id.plansAndFavoritesPlans);
        plansAndFavoritesSlash = (TextView) findViewById(R.id.plansAndFavoritesSlash);
        plansAndFavoritesFavorites = (TextView) findViewById(R.id.plansAndFavoritesFavorites);
        discard = (TextView) findViewById(R.id.discard);
        reset = (TextView) findViewById(R.id.reset);
        keep = (TextView) findViewById(R.id.keep);
        favoriteedPlans = new ArrayList<>();

        discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discardClicked();
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetClicked();
                populatePlansAndFavorites();
            }
        });

        keep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keepClicked();
            }
        });

        plansAndFavoritesFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favoritesCountClicked();
            }
        });

        getMessages().getPlans();
        configToolbar();
    }

    private void inflateSwipeDeck() {
        placeHolder = (FrameLayout) findViewById(R.id.swipeDeckFrameLayout);
        View child = (FrameLayout)getLayoutInflater().inflate(R.layout.plan_selector_swipedeck, null);
        placeHolder.addView(child);
    }

    private void favoritesCountClicked(){
        showFavorites(favoriteedPlans);
    }

    private void discardClicked() {
        if (planCardAdapter == null
            || planCardAdapter.getCount() == 0){
            return;
        }

        deckView.swipeTopCardLeft(1200);
    }

    private void resetClicked() {
        if (planCardAdapter == null
                || planCardAdapter.getCount() == 0){
            return;
        }
        placeHolder.removeAllViews();
        inflateSwipeDeck();
        deckView = (SwipeDeck) findViewById(R.id.swipeDeck);
        populateSwipeDeck(currentFilteredPlans);
        favoriteedPlans = new ArrayList<>();
    }

    private void keepClicked() {
        if (planCardAdapter == null
                || planCardAdapter.getCount() == 0){
            return;
        }

        deckView.swipeTopCardRight(1200);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetPlansResult getPlansResult) throws Exception {
        planList = getPlansResult.getPlanList();
        currentPremium = getPlansResult.getPremiumFilter();
        currentDeductible = getPlansResult.getDeductibleFilter();
        currentFilteredPlans = PlanUtilities.getPlansInRange(planList, currentPremium, currentDeductible);
        filteredPlans = currentFilteredPlans;
        populate();
    }

    private void populate() {
        deckView.setCallback(new SwipeDeckCallback() {
            @Override
            public void cardSwipedLeft(long positionInAdapter) {

                planCardAdapter.cardSwipedLeft(positionInAdapter);
                populatePlansAndFavorites();
            }

            @Override
            public void cardSwipedRight(long positionInAdapter) {
                favoriteedPlans.add(((PlanCardAdapter.CardWrapper)(planCardAdapter.getItem((int)positionInAdapter))).getPlan());
                planCardAdapter.cardSwipedRight(positionInAdapter);
                populatePlansAndFavorites();
            }

            @Override
            public boolean isDragEnabled(long itemId){
                return !planCardAdapter.isLastCard(itemId);
            }
        });

        populateSwipeDeck(currentFilteredPlans);
        populatePlansAndFavorites();
    }

    private void populateSwipeDeck(ArrayList<Plan> plans) {
        planCardAdapter = new PlanCardAdapter(plans, this);
        deckView.setAdapter(planCardAdapter);
        deckView.setLeftImage(R.id.discardOverlay);
        deckView.setRightImage(R.id.keepdOverlay);
        deckView.setAdapterIndex(0);
        deckView.setCallback(new SwipeDeckCallback() {
            @Override
            public void cardSwipedLeft(long positionInAdapter) {

                planCardAdapter.cardSwipedLeft(positionInAdapter);
                populatePlansAndFavorites();
            }

            @Override
            public void cardSwipedRight(long positionInAdapter) {
                favoriteedPlans.add(((PlanCardAdapter.CardWrapper)(planCardAdapter.getItem((int)positionInAdapter))).getPlan());
                planCardAdapter.cardSwipedRight(positionInAdapter);
                populatePlansAndFavorites();
            }

            @Override
            public boolean isDragEnabled(long itemId){
                return !planCardAdapter.isLastCard(itemId);
            }
        });
    }

    private void populatePlansAndFavorites() {
        plansAndFavoritesPlans.setText(String.format(getString(R.string.plans_and_favorites_plans), planCardAdapter.getPlanCount()));
        plansAndFavoritesFavorites.setText(String.format(getString(R.string.plans_and_favorites_favorites), planCardAdapter.getFavoriteCount()));
    }

    public void showFavorites(ArrayList<Plan> filteredPlans) {
        placeHolder.removeAllViews();
        inflateSwipeDeck();
        deckView = (SwipeDeck) findViewById(R.id.swipeDeck);
        populateSwipeDeck(favoriteedPlans);
        favoriteedPlans = new ArrayList<>();
    }

    public ArrayList<Plan> getFavoriteedPlans(){
        return favoriteedPlans;
    }
}
