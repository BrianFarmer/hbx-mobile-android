package gov.dc.broker;

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

import gov.dc.broker.models.roster.Enrollment;
import gov.dc.broker.models.roster.RosterEntry;
import gov.dc.broker.models.roster.Health;

/**
 * Created by plast on 11/2/2016.
 */

public class CostsAdapter extends BaseAdapter {
    private static String TAG = "RosterAdapter";

    private final BrokerFragment fragment;
    private final Context context;
    private final ArrayList<RosterEntry> employees;
    private final String brokerClientId;
    private final LocalDate coverageYear;

    public CostsAdapter(BrokerFragment fragment, Context context, ArrayList<RosterEntry> employees, String brokerClientId, LocalDate coverageYear){
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
            final RosterEntry rosterEntry = employees.get(i);
            TextView employeeName = (TextView) view.findViewById(R.id.textViewEmployeeName);
            employeeName.setText(BrokerUtilities.getFullName(rosterEntry));

            TextView textViewEmployerCost = (TextView) view.findViewById(R.id.textViewEmployerCost);
            TextView textViewEmployeeCost = (TextView) view.findViewById(R.id.textViewEmployeeCost);

            Enrollment enrollmentForCoverageYear = null;
            try {
                enrollmentForCoverageYear = BrokerUtilities.getEnrollmentForCoverageYear(rosterEntry, coverageYear);
            } catch (Exception e){
                Log.e(TAG, "exception get coverate year!!!");
            }
            if (enrollmentForCoverageYear != null) {
                Health health = enrollmentForCoverageYear.health;
                if (health.status.compareToIgnoreCase("enrolled") == 0) {
                    textViewEmployerCost.setText(String.format("$%.2f", health.employerContribution));
                    textViewEmployeeCost.setText(String.format("$%.2f", health.employeeCost));
                } else {
                    textViewEmployerCost.setText(health.status);
                    textViewEmployerCost.setTextColor(ContextCompat.getColor(context, Utilities.colorFromEmployeeStatus(health.status)));
                    textViewEmployeeCost.setText(health.status);
                    textViewEmployeeCost.setTextColor(ContextCompat.getColor(context, Utilities.colorFromEmployeeStatus(health.status)));
                }
            } else {
                textViewEmployeeCost.setText("");
                textViewEmployerCost.setText("");
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intents.launchEmployeeDetails((BrokerActivity)fragment.getActivity(), rosterEntry.id, brokerClientId);
                }
            });
            return view;
        } catch (Exception e){
            Log.e(TAG, "Exception getting employee item", e);
            return null;
        }
    }
}
