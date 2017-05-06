package org.dchbx.coveragehq;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by plast on 5/5/2017.
 */

public class FamilyAdapter extends BaseAdapter {
    private final FamilyActivity activity;
    private final List<Integer> ageList;

    public FamilyAdapter(FamilyActivity activity, List<Integer> ageList) {

        this.activity = activity;
        this.ageList = ageList;
    }

    @Override
    public int getCount() {
        return ageList.size();
    }

    @Override
    public Object getItem(int i) {
        return ageList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        if (v == null){
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.carrier_item, viewGroup, false);
        }

        if (i == 0){

        } else {

        }



        return v;
    }
}
