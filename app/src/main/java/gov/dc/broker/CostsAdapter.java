package gov.dc.broker;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import gov.dc.broker.models.roster.Employee;
import gov.dc.broker.models.roster.Health;

/**
 * Created by plast on 11/2/2016.
 */

public class CostsAdapter extends BaseAdapter {
    private static String TAG = "RosterAdapter";

    private final BrokerFragment fragment;
    private final Context context;
    private final ArrayList<Employee> employees;
    private final int brokerClientId;
    private final String coverageYear;

    public CostsAdapter(BrokerFragment fragment, Context context, ArrayList<Employee> employees, int brokerClientId, String coverageYear){
        this.fragment = fragment;
        this.context = context;
        this.employees = employees;
        this.brokerClientId = brokerClientId;
        this.coverageYear = coverageYear;
    }

    @Override
    public int getCount() {
        return employees.size();
    }

    @Override
    public Object getItem(int i) {
        return employees.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getViewTypeCount(){
        return  1;
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.costs_item, viewGroup, false);
        }

        try {
            final Employee employee = employees.get(i);
            Health health;
            if (coverageYear.compareToIgnoreCase("active") == 0){
                health = employee.enrollments.active.health;
            } else {
                health = employee.enrollments.renewal.health;
            }
            TextView employeeName = (TextView) view.findViewById(R.id.textViewEmployeeName);
            employeeName.setText(employee.getFullName());
            TextView textViewEmployerCost = (TextView) view.findViewById(R.id.textViewEmployerCost);
            TextView textViewEmployeeCost = (TextView) view.findViewById(R.id.textViewEmployeeCost);
            if (health.status.compareToIgnoreCase("enrolled") == 0) {
                textViewEmployerCost.setText(String.format("$%.2f", health.employerContribution));
                textViewEmployeeCost.setText(String.format("$%.2f", health.employeeCost));
            } else {
                textViewEmployerCost.setText(health.status);
                textViewEmployerCost.setTextColor(ContextCompat.getColor(context, Utilities.colorFromEmployeeStatus(health.status)));
                textViewEmployeeCost.setText(health.status);
                textViewEmployeeCost.setTextColor(ContextCompat.getColor(context, Utilities.colorFromEmployeeStatus(health.status)));
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intents.launchEmployeeDetails((BrokerActivity)fragment.getActivity(), employee.id, brokerClientId);
                }
            });
            return view;
        } catch (Exception e){
            Log.e(TAG, "Exception getting employee item", e);
            return null;
        }
    }
}
