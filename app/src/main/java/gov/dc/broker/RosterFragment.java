package gov.dc.broker;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import gov.dc.broker.models.roster.Enrollment;
import gov.dc.broker.models.roster.Roster;
import gov.dc.broker.models.roster.RosterEntry;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by plast on 10/21/2016.
 */

public class RosterFragment extends BrokerFragment implements EmployeeFilterDialog.OnDialogFinishedListener{
    private View view;
    private Roster rosterResult;
    private int brokerClientId;
    private Roster roster;
    private LocalDate coverageYear;
    private String filterName = "";
    private String filterLetter;

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
        populateSideIndex();
   }

    private void populateSideIndex() {
        TreeMap<Character, Integer> foundChars = new TreeMap<>();
        for (int i = 0; i < roster.roster.size(); i ++){
            RosterEntry employee = roster.roster.get(i);
            if (employee.lastName != null
                && employee.lastName.length() > 0
                && foundChars.get(employee.lastName.charAt(0)) == null){
                foundChars.put(employee.lastName.charAt(0), i);
            }
        }

        LinearLayout linearLayoutSideIndex = (LinearLayout)view.findViewById(R.id.linearLayoutSideIndex);
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        for (final Map.Entry<Character, Integer> entry: foundChars.entrySet()) {
            TextView sideIndexItem = (TextView) layoutInflater.inflate(R.layout.side_index_item, null);
            sideIndexItem.setText(entry.getKey().toString());
            sideIndexItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RosterFragment.this.filterLetter = entry.getKey().toString();
                    try {
                        RosterFragment.this.filter();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            linearLayoutSideIndex.addView(sideIndexItem);
        }
    }

    private void populateRosterList() {
        ListView listViewRoster = (ListView) view.findViewById(R.id.listViewRoster);
        ArrayList<RosterEntry> rosterEntries = new ArrayList<>();
        for (RosterEntry rosterEntry : roster.roster) {
            for (Enrollment enrollment : rosterEntry.enrollments) {
                if (enrollment.startOn.compareTo(coverageYear) == 0){
                    rosterEntries.add(rosterEntry);
                    break;
                }
            }
        }

        RosterAdapter rosterAdapter = new RosterAdapter(this, this.getActivity(),
                                                        rosterEntries, brokerClientId,
                                                        coverageYear);
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
    public void filter(String filterName) throws Exception {
        this.filterName = filterName;
        filter();
    }

    public void filter() throws Exception {
        char lowerCaseLetter = 'a';
        boolean compareLetter = false;
        if (filterLetter != null
            && filterLetter.length() > 0) {
            lowerCaseLetter = filterLetter.toLowerCase().charAt(0);
            compareLetter = true;
        }

        ArrayList<RosterEntry> filteredEmployees = new ArrayList<>();
        if (filterName == null
            || filterName.length() == 0){
            for(RosterEntry employee : roster.roster){
                if (compareLetter) {
                    if (employee.lastName.toLowerCase().charAt(0) == lowerCaseLetter) {
                        filteredEmployees.add(employee);
                    }
                } else {
                    filteredEmployees.add(employee);
                }
            }
        } else {
            for(RosterEntry employee : roster.roster){
                Enrollment enrollmentForCoverageYear = BrokerUtilities.getEnrollmentForCoverageYear(employee, coverageYear);
                if (compareLetter) {
                    if (employee.lastName.charAt(0) == lowerCaseLetter
                        && enrollmentForCoverageYear.health.status.compareToIgnoreCase(filterName) == 0) {
                        filteredEmployees.add(employee);
                    }
                } else {
                    if (enrollmentForCoverageYear.health.status.compareToIgnoreCase(filterName) == 0) {
                        filteredEmployees.add(employee);
                    }
                }
            }
        }

        RosterAdapter rosterAdapter = new RosterAdapter(this, this.getActivity(), filteredEmployees,
                                                        brokerClientId, coverageYear);
        ListView listViewRoster = (ListView) view.findViewById(R.id.listViewRoster);
        listViewRoster.setAdapter(rosterAdapter);
    }
}
