package gov.dc.broker;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

import java.util.ArrayList;

import gov.dc.broker.models.roster.Roster;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

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
        populateList();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.RosterResult rosterResult) {
        roster = rosterResult.getRoster();
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
                // If we get here the employer id in the intent wasn't initialized and
                // we are in a bad state.
                Log.e(TAG, "onCreate: no client id found in intent");
                return view;
            }
            getMessages().getRoster(brokerClientId);
        }

        return view;
    }
}
