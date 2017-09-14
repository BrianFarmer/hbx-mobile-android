package org.dchbx.coveragehq;

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

import org.dchbx.coveragehq.financialeligibility.ApplicationQuestionsActivity;
import org.dchbx.coveragehq.financialeligibility.FinancialEligibilityService;
import org.dchbx.coveragehq.models.fe.Family;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.OnActivityResultListener;
import org.dchbx.coveragehq.statemachine.StateManager;

/**
 * Created by plast on 5/5/2017.
 */

public class FamilyAdapter extends BaseAdapter {
    private final FamilyActivity activity;
    private final Family family;
    private final JsonArray person;
    private View currentlyFocusedRow;

    public FamilyAdapter(FamilyActivity activity, Family family){

        this.activity = activity;
        this.family = family;
        this.person = family.Person;
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


        ImageButton removeMember = (ImageButton) v.findViewById(R.id.removeMember);
        TextView memberLabel = (TextView) v.findViewById(R.id.memberLabel);
        JsonObject item = (JsonObject) getItem(i);
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
            memberLabel.setText(String.format(activity.getString(R.string.family_member), i));
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
                    }
                }
            });
            activity.getMessages().appEvent(StateManager.AppEvents.EditFamilyMember, EventParameters.build().add("FamilyMember", new Gson().toJson(family.Person.get(i))));
            }
        });
        return v;
    }

    private void removeFamilyMember(int i ){
        FinancialEligibilityService.removeFamilyMember(family, i);
        notifyDataSetChanged();
        activity.saveData();
    }
}
