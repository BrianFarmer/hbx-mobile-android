package org.dchbx.coveragehq.financialeligibility;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.models.fe.Family;
import org.dchbx.coveragehq.models.fe.Schema;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.OnActivityResultListener;
import org.dchbx.coveragehq.statemachine.StateManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by plast on 5/5/2017.
 */

public class RelationshipsAdapter extends BaseAdapter {
    private final Schema schema;
    private ArrayList<FamilyPair> familyPairs;
    private final Family family;
    private final JsonArray person;
    private final RelationshipsActivity activity;
    private View currentlyFocusedRow;

    public class FamilyPair{
        private final JsonObject from;
        private final JsonObject cross;

        public FamilyPair(JsonObject from, JsonObject cross){

            this.from = from;
            this.cross = cross;
        }

        public JsonObject getFrom() {
            return from;
        }

        public JsonObject getCross() {
            return cross;
        }
    }

    public RelationshipsAdapter(RelationshipsActivity activity, Family family, Schema schema){
        this.schema = schema;
        int familySize = family.Person.size();
        familyPairs = new ArrayList<>();

        for (int x = 0; x < familySize - 1; x ++){
            for (int y = x + 1; y < familySize; y ++){
                familyPairs.add(new FamilyPair(family.Person.get(x).getAsJsonObject(), family.Person.get(y).getAsJsonObject()));
            }
        }


        this.activity = activity;
        this.family = family;
        this.person = family.Person;
    }

    private JsonObject getRelationship(FamilyPair pair){
        if (!family.Relationship.containsKey(pair.from.get("eapersonid").getAsString())){
            family.Relationship.put(pair.from.get("eapersonid").getAsString(), new HashMap<String, JsonObject>());
        }
        HashMap<String, JsonObject> crosses = family.Relationship.get(pair.from.get("eapersonid").getAsString());
        if (!crosses.containsKey(pair.cross.get("eapersonid").getAsString())){
            crosses.put(pair.cross.get("eapersonid").getAsString(), FinancialEligibilityService.build(schema.Relationship));
        }
        return crosses.get(pair.cross.get("eapersonid").getAsString());
    }

    @Override
    public boolean hasStableIds(){
        return false;
    }

    @Override
    public int getCount() {
        return familyPairs.size();
    }

    @Override
    public Object getItem(int i) {
        return familyPairs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, final ViewGroup viewGroup) {
        View v = view;
        if (v == null){
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.relationship_row, viewGroup, false);
        }

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApplicationQuestionsActivity.setOnActivityResultListener(new OnActivityResultListener() {
                    @Override
                    public void onActivityResult(Intent intent) {
                        if (intent != null) {
                            String jsonString = intent.getStringExtra("Result");
                            Gson gson = new Gson();
                            JsonObject person = gson.fromJson(jsonString, JsonObject.class);
                            FamilyPair pair = (FamilyPair) getItem(i);
                            HashMap<String, JsonObject> crosses = family.Relationship.get(pair.from.get("eapersonid").getAsString());
                            crosses.put(pair.cross.get("eapersonid").getAsString(), person);
                            activity.saveData();
                        }
                    }
                });
                Gson gson = new Gson();
                FamilyPair pair = (FamilyPair) getItem(i);
                JsonObject relationship = getRelationship(pair);
                activity.getMessages().appEvent(StateManager.AppEvents.EditRelationship,
                        EventParameters.build().add("Relationship", gson.toJson(relationship))
                                               .add("From", gson.toJson(pair.from))
                                               .add("Cross", gson.toJson(pair.cross)));
            }
        });

        FamilyPair pair = (FamilyPair) getItem(i);
        String formattedString = activity.getString(R.string.family_relationship, pair.from.get("personfirstname").getAsString(),
                pair.from.get("personlastname").getAsString(),
                pair.cross.get("personfirstname").getAsString(),
                pair.cross.get("personlastname").getAsString());
        TextView relationshipLabel = (TextView) v.findViewById(R.id.relationshipLabel);
        relationshipLabel.setText(formattedString);
        return v;
    }

    private void removeFamilyMember(int i ){
        family.Person.remove(i);
        notifyDataSetChanged();
        activity.saveData();
    }
}
