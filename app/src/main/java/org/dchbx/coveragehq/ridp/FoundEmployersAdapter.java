package org.dchbx.coveragehq.ridp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.models.ridp.Employer;

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
public class FoundEmployersAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<Employer> employers;

    public FoundEmployersAdapter(Context context, ArrayList<org.dchbx.coveragehq.models.ridp.Employer> employers){
        this.context = context;
        this.employers = employers;
    }

    @Override
    public int getCount() {
        return employers.size();
    }

    @Override
    public Object getItem(int position) {
        return employers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.acct_found_employer_item, parent, false);
        }
        Employer employer = employers.get(position);
        TextView companyName = (TextView) view.findViewById(R.id.companyName);
        Button connect = (Button) view.findViewById(R.id.connect);
        companyName.setText(employer.employer.legalName);
        return view;
    }
}
