package gov.dc.broker;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import gov.dc.broker.models.roster.Employee;
import gov.dc.broker.models.roster.Roster;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by plast on 10/21/2016.
 */

public class RosterFragment extends BrokerFragment implements EmployeeFilterDialog.OnDialogFinishedListener{
    private View view;
    private Roster rosterResult;
    private int brokerClientId;
    private Roster roster;
    private String coverageYear;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.CoverageYear coverageYear) {
        this.coverageYear = coverageYear.getYear();
        populateRosterList();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(final Events.RosterResult rosterResult) {
        roster = rosterResult.getRoster();
        EmployerDetailsActivity activity = (EmployerDetailsActivity) getActivity();
        this.coverageYear = activity.getCoverageYear();
        populateRosterList();
   }

    private void populateRosterList() {
        ListView listViewRoster = (ListView) view.findViewById(R.id.listViewRoster);
        RosterAdapter rosterAdapter = new RosterAdapter(this, this.getActivity(),
                                                        new ArrayList<>(roster.roster), brokerClientId,
                                                        coverageYear.compareToIgnoreCase("active")==0);
        listViewRoster.setAdapter(rosterAdapter);
        ImageView imageViewDownArrow = (ImageView) view.findViewById(R.id.imageViewDownArrow);
        imageViewDownArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EmployeeFilterDialog dialog = EmployeeFilterDialog.build(RosterFragment.this);
                dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
                dialog.show(RosterFragment.this.getFragmentManager(), "SecurityQuestionDialog");
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        init();

        view = LayoutInflater.from(getActivity()).inflate(R.layout.roster_fragment,
                null);

        if (rosterResult == null) {
            brokerClientId = getBrokerActivity().getIntent().getIntExtra(Intents.BROKER_CLIENT_ID, -1);
            if (brokerClientId == -1) {
                // If we get here the employer id in the intent wasn't initialized and
                // we are in a bad state.
                Log.e(TAG, "onCreate: no client id found in intent");
                return view;
            }
            getMessages().getRoster(brokerClientId);
        }
        return view;
    }

    @Override
    public void canceled() {

    }

    @Override
    public void filter(String filterName) {
        boolean active = coverageYear.compareToIgnoreCase("active") == 0;
        ArrayList<Employee> filteredEmployees = new ArrayList<>();
        if (filteredEmployees == null){
            for(Employee employee : roster.roster){
                filteredEmployees.add(employee);
            }
        } else {

            if (active) {
                for(Employee employee : roster.roster){
                    if (employee.enrollments.active.health.status.compareToIgnoreCase(filterName) == 0){
                        filteredEmployees.add(employee);
                    }
                }
            } else {
                for(Employee employee : roster.roster){
                    if (employee.enrollments.renewal.health.status.compareToIgnoreCase(filterName) == 0){
                        filteredEmployees.add(employee);
                    }
                }
            }
        }

        RosterAdapter rosterAdapter = new RosterAdapter(this, this.getActivity(), filteredEmployees,
                                                        brokerClientId, active);
        ListView listViewRoster = (ListView) view.findViewById(R.id.listViewRoster);
        listViewRoster.setAdapter(rosterAdapter);
    }
}
