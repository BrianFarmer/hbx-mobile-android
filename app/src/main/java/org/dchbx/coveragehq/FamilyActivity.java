package org.dchbx.coveragehq;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.dchbx.coveragehq.financialeligibility.FinancialEligibilityService;
import org.dchbx.coveragehq.models.fe.Family;
import org.dchbx.coveragehq.models.fe.Person;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.OnActivityResultListener;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by plast on 5/4/2017.
 */

public class FamilyActivity extends BaseActivity {
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(FamilyActivity.class);

    private FamilyAdapter familyAdapter;
    private Family family;

    public FamilyActivity(){
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.family);
        configToolbar();
        getMessages().getPlanShopping();
        getMessages().getUqhpFamily();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetPlanShoppingResult  getPlanShoppingResult) throws Exception {
        populate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetUqhpFamilyResponse getUqhpFamilyResponse) throws Exception {
        this.family = getUqhpFamilyResponse.getFamily();
        populate();
    }

    protected void populate() {
        if (family == null) {
            return;
        }

        familyAdapter = new FamilyAdapter(this, family);
        ListView memberListView = (ListView) findViewById(R.id.memberList);
        memberListView.setAdapter(familyAdapter);

        TextView shouldInclude = (TextView) findViewById(R.id.shouldInclude);
        shouldInclude.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMessages().appEvent(StateManager.AppEvents.ShowGlossaryItem, EventParameters.build().add("term", "family"));
            }
        });
        ImageView shouldIncludeImage = (ImageView) findViewById(R.id.shouldIncludeImage);
        shouldIncludeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMessages().appEvent(StateManager.AppEvents.ShowGlossaryItem, EventParameters.build().add("term", "family"));
            }
        });
        Button addFamilyMember = (Button) findViewById(R.id.addFamilyMember);
        addFamilyMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FinancialEligibilityService financialEligibilityService = ServiceManager.getServiceManager().getFinancialEligibilityService();
                Person newPerson = financialEligibilityService.getNewPerson();
                BaseActivity.setOnActivityResultListener(new OnActivityResultListener() {
                    @Override
                    public void onActivityResult(Intent intent) {
                        if (intent != null) {
                            String jsonString = intent.getStringExtra("Result");
                            Gson gson = new Gson();
                            JsonObject person = gson.fromJson(jsonString, JsonObject.class);
                            FinancialEligibilityService.addPersonToFamily(family, person);
                            familyAdapter.notifyDataSetChanged();
                        }
                    }
                });
                getMessages().appEvent(StateManager.AppEvents.EditFamilyMember, EventParameters.build().add("FamilyMember", newPerson));
            }
        });

        Button continueButton = (Button) findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMessages().appEvent(StateManager.AppEvents.Continue);
            }
        });
    }

    public void saveData() {
        getMessages().saveUqhpFamily(family);
    }
}
