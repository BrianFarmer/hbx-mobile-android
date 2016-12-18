package gov.dc.broker;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

import java.util.ArrayList;

import gov.dc.broker.models.brokeragency.BrokerClient;
import gov.dc.broker.models.brokeragency.PlanYear;
import gov.dc.broker.models.roster.Roster;

/**
 * Created by plast on 10/21/2016.
 */

public class InfoFragment extends BrokerFragment {
    private static String TAG = "BrokerFragment";

    private String brokerClientId;
    private Roster roster = null;
    private BrokerClient brokerClient = null;
    private View view;
    private static boolean renewalsInitiallyOpen = true;
    private static boolean participationInitiallyOpen = false;
    private static boolean monthlyCostsInitiallyOpen = false;
    private boolean currentRenewalsOpen;
    private boolean currentParticipationOpen;
    private boolean currentMonthlyCostsOpen;
    private LocalDate coverageYear;


    public InfoFragment(){
        currentRenewalsOpen = renewalsInitiallyOpen;
        currentParticipationOpen = participationInitiallyOpen;
        currentMonthlyCostsOpen = monthlyCostsInitiallyOpen;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        init();

        // TODO Auto-generated method stub
        view = LayoutInflater.from(getActivity()).inflate(R.layout.info_fragment, null);

        if (brokerClient == null) {
            brokerClientId = getBrokerActivity().getIntent().getStringExtra(Intents.BROKER_CLIENT_ID);
            if (brokerClientId == null) {
                // If we get here the employer id in the intent wasn't initialized and
                // we are in a bad state.
                Log.e(TAG, "onCreate: no client id found in intent");
                return view;
            }
            getMessages().getEmployer(brokerClientId);
            getMessages().getRoster(brokerClientId);
        } else {
            try {
                populateField();
            } catch (Exception e) {
                Log.e(TAG, "exception populating InfoFragment", e);
            }
        }

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        configureDrawers();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.BrokerClient brokerClientEvent) {
        brokerClient = brokerClientEvent.getBrokerClient();
        EmployerDetailsActivity activity = (EmployerDetailsActivity) getActivity();
        this.coverageYear = activity.getCoverageYear();
        configureDrawers();
        try {
            populateField();
        } catch (Exception e) {
            Log.e(TAG, "exception populting fields in dothis(BrokerClient");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.RosterResult rosterResult) {
        roster = rosterResult.getRoster();
        EmployerDetailsActivity activity = (EmployerDetailsActivity) getActivity();
        this.coverageYear = activity.getCoverageYear();
        configureDrawers();
        try {
            populateField();
        } catch (Exception e) {
            Log.e(TAG, "exception populating infofragment in dothis(RosterResult)", e);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.CoverageYear coverageYear) {
        this.coverageYear = coverageYear.getYear();
        try {
            populateField();
        } catch (Exception e) {
            Log.e(TAG, "exception populating infofragment in dothis(CoverageYear)", e);
        }
    }

    private void configureDrawers(){
        ImageView renewalDeadlines = (ImageView) view.findViewById(R.id.imageViewRenewalDeadlines);
        setVisibility(view, R.string.renewal_group, currentRenewalsOpen, R.id.imageViewRenewalDeadlines, R.drawable.uparrow, R.drawable.circle_plus);
        renewalDeadlines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentRenewalsOpen = invertGroup(view, R.string.renewal_group, R.id.imageViewRenewalDeadlines, R.drawable.uparrow, R.drawable.circle_plus);
            }
        });
        ImageView participation = (ImageView) view.findViewById(R.id.imageViewParticipation);
        setVisibility(view, R.string.participation_group, currentParticipationOpen, R.id.imageViewParticipation, R.drawable.uparrow, R.drawable.circle_plus);
        participation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentParticipationOpen = invertGroup(view, R.string.participation_group, R.id.imageViewParticipation, R.drawable.uparrow, R.drawable.circle_plus);
            }
        });
        final ImageView monthlyCosts = (ImageView) view.findViewById(R.id.imageViewMonthlyCosts);
        setVisibility(view, R.string.monthly_costs_group, currentMonthlyCostsOpen, R.id.imageViewMonthlyCosts, R.drawable.uparrow, R.drawable.circle_plus);
        monthlyCosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonthlyCostsOpen = invertGroup(view, R.string.monthly_costs_group, R.id.imageViewMonthlyCosts, R.drawable.uparrow, R.drawable.circle_plus);
            }
        });
    }

    private void populateField() throws Exception {
        if (brokerClient == null
            || roster == null){
            return;
        }

        if (brokerClient.planYears != null
            && brokerClient.planYears.size() > 0) {
            LocalDate now = LocalDate.now();
            PlanYear planYearForCoverageYear = BrokerUtilities.getPlanYearForCoverageYear(brokerClient, coverageYear);

            TextView textViewEmployerApplicationDueLabel = (TextView) view.findViewById(R.id.textViewEmployerApplicationDueLabel);
            TextView textViewEmployerApplicationDue = (TextView) view.findViewById(R.id.textViewEmployerApplicationDue);
            if (planYearForCoverageYear.renewalApplicationDue != null) {
                textViewEmployerApplicationDue.setVisibility(View.VISIBLE);
                textViewEmployerApplicationDueLabel.setVisibility(View.VISIBLE);
                textViewEmployerApplicationDue.setText(Utilities.DateAsString(planYearForCoverageYear.renewalApplicationDue));
            } else {
                textViewEmployerApplicationDue.setVisibility(View.GONE);
                textViewEmployerApplicationDueLabel.setVisibility(View.GONE);
            }

            TextView openEnrollmentBeginsLabel = (TextView) view.findViewById(R.id.textViewOpenEnrollmentBeginsLabel);
            TextView openEnrollmentBegins = (TextView) view.findViewById(R.id.textViewOpenEnrollmentBegins);
            if (planYearForCoverageYear.openEnrollmentBegins != null) {
                openEnrollmentBeginsLabel.setVisibility(View.VISIBLE);
                openEnrollmentBegins.setVisibility(View.VISIBLE);
                if (planYearForCoverageYear.openEnrollmentBegins.compareTo(now) < 0) {
                    openEnrollmentBeginsLabel.setText(getResources().getText(R.string.open_enrollment_begins_label_past));
                } else {
                    openEnrollmentBeginsLabel.setText(getResources().getText(R.string.open_enrollment_begins_label_future));
                }
                openEnrollmentBegins.setText(Utilities.DateAsString(planYearForCoverageYear.openEnrollmentBegins));
            } else {
                openEnrollmentBeginsLabel.setVisibility(View.GONE);
                openEnrollmentBegins.setVisibility(View.GONE);
            }

            TextView openEnrollmentEndsLabel = (TextView) view.findViewById(R.id.textViewOpenEnrollmentEndsLabel);
            TextView openEnrollmentEnds = (TextView) view.findViewById(R.id.textViewOpenEnrollmentEnds);
            if (planYearForCoverageYear.openEnrollmentEnds != null) {
                openEnrollmentEnds.setVisibility(View.VISIBLE);
                openEnrollmentEndsLabel.setVisibility(View.VISIBLE);
                if (planYearForCoverageYear.openEnrollmentEnds.compareTo(now) < 0) {
                    openEnrollmentEndsLabel.setText(R.string.open_enrollment_ends_label_past);
                } else {
                    openEnrollmentEndsLabel.setText(R.string.open_enrollment_ends_label_future);
                }
                openEnrollmentEnds.setText(Utilities.DateAsString(planYearForCoverageYear.openEnrollmentEnds));
            } else {
                openEnrollmentEnds.setVisibility(View.GONE);
                openEnrollmentEndsLabel.setVisibility(View.GONE);
            }

            TextView textViewCoverageBeginsLabel = (TextView) view.findViewById(R.id.textViewCoverageBeginsLabel);
            TextView textViewCoverageBegins = (TextView) view.findViewById(R.id.textViewCoverageBegins);
            if (planYearForCoverageYear.planYearBegins != null){
                textViewCoverageBeginsLabel.setVisibility(View.VISIBLE);
                textViewCoverageBegins.setVisibility(View.VISIBLE);
                textViewCoverageBegins.setText(Utilities.DateAsString(planYearForCoverageYear.planYearBegins));
            } else {
                textViewCoverageBeginsLabel.setVisibility(View.GONE);
                textViewCoverageBegins.setVisibility(View.GONE);
            }
            TextView daysLeft = (TextView) view.findViewById(R.id.textViewDaysLeft);
            daysLeft.setText(now.compareTo(coverageYear) < 0 ? Long.toString(BrokerUtilities.daysLeft(planYearForCoverageYear, now)):"");
            BrokerUtilities.EmployeeCounts employeeCounts = BrokerUtilities.getEmployeeCounts(roster, coverageYear);
            TextView textViewEnrolled = (TextView) view.findViewById(R.id.textViewEnrolled);
            textViewEnrolled.setText(Integer.toString(employeeCounts.Enrolled));
            TextView textViewWaived = (TextView) view.findViewById(R.id.textViewWaived);
            textViewWaived.setText(Integer.toString(employeeCounts.Waived));
            TextView textViewNotEnrolled = (TextView) view.findViewById(R.id.textViewNotEnrolled);
            textViewNotEnrolled.setText(Integer.toString(employeeCounts.NotEnrolled));
            TextView textViewTotalEmployees = (TextView) view.findViewById(R.id.textViewTotalEmployees);
            textViewTotalEmployees.setText(Integer.toString(employeeCounts.Enrolled + employeeCounts.NotEnrolled + employeeCounts.Waived));


            TextView textViewEnrolledLabel = (TextView) view.findViewById(R.id.textViewEnrolledLabel);
            textViewEnrolledLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EmployerDetailsActivity activity = (EmployerDetailsActivity) getActivity();
                    activity.showRoster(RosterFragment.EnrolledStatus);
                }
            });
            TextView textViewNotEnrolledLabel = (TextView) view.findViewById(R.id.textViewNotEnrolledLabel);
            textViewNotEnrolledLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EmployerDetailsActivity activity = (EmployerDetailsActivity) getActivity();
                    activity.showRoster(RosterFragment.NotEnrolledStatus);
                }
            });
            TextView textViewWaivedLabel = (TextView) view.findViewById(R.id.textViewWaivedLabel);
            textViewWaivedLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EmployerDetailsActivity activity = (EmployerDetailsActivity) getActivity();
                    activity.showRoster(RosterFragment.WaivedStatus);
                }
            });
            configurePieChartData(employeeCounts);
        }
        BrokerUtilities.Totals totals = BrokerUtilities.calcTotals(roster, coverageYear);
        TextView textViewEmployerContribution = (TextView)view.findViewById(R.id.textViewEmployerContribution);
        textViewEmployerContribution.setText(String.format("$%.2f", totals.employerTotal));
        TextView textViewEmployeeContribution = (TextView)view.findViewById(R.id.textViewEmployeeContribution);
        textViewEmployeeContribution.setText(String.format("$%.2f", totals.employeeTotal));
        TextView textViewTotal = (TextView)view.findViewById(R.id.textViewTotal);
        textViewTotal.setText(String.format("$%.2f", totals.total));
    }

    private void configurePieChartData(BrokerUtilities.EmployeeCounts employeeCounts) {
        PieChart pieChart = (PieChart) view.findViewById(R.id.pieChart);
        pieChart.setUsePercentValues(false);
        pieChart.setDrawEntryLabels(true);
        pieChart.setDrawHoleEnabled(false);
        pieChart.setDescription(null);
        pieChart.getLegend().setEnabled(false);
        pieChart.setCenterText("");
        pieChart.setDrawSliceText(false);

        ArrayList<PieEntry> yValues = new ArrayList<>();

        yValues.add(new PieEntry(2*employeeCounts.Enrolled, Integer.toString(employeeCounts.Enrolled)));
        yValues.add(new PieEntry(2*employeeCounts.Waived, Integer.toString(employeeCounts.Waived)));
        int notEnrolledCount = 2*(brokerClient.employeesTotal - (employeeCounts.Enrolled + employeeCounts.Waived));
        yValues.add(new PieEntry(notEnrolledCount, Integer.toString(notEnrolledCount)));

        PieDataSet dataSet = new PieDataSet(yValues, "");
        dataSet.setValueTextSize(16f);
        dataSet.setValueTextColor(ContextCompat.getColor(this.getActivity(), R.color.white));
        dataSet.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return Integer.toString((int)(value/2));
            }
        });
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(this.getActivity(), R.color.enrolled_color));
        colors.add(ContextCompat.getColor(this.getActivity(), R.color.waived_color));
        colors.add(ContextCompat.getColor(this.getActivity(), R.color.not_enrolled_color));
        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
    }
}