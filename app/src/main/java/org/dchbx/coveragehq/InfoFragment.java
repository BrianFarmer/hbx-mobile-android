package org.dchbx.coveragehq;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.microsoft.azure.mobile.analytics.Analytics;

import org.dchbx.coveragehq.models.brokeragency.BrokerClient;
import org.dchbx.coveragehq.models.employer.Employer;
import org.dchbx.coveragehq.models.employer.PlanYear;
import org.dchbx.coveragehq.models.roster.Roster;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.dchbx.coveragehq.RosterFragment.EnrolledStatus;
import static org.dchbx.coveragehq.RosterFragment.NotEnrolledStatus;
import static org.dchbx.coveragehq.RosterFragment.TerminatedStatus;
import static org.dchbx.coveragehq.RosterFragment.WaivedStatus;

/**
 * Created by plast on 10/21/2016.
 */

public class InfoFragment extends BrokerFragment {
    private static String TAG = "InfoFragment";

    private String brokerClientId;
    private Roster roster = null;
    private BrokerClient brokerClient = null;
    private Employer employer = null;
    private View view;
    private static boolean renewalsInitiallyOpen = true;
    private static boolean participationInitiallyOpen = false;
    private static boolean monthlyCostsInitiallyOpen = false;
    private boolean currentRenewalsOpen;
    private boolean currentParticipationOpen;
    private boolean currentMonthlyCostsOpen;
    private LocalDate coverageYear;
    private ArrayList<ControlState> controlStates;
    private ArrayList<String> filterStrings;
    private Boolean renewalsDrawerShown = true;
    private boolean participationDrawerShown = true;
    private boolean costsDrawerShown = true;
    private int getEmployerRequestId = 0;
    private PieData data;
    private Boolean employerReady = false;

