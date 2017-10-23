package org.dchbx.coveragehq.financialeligibility;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.JsonObject;

import org.dchbx.coveragehq.BaseActivity;
import org.dchbx.coveragehq.Events;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.models.fe.Family;
import org.dchbx.coveragehq.models.fe.Schema;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by plast on 5/4/2017.
 */

public class RelationshipsActivity extends BaseActivity {
    private static final String TAG = "RelationshipsActivity";
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(RelationshipsActivity.class);

    private RelationshipsAdapter relationshipsAdapter;
    private Family family;
    private Schema schema;

    public RelationshipsActivity(){
        Log.d(TAG, "familyactivity ctor");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.relationships);
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

        relationshipsAdapter = new RelationshipsAdapter(this, family, schema);
        ListView relationshipListView = (ListView) findViewById(R.id.relationshipsList);
        relationshipListView.setAdapter(relationshipsAdapter);

        Button continueButton = (Button) findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkRelationships()){
                    simpleAlert(R.string.app_name, R.string.please_check_relationships);
                    return;
                }
                getMessages().appEvent(StateManager.AppEvents.Continue);
            }
        });
    }

    private boolean checkRelationships() {
        if (family.Relationship.size() != family.Person.size() - 1){
            return false;
        }

        int i = 1;
        for (Map.Entry<String, HashMap<String, JsonObject>> entry : family.Relationship.entrySet()) {
            HashMap<String, JsonObject> relationships = entry.getValue();
            if (relationships.size() != family.Person.size() - i){
                return false;
            }

            for (Map.Entry<String, JsonObject> relationship : relationships.entrySet()) {
                if (relationship == null){
                    return false;
                }
            }
            i ++;
        }

        return true;
    }

    public void saveData() {
        getMessages().saveUqhpFamily(family);
    }
}
