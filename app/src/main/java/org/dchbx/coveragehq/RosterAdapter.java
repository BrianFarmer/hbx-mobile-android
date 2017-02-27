package org.dchbx.coveragehq;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.joda.time.LocalDate;

import java.util.ArrayList;

import org.dchbx.coveragehq.models.roster.Enrollment;
import org.dchbx.coveragehq.models.roster.Health;
import org.dchbx.coveragehq.models.roster.RosterEntry;

/**
 * Created by plast on 11/2/2016.
 */

public class RosterAdapter extends BaseAdapter {
    private static String TAG = "RosterAdapter";

    private final BrokerFragment fragment;
    private final Context context;
    private final ArrayList<RosterEntry> employees;
    private final String brokerClientId;
    private final LocalDate coverageDate;

    public RosterAdapter(BrokerFragment fragment, Context context, ArrayList<RosterEntry> employees,
                         String brokerClientId, LocalDate coverageDate){
        this.fragment = fragment;
        this.context = context;
        this.employees = employees;
        this.brokerClientId = brokerClientId;
        this.coverageDate = coverageDate;
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
            view = LayoutInflater.from(context).inflate(R.layout.roster_item, viewGroup, false);
        }

        try {
            final RosterEntry employee = employees.get(i);
            Health health;
            TextView employeeName = (TextView) view.findViewById(R.id.textViewEmployeeName);
            employeeName.setText(BrokerUtilities.getFullName(employee));
            try {
                Enrollment enrollmentForCoverageYear = BrokerUtilities.getEnrollmentForCoverageYear(employee, coverageDate);
                health = enrollmentForCoverageYear.health;
                TextView statusThisYear = (TextView) view.findViewById(R.id.textViewStatusThisYear);
                statusThisYear.setTextColor(ContextCompat.getColor(context, Utilities.colorFromEmployeeStatus(health.status)));
                statusThisYear.setText(health.status);
            } catch (Exception e){
                Log.e(TAG, "enrollment coverage year must be missing.", e);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intents.launchEmployeeDetails((BrokerActivity)fragment.getActivity(), employee.id, brokerClientId, coverageDate);
                }
            });
            return view;
        } catch (Exception e){
            Log.e(TAG, "Exception getting employee item", e);
            return null;

        }
    }
}
