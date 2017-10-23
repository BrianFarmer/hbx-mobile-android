package org.dchbx.coveragehq.financialeligibility;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.dchbx.coveragehq.BaseActivity;
import org.dchbx.coveragehq.Events;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.ServiceManager;
import org.dchbx.coveragehq.models.account.Account;
import org.dchbx.coveragehq.models.fe.Family;
import org.dchbx.coveragehq.models.fe.Schema;
import org.dchbx.coveragehq.ridp.RidpService;
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
    private Account account;

    public FamilyActivity(){
        Log.d(TAG, "familyactivity ctor");
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (familyAdapter != null){
            familyAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        account = RidpService.getAccountFromIntent(intent);

        setContentView(R.layout.family);
        configToolbar();
        getMessages().getUqhpFamily();
        getMessages().getUqhpSchema();
        if (account == null){
            getMessages().getCreateAccountInfo();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetCreateAccountInfoResult getCreateAccountInfoResult){
        account = getCreateAccountInfoResult.getAccount();
        populate();
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
            || family == null
            || account == null){
            return;
        }

        if (family.Person.size() == 0){
            JsonObject newPerson = FinancialEligibilityService.getNewPerson(account, schema);
            family.Person.add(newPerson);
        }

        familyAdapter = new org.dchbx.coveragehq.financialeligibility.FamilyAdapter(this, family, schema);
        ListView memberListView = (ListView) findViewById(R.id.memberList);
        memberListView.setAdapter(familyAdapter);

        installSimpleAlertClickListener(R.id.shouldInclude, R.string.who_can_apply_together_title, R.string.who_can_apply_together_text);
        installSimpleAlertClickListener(R.id.shouldIncludeImage, R.string.who_can_apply_together_title, R.string.who_can_apply_together_text);

        //ImageView shouldIncludeImage = (ImageView) findViewById(R.id.shouldIncludeImage);
        //shouldIncludeImage.setOnClickListener(clickForSimpleAlert(R.string.who_can_apply_together_title, R.string.who_can_apply_together_text));

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
                if (!allcomplete()){
                    simpleAlert(R.string.app_name, R.string.please_check_person);
                    return;
                }

                if (family.Person.size() == 1){
                    getMessages().appEvent(StateManager.AppEvents.ContinueSingleMemberFamily, EventParameters.build().add("FamilyMemberCount", family.Person.size()));
                } else {
                    getMessages().appEvent(StateManager.AppEvents.ContinueMultipleMemberFamily, EventParameters.build().add("FamilyMemberCount", family.Person.size()));
                }
            }
        });
    }

    private boolean allcomplete() {
        for (JsonElement jsonElement : family.Person) {
            JsonObject person = jsonElement.getAsJsonObject();
            if (!FinancialEligibilityService.checkObject(person, schema.Person)){
                return false;
            }
        }

        return true;
    }

    public void saveData() {
        getMessages().saveUqhpFamily(family);
    }
}
