package org.dchbx.coveragehq.financialeligibility;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.dchbx.coveragehq.ApplicationUtilities;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.models.fe.FinancialAssistanceApplication;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by plast on 5/5/2017.
 */

public class FamilyAdapter extends BaseAdapter {
    private final FamilyActivity activity;
    private final FinancialAssistanceApplication financialAssistanceApplication;
    private final ArrayList<HashMap<String, Object>> people;
    private View currentlyFocusedRow;

    public FamilyAdapter(FamilyActivity activity, FinancialAssistanceApplication financialAssistanceApplication) {

        this.activity = activity;
        this.financialAssistanceApplication = financialAssistanceApplication;
        people = financialAssistanceApplication.person;
    }

    @Override
    public boolean hasStableIds(){
        return false;
    }

    @Override
    public int getCount() {
        return people.size();
    }

    @Override
    public Object getItem(int i) {
        return people.get(i);
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
            v = inflater.inflate(R.layout.family_row_static_age, viewGroup, false);
        }

        HashMap<String, Object> person = people.get(i);

        TextView memberLabel = (TextView) v.findViewById(R.id.memberLabel);
        memberLabel.setText(String.format(ApplicationUtilities.getFullName(person)));

        EditText age = (EditText) v.findViewById(R.id.age);
        age.setText(ApplicationUtilities.getAge(person));

        ImageButton removeMember = (ImageButton) v.findViewById(R.id.removeMember);
        removeMember.setVisibility(View.VISIBLE);
        removeMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFamilyMember(i);
            }
        });

        return v;
    }

    private void removeFamilyMember(int i ){
        people.remove(i);
        notifyDataSetChanged();
    }
}
