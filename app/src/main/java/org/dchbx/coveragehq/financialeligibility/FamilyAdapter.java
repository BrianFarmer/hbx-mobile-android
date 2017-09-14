package org.dchbx.coveragehq.financialeligibility;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
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

/**
 * Created by plast on 5/5/2017.
 */

public class FamilyAdapter extends BaseAdapter {
    private final Schema schema;
    private final Family family;
    private final JsonArray person;
    private final FamilyActivity activity;

    public FamilyAdapter(org.dchbx.coveragehq.financialeligibility.FamilyActivity activity,
                         Family family, Schema schema){

        this.activity = activity;
        this.family = family;
        this.person = family.Person;
        this.schema = schema;
    }

    @Override
    public boolean hasStableIds(){
        return false;
    }

    @Override
    public int getCount() {
        return person.size();
    }

    @Override
    public Object getItem(int i) {
        return person.get(i);
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
            v = inflater.inflate(R.layout.family_row, viewGroup, false);
        }


        final View indicator = v.findViewById(R.id.indicator);
        ImageButton removeMember = (ImageButton) v.findViewById(R.id.removeMember);
        TextView memberLabel = (TextView) v.findViewById(R.id.memberLabel);
        if (i == 0){
            removeMember.setVisibility(View.INVISIBLE);
            memberLabel.setText(R.string.you_primary);
        } else {
            removeMember.setVisibility(View.VISIBLE);
            removeMember.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeFamilyMember(i);
                }
            });
            JsonObject item = (JsonObject) getItem(i);
            if (item.has("personfirstname")
                    || item.has("personlastname")){
                String name = "";
                if (item.has("personfirstname")){
                    name = item.get("personfirstname").getAsString();
                }
                if (item.has("personlastname")){
                    name += " " + item.get("personlastname").getAsString();
                }
                name = name.trim();
                memberLabel.setText(name);
            } else {
                memberLabel.setText(String.format(activity.getString(R.string.family_member), i));
            }
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
                            family.Person.set(i, person);
                            activity.saveData();
                            indicator.setVisibility(FinancialEligibilityService.checkObject(person, schema.Person)?View.GONE:View.VISIBLE);
                        }
                    }
                });
                activity.getMessages().appEvent(StateManager.AppEvents.EditFamilyMember, EventParameters.build().add("FamilyMember", new Gson().toJson(family.Person.get(i))));
            }
        });
        indicator.setVisibility(FinancialEligibilityService.checkObject(family.Person.get(i).getAsJsonObject(), schema.Person)?View.GONE:View.VISIBLE);
        return v;
    }

    private void removeFamilyMember(int i ){
        family.Person.remove(i);
        notifyDataSetChanged();
        activity.saveData();
    }
}
