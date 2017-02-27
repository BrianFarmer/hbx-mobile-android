package org.dchbx.coveragehq;

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

import com.microsoft.azure.mobile.analytics.Analytics;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.dchbx.coveragehq.models.roster.Enrollment;
import org.dchbx.coveragehq.models.roster.Roster;
import org.dchbx.coveragehq.models.roster.RosterEntry;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by plast on 10/21/2016.
 */

public class RosterFragment extends BrokerFragment implements EmployeeFilterDialog.OnDialogFinishedListener{
    public static String NotEnrolledStatus = "Not Enrolled";
    public static String EnrolledStatus = "Enrolled";
    public static String WaivedStatus = "waived";
    public static String TerminatedStatus = "Terminated";
    private View view;
    private Roster rosterResult;
    private String brokerClientId;
    private Roster roster;
    private LocalDate coverageYear;
    private String filterName = "";
    private String filterLetter;
    private ArrayList<RosterEntry> rosterEntries;
    private ListView listViewRoster;
    ArrayList<RosterEntry> filteredEmployees = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.CoverageYear coverageYear) throws Exception {
        this.coverageYear = coverageYear.getYear();

        Map<String,String> properties=new HashMap<String,String>();
        properties.put("CurrentTab", "Roster");
        Analytics.trackEvent("Coverage Year Changed", properties);

        populateRosterList();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(final Events.RosterResult rosterResult) throws Exception {
        roster = rosterResult.getRoster();

        Map<String,String> properties=new HashMap<String,String>();
        if (roster.roster == null) {
            properties.put("Roster Size", "NULL");
        } else {
            properties.put("Roster Size", Integer.toString(roster.roster.size()));

        }
        Analytics.trackEvent("Roster Tab", properties);


        EmployerDetailsActivity activity = (EmployerDetailsActivity) getActivity();
        this.coverageYear = activity.getCoverageYear();
        populateRosterList();
   }

    private void populateSideIndex() {
        TreeMap<Character, Integer> foundChars = new TreeMap<>();
        for (int i = 0; i < filteredEmployees.size(); i ++){
            RosterEntry employee = filteredEmployees.get(i);
            if (employee.lastName != null
                && employee.lastName.length() > 0
                && foundChars.get(employee.lastName.charAt(0)) == null){
                foundChars.put(employee.lastName.charAt(0), i);
            }
        }

        LinearLayout linearLayoutSideIndex = (LinearLayout)view.findViewById(R.id.linearLayoutSideIndex);
        linearLayoutSideIndex.removeAllViews();
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        for (final Map.Entry<Character, Integer> entry: foundChars.entrySet()) {
            TextView sideIndexItem = (TextView) layoutInflater.inflate(R.layout.side_index_item, null);
            sideIndexItem.setText(entry.getKey().toString());
            sideIndexItem.setTag(entry.getValue());
            sideIndexItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RosterFragment.this.filterLetter = entry.getKey().toString();
                    try {
                        listViewRoster.setSelection((Integer)view.getTag());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            linearLayoutSideIndex.addView(sideIndexItem);
        }
    }

    private void populateRosterList() throws Exception {
        filterName = ((EmployerDetailsActivity) getActivity()).getRosterFilter();
        listViewRoster = (ListView) view.findViewById(R.id.listViewRoster);
        filter();
        ImageView imageViewDownArrow = (ImageView) view.findViewById(R.id.imageViewDownArrow);
        imageViewDownArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchEmployeeFilterDialog();
            }
        });
        TextView textViewStatusColumnHeader = (TextView) view.findViewById(R.id.textViewStatusColumnHeader);
        textViewStatusColumnHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchEmployeeFilterDialog();
            }
        });
    }

    private void launchEmployeeFilterDialog() {
        EmployeeFilterDialog dialog = EmployeeFilterDialog.build(RosterFragment.this);
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
        dialog.show(RosterFragment.this.getFragmentManager(), "SecurityQuestionDialog");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        init();

        Log.d(TAG, "RosterFragment.onCreateView()");

        view = LayoutInflater.from(getActivity()).inflate(R.layout.roster_fragment,
                null);

        if (rosterResult == null) {
            brokerClientId = getBrokerActivity().getIntent().getStringExtra(Intents.BROKER_CLIENT_ID);
            if (brokerClientId == null) {
                getMessages().getRoster();
            } else {
                getMessages().getRoster(brokerClientId);
            }
        } else {
            try {
                populateRosterList();
            } catch (Exception e) {
                Log.e(TAG, "exception populating InfoFragment", e);
            }
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

        filteredEmployees = new ArrayList<>();
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
        populateSideIndex();
    }
}
