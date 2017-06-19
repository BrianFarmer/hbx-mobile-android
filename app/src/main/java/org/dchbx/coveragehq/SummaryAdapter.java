package org.dchbx.coveragehq;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.dchbx.coveragehq.models.services.Service;

import java.util.ArrayList;
import java.util.List;

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
class SummaryAdapter extends BaseAdapter {
    private static String TAG = SummaryAdapter.class.getSimpleName();
    protected LayoutInflater inflater;
    protected Activity activity;
    protected List<Service> servicesList;
    protected ArrayList<AdapterItemWrapperBase> items;

    SummaryAdapter(Activity activity) {
        this.activity = activity;
        this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemViewType(int position){
        return (int) items.get(position).getId();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AdapterItemWrapperBase item = items.get(position);
        String  viewType = item.getViewType();
        View view;
        boolean createNewView = true;

        if (convertView != null){
            Object tagObject = convertView.getTag(R.id.view_type);
            if (tagObject != null){
                String tagString = (String) tagObject;
                if (tagString.equals(viewType)){
                    createNewView = false;
                }
            }
        }
        if (createNewView) {
            view = inflater.inflate(item.getLayout(), parent, false);
        } else {
            view = convertView;
        }
        item.populate(view);
        return view;

    }
}

class SummaryHeaderWrapper extends AdapterItemWrapperBase {
    private static String TAG = SummaryHeaderWrapper.class.getSimpleName();
    @Override
    public void populate(View convertView){
        Log.d(TAG, "in SummaryHeaderWrapper.fillValues");
    }

    @Override
    public int getLayout() {
        return R.layout.summary_benefits_header;
    }

    @Override
    public String getViewType() {
        return null;
    }
}

class SummaryItemWrapper extends AdapterItemWrapperBase {
    private static String TAG = SummaryItemWrapper.class.getSimpleName();
    private final Service service;
    private final int position;
    private final SummaryAdapter summaryAdapter;
    private boolean bodyVisible;

    public SummaryItemWrapper(Service service, int position, SummaryAdapter summaryAdapter) {
        this.service = service;
        this.position = position;
        this.summaryAdapter = summaryAdapter;
        bodyVisible = false;
    }

    @Override
    public int getLayout() {
        return R.layout.summary_benefit_item;
    }

    @Override
    public void populate(View convertView){
        Log.d(TAG, "in SummaryItemWrapper.fillValues");
        TextView label = (TextView) convertView.findViewById(R.id.label);
        label.setText(service.service);
        final View convertViewFinal = convertView;
        label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            RelativeLayout body = (RelativeLayout) convertViewFinal.findViewById(R.id.body);
            if (bodyVisible){
                Log.d(TAG, "hiding body");
                body.setVisibility(View.GONE);
                bodyVisible = false;
            } else {
                Log.d(TAG, "showing body");
                body.setVisibility(View.VISIBLE);
                bodyVisible = true;
            }
            summaryAdapter.notifyDataSetChanged();
            }
        });

        RelativeLayout body = (RelativeLayout) convertView.findViewById(R.id.body);
        if (bodyVisible){
            body.setVisibility(View.VISIBLE);
        } else {
            body.setVisibility(View.GONE);
        }
        TextView copay = (TextView) body.findViewById(R.id.copay);
        if (service.copay != null) {
            copay.setText(service.copay);
        } else {
            copay.setText("");
        }
        TextView coinsurance = (TextView) body.findViewById(R.id.coinsurance);
        if (service.coinsurance != null) {
            coinsurance.setText(service.coinsurance);
        } else {
            copay.setText("");
        }
    }

    @Override
    public String getViewType() {
        return null;
    }
}

class ResourcesHeaderWrapper extends AdapterItemWrapperBase {
    private static String TAG = ResourcesHeaderWrapper.class.getSimpleName();

    @Override
    public int getLayout() {
        return R.layout.summary_resources_header;
    }

    @Override
    public void populate(View view) {

    }

    @Override
    public String getViewType() {
        return null;
    }
}



class ResourcesItemWrapper extends AdapterItemWrapperBase{
    private static String TAG = ResourcesItemWrapper.class.getSimpleName();
    private final String url;
    private final String expandedText;
    private final int imageResourceId;
    private final Activity activity;

    public ResourcesItemWrapper(String url, int imageResourceId,
                                String expandedText, Activity activity){
        this.url = url;
        this.expandedText = expandedText;
        this.imageResourceId = imageResourceId;
        this.activity = activity;
    }

    @Override
    public void populate(View view) {
        Log.d(TAG, "in ResourcesItemWrapper.fillValues");

        ImageView resourceImage = (ImageView) view.findViewById(R.id.resourceImage);
        resourceImage.setImageResource(imageResourceId);
        TextView resourceName = (TextView) view.findViewById(R.id.resourceName);
        resourceName.setText(expandedText);
        resourceName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public int getLayout() {
        return R.layout.summary_resource_item;
    }

    @Override
    public String getViewType() {
        return null;
    }
}