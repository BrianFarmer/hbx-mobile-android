package org.dchbx.coveragehq;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.dchbx.coveragehq.models.planshopping.PlanShoppingParameters;

import java.util.List;

/**
 * Created by plast on 5/5/2017.
 */

public class FamilyAdapter extends BaseAdapter {
    private final FamilyActivity activity;
    private final PlanShoppingParameters planShoppingParameters;
    private final List<Integer> ages;
    private View currentlyFocusedRow;

    public FamilyAdapter(FamilyActivity activity, PlanShoppingParameters planShoppingParameters) {

        this.activity = activity;
        this.planShoppingParameters = planShoppingParameters;
        this.ages = planShoppingParameters.ages;
    }

    @Override
    public boolean hasStableIds(){
        return false;
    }

    @Override
    public int getCount() {
        return ages.size();
    }

    @Override
    public Object getItem(int i) {
        return ages.get(i);
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

        if (ages.get(i) != -1){
            EditText age = (EditText) v.findViewById(R.id.age);
            age.setText(ages.get(i).toString());
        }
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
            memberLabel.setText(String.format(activity.getString(R.string.family_member), i));
        }

        final EditText age = (EditText) v.findViewById(R.id.age);
        if (planShoppingParameters.ages.get(i) == -1){
            age.setText(null);
        } else {
            age.setText(planShoppingParameters.ages.get(i).toString());
        }
        age.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus
                    && i < planShoppingParameters.ages.size()){
                    String ageString = age.getText().toString();
                    if (ageString != null
                        && ageString.length() > 0){
                        try {
                            int ageValue = Integer.parseInt(ageString);
                            planShoppingParameters.ages.set(i, ageValue);
                            activity.saveData();
                        } catch (NumberFormatException e){

                        }
                    }
                }
            }
        });

        return v;
    }

    private void removeFamilyMember(int i ){
        ages.remove(i);
        notifyDataSetChanged();
    }
}
