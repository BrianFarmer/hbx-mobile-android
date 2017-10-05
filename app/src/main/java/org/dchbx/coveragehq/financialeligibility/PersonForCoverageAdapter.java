package org.dchbx.coveragehq.financialeligibility;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.models.fe.PersonForCoverage;

import java.util.ArrayList;

/*
    This file is part of DC.

    DC Health Link SmallBiz is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DC Health Link SmallBiz is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DC Health Link SmallBiz.  If not, see <http://www.gnu.org/licenses/>.
    This statement should go near the beginning of every source file, close to the copyright notices. When using the Lesser GPL, insert the word “Lesser” before “General” in all three places. When using the GNU AGPL, insert the word “Affero” before “General” in all three places.
*/
public class PersonForCoverageAdapter extends BaseAdapter {
    private static String TAG = "Person4CoverageAdapter";

    private final Activity activity;
    private final ArrayList<PersonForCoverage> people;

    public PersonForCoverageAdapter(Activity activity, ArrayList<PersonForCoverage> people) {
        this.activity = activity;
        this.people = people;
    }

    @Override
    public int getCount() {
        return people.size();
    }

    @Override
    public Object getItem(int position) {
        return people.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        try {
            if (view == null) {
                LayoutInflater layoutInflater = activity.getLayoutInflater();
                view = layoutInflater.inflate(R.layout.person_for_coverage, parent, false);
            }
            TextView personName = (TextView) view.findViewById(R.id.personName);
            personName.setText(FinancialEligibilityService.getNameForPersonForCoverage(people.get(position)));
            return view;
        } catch (Throwable t){
            Log.d(TAG, "throwable: " + t);
        }
        return view;
    }
}
