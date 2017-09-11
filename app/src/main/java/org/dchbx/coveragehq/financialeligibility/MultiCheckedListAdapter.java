package org.dchbx.coveragehq.financialeligibility;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.models.fe.Option;

import java.util.ArrayList;
import java.util.HashSet;

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
public class MultiCheckedListAdapter extends BaseAdapter {

    private final ArrayList<Option> options;
    private final Activity activity;
    private HashSet<Integer> currentChecked;

    public MultiCheckedListAdapter(ArrayList<Option> options, Activity activity, ArrayList<Integer> initialCheckedIndexes){
        this.options = options;
        this.activity = activity;
        currentChecked = new HashSet<Integer>();
        currentChecked.addAll(initialCheckedIndexes);
    }

    @Override
    public int getCount() {
        return options.size();
    }

    @Override
    public Object getItem(int position) {
        return options.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater layoutInflater = activity.getLayoutInflater();
            view = layoutInflater.inflate(R.layout.app_list_row, parent, false);
        }
        TextView label = (TextView) view.findViewById(R.id.label);
        label.setText(options.get(position).name);
        ImageView imageView = (ImageView)view.findViewById(R.id.image);
        if (hasCheckedIndex(position)){
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rowClicked(position);
            }
        });
        return view;
    }

    private boolean hasCheckedIndex(int position) {
        if (currentChecked == null){
            return false;
        }
        return currentChecked.contains(position);
    }

    private void rowClicked(int position) {
        currentChecked.add(position);
        notifyDataSetChanged();
    }

    public ArrayList<Integer> getCurrentIndexes() {
        ArrayList<Integer> list = new ArrayList<>(currentChecked.size());
        for (Integer integer : currentChecked) {
            list.add(integer);
        }
        return list;
    }
}