    class ControlState {
        public View view;
        public boolean visible = false;
    }

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
        try {

            view = LayoutInflater.from(getActivity()).inflate(R.layout.info_fragment, null);
        } catch (Exception e){
            Log.e(TAG, "Exception infloating view", e);
            throw e;
        }
        init();
        return view;
    }



    private boolean fetchData() {
        EmployerDetailsActivity activity = (EmployerDetailsActivity) getActivity();
        if (activity.isInErrorState()){
            return false;
        }

        if (employer == null) {
            brokerClientId = getBrokerActivity().getIntent().getStringExtra(Intents.BROKER_CLIENT_ID);
            if (brokerClientId == null) {
                Log.e(TAG, "onCreate: no client id found in intent");
                brokerClient = null;
                employer = null;
                roster = null;

                getMessages().getEmployer(null);
                getMessages().getRoster(null);
                return true;
            }
            getEmployerRequestId = getMessages().getEmployer(brokerClientId);
            getMessages().getRoster(brokerClientId);
        } else {
            try {
                populateField();
            } catch (Exception e) {
                Log.e(TAG, "exception populating InfoFragment", e);
            }
        }
        return false;
    }

    @Override
    public void onResume(){
        super.onResume();
        if (employerReady){
            fetchData();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void doThis(Events.EmployerActivityReady employerReady) {
        this.employerReady = true;
        fetchData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Error error){
        Log.d(TAG, "handling error in InfoFragment");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.BrokerClient brokerClientEvent) {

        if (brokerClient != null) {
            return;
        }
        brokerClient = brokerClientEvent.getBrokerClient();
        employer = brokerClientEvent.getEmployer();

        Map<String,String> properties=new HashMap<String,String>();
        if (brokerClient != null) {
            if (brokerClient.planYears.size() > 0) {
                org.dchbx.coveragehq.models.brokeragency.PlanYear planYear = BrokerUtilities.getMostRecentPlanYear(brokerClient);
                properties.put("Status", BrokerUtilities.getBrokerClientStatus(planYear, planYear.planYearBegins).name());
            }
        } else {
            if (employer.planYears.size() > 0) {
                PlanYear planYear = BrokerUtilities.getMostRecentPlanYear(employer);
                properties.put("Status", BrokerUtilities.getBrokerClientStatus(planYear, planYear.planYearBegins).name());
            }
        }
        Analytics.trackEvent("Info Tab", properties);


        EmployerDetailsActivity activity = (EmployerDetailsActivity) getActivity();
        this.coverageYear = activity.getCoverageYear();
        try {
            populateField();
        } catch (Exception e) {
            Log.e(TAG, "exception populting dependentFields in dothis(BrokerClient");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.RosterResult rosterResult) {
        if (roster != null){
            return;
        }

        roster = rosterResult.getRoster();
        EmployerDetailsActivity activity = (EmployerDetailsActivity) getActivity();
        try {
            populateField();
        } catch (Exception e) {
            Log.e(TAG, "exception populating infofragment in dothis(RosterResult)", e);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.CoverageYear coverageYear) {
        LocalDate newCoverageYear = coverageYear.getYear();

        if (this.coverageYear.compareTo(newCoverageYear) == 0){
            return;
        }

        this.coverageYear = newCoverageYear;


        Map<String,String> properties=new HashMap<String,String>();
        properties.put("CurrentTab", "Info");
        Analytics.trackEvent("Coverage Year Changed", properties);

        try {
            populateField();
        } catch (Exception e) {
            Log.e(TAG, "exception populating infofragment in dothis(CoverageYear)", e);
        }
    }

    private void showRenewals(Boolean show, boolean open){
        final ImageView renewalDeadlines = (ImageView) view.findViewById(R.id.imageViewRenewalDeadlines);
        final RelativeLayout relativeLayoutRenewalGroup = (RelativeLayout) view.findViewById(R.id.relativeLayoutRenewalGroup);
        final RelativeLayout relativeLayoutRenewalItemsGroup = (RelativeLayout) view.findViewById(R.id.relativeLayoutRenewalItemsGroup);
        if (show){
            relativeLayoutRenewalGroup.setVisibility(View.VISIBLE);
            renewalDeadlines.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentRenewalsOpen = !currentRenewalsOpen;
                    relativeLayoutRenewalItemsGroup.setVisibility(currentRenewalsOpen?View.VISIBLE:View.GONE);
                    renewalDeadlines.setImageResource(currentRenewalsOpen?R.drawable.uparrow:R.drawable.circle_plus);
                }
            });
        } else {
            relativeLayoutRenewalGroup.setVisibility(View.GONE);
            renewalDeadlines.setOnClickListener(null);
        }
    }

    private void showParticipation(Boolean show, final boolean open){
        final ImageView participation = (ImageView) view.findViewById(R.id.imageViewParticipation);
        final View relativeLayoutParticipationGroup = view.findViewById(R.id.relativeLayoutParticipationGroup);

        currentParticipationOpen = open;
        participation.setVisibility(show?View.VISIBLE:View.GONE);
        participation.setImageResource(open?R.drawable.uparrow:R.drawable.circle_plus);
        relativeLayoutParticipationGroup.setVisibility(open?View.VISIBLE:View.GONE);
        currentParticipationOpen = open;

        if (show){
            participation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentParticipationOpen = !currentParticipationOpen;
                    relativeLayoutParticipationGroup.setVisibility(currentParticipationOpen ? View.VISIBLE : View.GONE);
                    participation.setImageResource(currentParticipationOpen?R.drawable.uparrow:R.drawable.circle_plus);
                    configurePieChartData(BrokerUtilities.getEmployeeCounts(roster, coverageYear));
                }
            });
        } else {
            participation.setOnClickListener(null);
        }
    }

    private void showCosts(Boolean show, boolean open){
        final TextView textViewMonthlyCosts = (TextView) view.findViewById(R.id.textViewMonthlyCosts);
        final ImageView monthlyCosts = (ImageView) view.findViewById(R.id.imageViewMonthlyCosts);
        final RelativeLayout relativeLayoutCostsGroup = (RelativeLayout) view.findViewById(R.id.relativeLayoutCostsGroup);
        if (show){
            textViewMonthlyCosts.setVisibility(View.VISIBLE);
            monthlyCosts.setVisibility(View.VISIBLE);
            monthlyCosts.setImageResource(currentMonthlyCostsOpen?R.drawable.uparrow:R.drawable.circle_plus);
            relativeLayoutCostsGroup.setVisibility(open?View.VISIBLE:View.GONE);
            monthlyCosts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                currentMonthlyCostsOpen = !currentMonthlyCostsOpen;
                relativeLayoutCostsGroup.setVisibility(currentMonthlyCostsOpen?View.VISIBLE:View.GONE);
                monthlyCosts.setImageResource(currentMonthlyCostsOpen?R.drawable.uparrow:R.drawable.circle_plus);
                }
            });
        } else {
            textViewMonthlyCosts.setVisibility(View.GONE);
            monthlyCosts.setVisibility(View.GONE);
            relativeLayoutCostsGroup.setVisibility(View.GONE);
            monthlyCosts.setOnClickListener(null);
        }
    }

    private void configureDrawers(){

        if (employer == null
            || employer.planYears == null
            || employer.planYears.size() == 0){

            showRenewals(false, false);
            showParticipation(false, false);
            showCosts(false, false);
        } else {

            showRenewals(renewalsDrawerShown, renewalsDrawerShown);
            showParticipation(participationDrawerShown, !renewalsDrawerShown);
            showCosts(costsDrawerShown, false);
        }
    }

    private void setVisibility(boolean show){
        ImageView imageView = (ImageView) view.findViewById(R.id.imageViewRenewalDeadlines);
        if (show){
            imageView.setImageResource(R.drawable.uparrow);
            for (ControlState controlState : controlStates) {
                controlState.view.setVisibility(controlState.visible ? View.VISIBLE : View.GONE);
            }
        } else {
            imageView.setImageResource(R.drawable.circle_plus);
            for (ControlState controlState : controlStates) {
            controlState.view.setVisibility(View.GONE);
            }
        }
    }

    private void populateField() throws Exception {
        if (employer == null
            || roster == null){
            return;
        }

        controlStates = new ArrayList<>();
        if (employer.planYears != null
            && employer.planYears.size() > 0) {

            LocalDate today = LocalDate.now();

            org.dchbx.coveragehq.models.employer.PlanYear planYearForCoverageYear = BrokerUtilities.getPlanYearForCoverageYear(employer, coverageYear);
            PlanYear mostRecentPlanYear = BrokerUtilities.getMostRecentPlanYear(employer);


            renewalsDrawerShown = true;

            if (planYearForCoverageYear.planYearBegins != null
                && today.compareTo(planYearForCoverageYear.planYearBegins) >= 0) {
                if (today.compareTo(planYearForCoverageYear.planYearBegins.plusYears(1)) >= 0){
                    currentParticipationOpen = true;
                    renewalsDrawerShown = false;
                } else {
                    initGoneControls(R.id.textViewOpenEnrollmentBeginsLabel, R.id.textViewOpenEnrollmentBegins);
                    initGoneControls(R.id.textViewOpenEnrollmentEndsLabel, R.id.textViewOpenEnrollmentEnds);
                    initGoneControls(R.id.textViewDaysLeftLabel, R.id.textViewDaysLeft);
                    initGoneControls(R.id.textViewEmployerApplicationDueLabel, R.id.textViewEmployerApplicationDue);
                    initGoneControls(R.id.textViewCoverageBeginsLabel, R.id.textViewCoverageBegins);
                    initVisibleControls(R.id.textViewRenewalAvailableLabel, R.id.textViewRenewalAvailable, planYearForCoverageYear.planYearBegins.plusMonths(9));
                }
            }
            else {
                if (planYearForCoverageYear.openEnrollmentBegins != null) {
                    if (today.compareTo(planYearForCoverageYear.openEnrollmentBegins) >= 0) {
                        initVisibleControls(R.id.textViewOpenEnrollmentBeginsLabel, R.id.textViewOpenEnrollmentBegins, R.string.open_enrollment_begins_label_past, planYearForCoverageYear.openEnrollmentBegins);
                    } else {
                        if (today.compareTo(planYearForCoverageYear.planYearBegins) < 0
                                && (planYearForCoverageYear.openEnrollmentEnds == null
                                || today.compareTo(planYearForCoverageYear.openEnrollmentEnds) <= 0)) {
                            initVisibleControls(R.id.textViewOpenEnrollmentBeginsLabel, R.id.textViewOpenEnrollmentBegins, R.string.open_enrollment_begins_label_future, planYearForCoverageYear.openEnrollmentBegins);
                        } else {
                            initGoneControls(R.id.textViewOpenEnrollmentBeginsLabel, R.id.textViewOpenEnrollmentBegins);
                        }
                    }
                } else {
                    initGoneControls(R.id.textViewOpenEnrollmentBeginsLabel, R.id.textViewOpenEnrollmentBegins);
                }

                if (planYearForCoverageYear.openEnrollmentEnds != null
                        && today.compareTo(planYearForCoverageYear.planYearBegins) <= 0) {
                    if (today.compareTo(planYearForCoverageYear.openEnrollmentEnds) <= 0) {
                        initVisibleControls(R.id.textViewOpenEnrollmentEndsLabel, R.id.textViewOpenEnrollmentEnds, R.string.open_enrollment_ends_label_future, planYearForCoverageYear.openEnrollmentEnds);
                        initVisibleControls(R.id.textViewDaysLeftLabel, R.id.textViewDaysLeft, today.compareTo(coverageYear) < 0 ? Long.toString(BrokerUtilities.daysLeft(planYearForCoverageYear, today)) : "");
                    } else {
                        initVisibleControls(R.id.textViewOpenEnrollmentEndsLabel, R.id.textViewOpenEnrollmentEnds, R.string.open_enrollment_ends_label_past, planYearForCoverageYear.openEnrollmentEnds);
                        initGoneControls(R.id.textViewDaysLeftLabel, R.id.textViewDaysLeft);
                    }
                } else {
                    initGoneControls(R.id.textViewOpenEnrollmentEndsLabel, R.id.textViewOpenEnrollmentEnds);
                    initGoneControls(R.id.textViewDaysLeftLabel, R.id.textViewDaysLeft);
                }

                if (planYearForCoverageYear.renewalInProgress == false
                        && !BrokerUtilities.isInOpenEnrollment(planYearForCoverageYear, today)) {
                    initVisibleControls(R.id.textViewRenewalAvailableLabel, R.id.textViewRenewalAvailable, planYearForCoverageYear.renewalApplicationAvailable);
                } else {
                    initGoneControls(R.id.textViewRenewalAvailableLabel, R.id.textViewRenewalAvailable);
                }

                if (planYearForCoverageYear.renewalInProgress
                        && planYearForCoverageYear.renewalApplicationDue != null
                        && !BrokerUtilities.isInOpenEnrollment(planYearForCoverageYear, today)) {
                    initVisibleControls(R.id.textViewEmployerApplicationDueLabel, R.id.textViewEmployerApplicationDue, planYearForCoverageYear.renewalApplicationDue);
                } else {
                    initGoneControls(R.id.textViewEmployerApplicationDueLabel, R.id.textViewEmployerApplicationDue);
                }


                if (planYearForCoverageYear.planYearBegins != null
                        && !BrokerUtilities.isInOpenEnrollment(planYearForCoverageYear, today)) {
                    if (planYearForCoverageYear.renewalInProgress) {
                        initVisibleControls(R.id.textViewCoverageBeginsLabel, R.id.textViewCoverageBegins, R.string.coverage_begins_label, planYearForCoverageYear.planYearBegins);
                    } else {
                        initVisibleControls(R.id.textViewCoverageBeginsLabel, R.id.textViewCoverageBegins, R.string.next_coverage_year_begins, planYearForCoverageYear.planYearBegins);
                    }
                } else {
                    initGoneControls(R.id.textViewCoverageBeginsLabel, R.id.textViewCoverageBegins);
                }
            }

            setVisibility(true);

            if (roster == null
                || roster.roster == null
                || roster.roster.size() == 0) {
                participationDrawerShown = false;
                costsDrawerShown = false;

                TextView textViewEnrolled = (TextView) view.findViewById(R.id.textViewEnrolled);
                textViewEnrolled.setText("0");
                TextView textViewWaived = (TextView) view.findViewById(R.id.textViewWaived);
                textViewWaived.setText("0");
                TextView textViewNotEnrolled = (TextView) view.findViewById(R.id.textViewNotEnrolled);
                textViewNotEnrolled.setText("0");
                TextView textViewTotalEmployees = (TextView) view.findViewById(R.id.textViewTotalEmployees);
                textViewTotalEmployees.setText("0");
            } else {
                participationDrawerShown = true;
                costsDrawerShown = true;

                BrokerUtilities.EmployeeCounts employeeCounts = BrokerUtilities.getEmployeeCounts(roster, coverageYear);
                TextView textViewEnrolled = (TextView) view.findViewById(R.id.textViewEnrolled);
                textViewEnrolled.setText(Integer.toString(employeeCounts.Enrolled));
                TextView textViewWaived = (TextView) view.findViewById(R.id.textViewWaived);
                textViewWaived.setText(Integer.toString(employeeCounts.Waived));
                TextView textViewNotEnrolled = (TextView) view.findViewById(R.id.textViewNotEnrolled);
                textViewNotEnrolled.setText(Integer.toString(employeeCounts.NotEnrolled));
                TextView textViewTerminated = (TextView) view.findViewById(R.id.textViewTerminated);
                textViewTerminated.setText(Integer.toString(employeeCounts.Terminated));
                TextView textViewTotalEmployees = (TextView) view.findViewById(R.id.textViewTotalEmployees);
                textViewTotalEmployees.setText(Integer.toString(employeeCounts.Enrolled + employeeCounts.NotEnrolled + employeeCounts.Waived + employeeCounts.Terminated));


                TextView textViewEnrolledLabel = (TextView) view.findViewById(R.id.textViewEnrolledLabel);
                textViewEnrolledLabel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EmployerDetailsActivity activity = (EmployerDetailsActivity) getActivity();
                        activity.showRoster(EnrolledStatus);
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
                TextView textViewTerminatedLabel = (TextView) view.findViewById(R.id.textViewTerminatedLabel);
                textViewTerminatedLabel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EmployerDetailsActivity activity = (EmployerDetailsActivity) getActivity();
                        activity.showRoster(RosterFragment.TerminatedStatus);
                    }
                });


                configurePieChartData(employeeCounts);

                BrokerUtilities.Totals totals = BrokerUtilities.calcTotals(roster, coverageYear);
                TextView textViewEmployerContribution = (TextView)view.findViewById(R.id.textViewEmployerContribution);
                textViewEmployerContribution.setText(String.format("$%.2f", totals.employerTotal));
                TextView textViewEmployeeContribution = (TextView)view.findViewById(R.id.textViewEmployeeContribution);
                textViewEmployeeContribution.setText(String.format("$%.2f", totals.employeeTotal));
                TextView textViewTotal = (TextView)view.findViewById(R.id.textViewTotal);
                textViewTotal.setText(String.format("$%.2f", totals.total));
            }
        } else {
            initGoneControls(R.id.textViewRenewalAvailableLabel, R.id.textViewRenewalAvailable);
            initGoneControls(R.id.textViewEmployerApplicationDueLabel, R.id.textViewEmployerApplicationDue);
            initGoneControls(R.id.textViewOpenEnrollmentBeginsLabel, R.id.textViewOpenEnrollmentBegins);
            initGoneControls(R.id.textViewOpenEnrollmentEndsLabel, R.id.textViewOpenEnrollmentEnds);
            initGoneControls(R.id.textViewCoverageBeginsLabel, R.id.textViewCoverageBegins);
            initGoneControls(R.id.textViewDaysLeftLabel, R.id.textViewDaysLeft);
            setVisibility(true);
        }
        configureDrawers();
    }

    private void initGoneDrawer(int textViewDrawerLabel, int imageViewId) {
        buildControlState(textViewDrawerLabel);
        buildControlState(imageViewId);
    }

    private void initVisibleDrawer(int textViewDrawerLabel, int imageViewId) {
        ControlState controlState = buildControlState(textViewDrawerLabel);
        controlState.visible = true;
        controlState = buildControlState(imageViewId);
        controlState.visible = true;
    }

    private ControlState buildControlState(int viewId) {
        ControlState controlState = new ControlState();
        controlState.view = view.findViewById(viewId);
        controlState.visible = false;
        controlStates.add(controlState);
        return controlState;
    }

    private void initVisibleControls(int labelId, int fieldId, int lableStringId, LocalDate dateTime){
        ControlState labelControlState = buildControlState(labelId);
        ((TextView)labelControlState.view).setText(getText(lableStringId));
        labelControlState.visible = true;

        ControlState fieldControlState = buildControlState(fieldId);
        ((TextView)fieldControlState.view).setText(Utilities.DateAsString(dateTime));
        fieldControlState.visible = true;
    }

    private void initVisibleControls(int labelId, int fieldId, LocalDate dateTime){
        ControlState labelControlState = buildControlState(labelId);
        labelControlState.visible = true;

        ControlState fieldControlState = buildControlState(fieldId);
        ((TextView)fieldControlState.view).setText(Utilities.DateAsString(dateTime));
        fieldControlState.visible = true;
    }

    private void initVisibleControls(int labelId, int fieldId, String text){
        ControlState labelControlState = buildControlState(labelId);
        labelControlState.visible = true;

        ControlState fieldControlState = buildControlState(fieldId);
        ((TextView)fieldControlState.view).setText(text);
        fieldControlState.visible = true;
    }

    private void initGoneControls(int labelId, int fieldId){
        buildControlState(labelId);
        buildControlState(fieldId);
    }

    private void configurePieChartData(BrokerUtilities.EmployeeCounts employeeCounts) {
        PieChart pieChart = (PieChart) view.findViewById(R.id.pieChart);
        pieChart.setTouchEnabled(true);
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e == null){
                    return;
                }
                ((EmployerDetailsActivity)getActivity()).showRoster((String)e.getData());
            }

            @Override
            public void onNothingSelected() {

            }
        });
        pieChart.setUsePercentValues(false);
        pieChart.setDrawEntryLabels(true);
        pieChart.setDrawHoleEnabled(false);
        pieChart.setDescription(null);
        pieChart.getLegend().setEnabled(false);
        pieChart.setCenterText("");
        pieChart.setDrawSliceText(false);

        ArrayList<PieEntry> yValues = new ArrayList<>();
        filterStrings = new ArrayList<>();

        if (employeeCounts.Enrolled > 0) {
            yValues.add(new PieEntry(2*employeeCounts.Enrolled, Integer.toString(employeeCounts.Enrolled), EnrolledStatus));
            filterStrings.add(EnrolledStatus);
        }
        if (employeeCounts.Waived > 0) {
            yValues.add(new PieEntry(2 * employeeCounts.Waived, Integer.toString(employeeCounts.Waived), WaivedStatus));
            filterStrings.add(WaivedStatus);
        }
        int notEnrolledCount = 2*(employeeCounts.Total - (employeeCounts.Enrolled + employeeCounts.Waived));
        if (notEnrolledCount > 0) {
            yValues.add(new PieEntry(notEnrolledCount, Integer.toString(notEnrolledCount), NotEnrolledStatus));
            filterStrings.add(NotEnrolledStatus);
        }
        if (employeeCounts.Terminated > 0){
            yValues.add(new PieEntry(2 * employeeCounts.Terminated, Integer.toString(employeeCounts.Terminated), TerminatedStatus));
            filterStrings.add(TerminatedStatus);
        }

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
        if (employeeCounts.Enrolled > 0) {
            colors.add(ContextCompat.getColor(this.getActivity(), R.color.enrolled_color));
        }
        if (employeeCounts.Waived > 0) {
            colors.add(ContextCompat.getColor(this.getActivity(), R.color.waived_color));
        }
        if (notEnrolledCount > 0) {
            colors.add(ContextCompat.getColor(this.getActivity(), R.color.not_enrolled_color));
        }
        if (employeeCounts.Terminated > 0) {
            colors.add(ContextCompat.getColor(this.getActivity(), R.color.terminated_color));
        }
        dataSet.setColors(colors);
        data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();
        Log.d(TAG, "width: " + pieChart.getWidth());
        Log.d(TAG, "height: " + pieChart.getHeight());
    }
}