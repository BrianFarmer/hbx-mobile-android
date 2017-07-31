package org.dchbx.coveragehq;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * Created by plast on 5/4/2017.
 */

public class FamilyActivity extends BaseActivity {
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(FamilyActivity.class);

    private PlanShoppingParameters planShoppingParameters;
    private FamilyAdapter familyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.family);
        configToolbar();
        getMessages().getPlanShopping();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetPlanShoppingResult  getPlanShoppingResult) throws Exception {
        planShoppingParameters = getPlanShoppingResult.getPlanShoppingParameters();
        populate();
    }

    private void populate() {
        if (planShoppingParameters.ages == null){
            planShoppingParameters.ages = new ArrayList<>();
        }

        if (planShoppingParameters.ages.size() == 0){
            planShoppingParameters.ages.add(-1);
        }

        familyAdapter = new FamilyAdapter(this, planShoppingParameters);
        ListView memberListView = (ListView) findViewById(R.id.memberList);
        memberListView.setAdapter(familyAdapter);

        Button addFamilyMember = (Button) findViewById(R.id.addFamilyMember);
        addFamilyMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FamilyActivity.this.addFamilyMember();
            }
        });

        Button continueButton = (Button) findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intents.launchPremiumAndDeductible(FamilyActivity.this);
            }
        });
    }

    private void addFamilyMember() {
        planShoppingParameters.ages.add(-1);
        saveData();
        familyAdapter.notifyDataSetChanged();
    }

    public void saveData() {
        getMessages().updatePlanShopping(planShoppingParameters);
    }
}
