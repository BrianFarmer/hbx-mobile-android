package org.dchbx.coveragehq.financialeligibility;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.dchbx.coveragehq.BaseActivity;
import org.dchbx.coveragehq.Events;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.ServiceManager;
import org.dchbx.coveragehq.models.fe.Family;
import org.dchbx.coveragehq.models.fe.Schema;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.OnActivityResultListener;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by plast on 5/4/2017.
 */

public class FamilyActivity extends BaseActivity {
    private static final String TAG = "FamilyActivity";
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(FamilyActivity.class);

    private FamilyAdapter familyAdapter;
    private Family family;
    private Schema schema;

    public FamilyActivity(){
        Log.d(TAG, "familyactivity ctor");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.family);
        configToolbar();
        getMessages().getUqhpFamily();
        getMessages().getUqhpSchema();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetFinancialEligibilityJsonResponse getFinancialEligibilityJsonResponse) throws Exception {
        schema = getFinancialEligibilityJsonResponse.getSchema();
        populate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetUqhpFamilyResponse getUqhpFamilyResponse) throws Exception {
        family = getUqhpFamilyResponse.getFamily();
        populate();
    }

    protected void populate() {
        if (schema == null
            || family == null){
            return;
        }

        familyAdapter = new org.dchbx.coveragehq.financialeligibility.FamilyAdapter(this, family, schema);
        ListView memberListView = (ListView) findViewById(R.id.memberList);
        memberListView.setAdapter(familyAdapter);

        TextView shouldInclude = (TextView) findViewById(R.id.shouldInclude);
        shouldInclude.setOnClickListener(clickForSimpleAlert(R.string.who_can_apply_together_title, R.string.who_can_apply_together_text));
        ImageView shouldIncludeImage = (ImageView) findViewById(R.id.shouldIncludeImage);
        shouldIncludeImage.setOnClickListener(clickForSimpleAlert(R.string.who_can_apply_together_title, R.string.who_can_apply_together_text));

        Button addFamilyMember = (Button) findViewById(R.id.addFamilyMember);
        addFamilyMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FinancialEligibilityService financialEligibilityService = ServiceManager.getServiceManager().getFinancialEligibilityService();
                JsonObject newPerson = financialEligibilityService.getNewPerson();
                BaseActivity.setOnActivityResultListener(new OnActivityResultListener() {
                    @Override
                    public void onActivityResult(Intent intent) {
                        if (intent != null) {
                            String jsonString = intent.getStringExtra("Result");
                            Gson gson = new Gson();
                            JsonObject person = gson.fromJson(jsonString, JsonObject.class);
                            family.Person.add(person);
                            familyAdapter.notifyDataSetChanged();
                            saveData();
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
