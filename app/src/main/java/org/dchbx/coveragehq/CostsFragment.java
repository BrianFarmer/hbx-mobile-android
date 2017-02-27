package org.dchbx.coveragehq;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.microsoft.azure.mobile.analytics.Analytics;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.dchbx.coveragehq.models.roster.Roster;

/**
 * Created by plast on 10/21/2016.
 */

public class CostsFragment extends BrokerFragment {
    private View view;
    private Roster rosterResult;
    private String brokerClientId;
    private Roster roster;
    private LocalDate coverageYear;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.CoverageYear coverageYear) {
        this.coverageYear = coverageYear.getYear();

        Map<String,String> properties=new HashMap<String,String>();
        properties.put("CurrentTab", "Costs");
        Analytics.trackEvent("Coverage Year Changed", properties);

        populateList();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.RosterResult rosterResult) {
        roster = rosterResult.getRoster();

        Map<String,String> properties=new HashMap<String,String>();
        if (roster.roster == null) {
            properties.put("Roster Size", "NULL");
        } else {
            properties.put("Roster Size", Integer.toString(roster.roster.size()));

        }
        Analytics.trackEvent("Costs Tab", properties);


        EmployerDetailsActivity activity = (EmployerDetailsActivity) getActivity();
        this.coverageYear = activity.getCoverageYear();
        populateList();

    }

    private void populateList() {
        if (roster == null){
            return;
        }
        ListView listViewRoster = (ListView) view.findViewById(R.id.listViewEmployeeCosts);
        CostsAdapter costsAdapter = new CostsAdapter(this, this.getActivity(), new ArrayList<>(roster.roster), brokerClientId, coverageYear);
        listViewRoster.setAdapter(costsAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        init();

        view = LayoutInflater.from(getActivity()).inflate(R.layout.costs_fragment,
                null);

        if (rosterResult == null) {
            brokerClientId = getBrokerActivity().getIntent().getStringExtra(Intents.BROKER_CLIENT_ID);
            if (brokerClientId == null) {
                getMessages().getRoster();
                return view;
            }
            getMessages().getRoster(brokerClientId);
        }

        return view;
    }
}
