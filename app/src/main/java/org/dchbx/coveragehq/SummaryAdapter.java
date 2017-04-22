package org.dchbx.coveragehq;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.dchbx.coveragehq.models.roster.Health;
import org.dchbx.coveragehq.models.services.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by plast on 4/19/2017.
 */

public class SummaryAdapter extends BaseAdapter {
    private static String TAG = SummaryAdapter.class.getSimpleName();
    private LayoutInflater inflater;
    private SummaryOfBenefitsActivity activity;
    private List<Service> servicesList;
    private ArrayList<SummaryItemWrapperBase> items;


    public SummaryAdapter(SummaryOfBenefitsActivity activity, List<Service> servicesList, Health plan){
        this.activity = activity;
        this.servicesList = servicesList;
        this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        items = new ArrayList<>();
        items.add(new ResourcesHeaderWrapper());
        items.add(new ResourcesItemWrapper(activity.getString(R.string.terms_and_conditions), plan.summaryOfBenefitsUrl, this));
        items.add(new SummaryHeaderWrapper());
        for (Service service : servicesList) {
            items.add(new SummaryItemWrapper(service, items.size(), this));
        }
    }

    public SummaryOfBenefitsActivity getActivity(){
        return activity;
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
    public int getViewTypeCount(){
        return  4;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    View generateView(int position, ViewGroup parent){
        SummaryItemWrapperBase item = (SummaryItemWrapperBase) getItem(position);
        return item.generateView(inflater, parent);
    }

    public void fillValues(int position, View convertView) {
        try {
            ((SummaryItemWrapperBase)getItem(position)).fillValues(convertView, activity);
        } catch (Exception e) {
            Log.e(TAG, "Exception in fillValues", e);
        }
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if(v == null){
            v = generateView(position, parent);
        }
        fillValues(position, v);
        return v;
    }}

abstract class SummaryItemWrapperBase {
    private static long nextId = 0;
    private final long id;
    public abstract int getType();

    protected SummaryItemWrapperBase(){
        id = nextId ;
        nextId = nextId + 1;
    }


    public long getId() {
        return id;
    }

    //public abstract View getView(View convertView, ViewGroup parent, MainActivity mainActivity, LayoutInflater inflater);

    //render a new item layout.
    public abstract View generateView(LayoutInflater inflater, ViewGroup parent);

    /*fill values to your item layout returned from `generateView`.
      The position param here is passed from the BaseAdapter's 'getView()*/
    public abstract void fillValues(View convertView, final SummaryOfBenefitsActivity mainActivity) throws Exception;
}

class ResourcesHeaderWrapper extends SummaryItemWrapperBase {
    private static String TAG = ResourcesHeaderWrapper.class.getSimpleName();

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public View generateView(LayoutInflater inflater, ViewGroup parent) {
        Log.d(TAG, "in ResourcesHeaderWrapper.generateView");
        return inflater.inflate(R.layout.summary_resources_header, parent, false);
    }

    @Override
    public void fillValues(View convertView, SummaryOfBenefitsActivity mainActivity) throws Exception {
        Log.d(TAG, "in ResourcesHeaderWrapper.fillValues");
    }
}

class ResourcesItemWrapper extends SummaryItemWrapperBase {
    private static String TAG = ResourcesItemWrapper.class.getSimpleName();
    private final String text;
    private final String url;
    private final SummaryAdapter summaryAdapter;
    private boolean bodyVisible = false;

    public ResourcesItemWrapper(String text, String url, SummaryAdapter summaryAdapter){
        this.text = text;
        this.url = url;
        this.summaryAdapter = summaryAdapter;
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public View generateView(LayoutInflater inflater, ViewGroup parent) {
        Log.d(TAG, "in ResourcesItemWrapper.generateView");
        return inflater.inflate(R.layout.summary_resource_item, parent, false);
    }

    @Override
    public void fillValues(View convertView, SummaryOfBenefitsActivity mainActivity) throws Exception {
        Log.d(TAG, "in ResourcesItemWrapper.fillValues");
        TextView label = (TextView) convertView.findViewById(R.id.label);
        label.setText(text);
        label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout body = (RelativeLayout) ((ViewGroup)view.getParent()).findViewById(R.id.body);
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
        TextView resourceName = (TextView) body.findViewById(R.id.resourceName);
        resourceName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(url));
                summaryAdapter.getActivity().startActivity(intent);
            }
        });
    }
}

class SummaryHeaderWrapper extends SummaryItemWrapperBase {
    private static String TAG = SummaryHeaderWrapper.class.getSimpleName();
    @Override
    public int getType() {
        return 2;
    }

    @Override
    public View generateView(LayoutInflater inflater, ViewGroup parent) {
        Log.d(TAG, "in SummaryHeaderWrapper.generateView");
        return inflater.inflate(R.layout.summary_benefits_header, parent, false);
    }

    @Override
    public void fillValues(View convertView, SummaryOfBenefitsActivity mainActivity) throws Exception {
        Log.d(TAG, "in SummaryHeaderWrapper.fillValues");
    }
}

class SummaryItemWrapper extends SummaryItemWrapperBase{
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
    public int getType() {
        return 3;
    }

    @Override
    public View generateView(LayoutInflater inflater, ViewGroup parent) {
        Log.d(TAG, "in SummaryItemWrapper.generateView");
        return inflater.inflate(R.layout.summary_benefit_item, parent, false);
    }

    @Override
    public void fillValues(View convertView, SummaryOfBenefitsActivity mainActivity) throws Exception {
        Log.d(TAG, "in SummaryItemWrapper.fillValues");
        TextView label = (TextView) convertView.findViewById(R.id.label);
        label.setText(service.service);
        label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout body = (RelativeLayout) view.findViewById(R.id.body);
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
    }
}