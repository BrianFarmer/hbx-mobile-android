package gov.dc.broker;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ClientDetailsActivity extends AppCompatActivity {
    private static final String TAG = "ClientDetailsActivity";
    public static final String BROKER_CLIENT_ID = "BrokerClientId";

    private EventBus eventBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ClientDetailsActivity _this = this;

        Intent intent = getIntent();
        int clientId = intent.getIntExtra(BROKER_CLIENT_ID, -1);
        if (clientId == -1){
            Log.d(TAG, "onCreate: no client id found");
            return;
        }
        eventBus = EventBus.getDefault();
        eventBus.register(this);
        eventBus.post(new Events.GetEmployer(clientId));

    }

    private CharSequence DateAsString(Date date){
        return DateFormat.format("MMM dd, yyyy", date);
    }

    private CharSequence DateAsMonthYear(Date date){
        return DateFormat.format("MMM dd, yyyy", date);
    }

    private long dateDifference(Date start, Date end) {
        Calendar cal = Calendar.getInstance();
        long milliSecondDiff = end.getTime() - start.getTime();
        return TimeUnit.DAYS.convert(milliSecondDiff, TimeUnit.MILLISECONDS);
    }

    private CharSequence nextMonthString(){
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 1);
        return DateAsString(calendar.getTime());
    }

    private void showOtherClients(BrokerClient brokerClient, BrokerClientDetails brokerClientDetails) {
        setContentView(R.layout.client_details_other);
        fillCoverateInfo(brokerClient);
        fillOpenScreeen(brokerClient, brokerClientDetails);
    }

    private void showNotAlerted(BrokerClient brokerClient, BrokerClientDetails brokerClientDetails) {
        setContentView(R.layout.client_details_minimum_met);
        fillMinimumParticipation(brokerClient);
        fillOpenEnrollmentFields(brokerClient);
        fillOpenScreeen(brokerClient, brokerClientDetails);
    }

    private void showAlerted(BrokerClient brokerClient, BrokerClientDetails brokerClientDetails) {
        setContentView(R.layout.client_details_minimum_not_met);
        fillMinimumParticipation(brokerClient);
        fillOpenEnrollmentFields(brokerClient);
        fillOpenScreeen(brokerClient, brokerClientDetails);
    }

    private void fillOpenScreeen(BrokerClient brokerClient, BrokerClientDetails brokerClientDetails) {
        TextView companyName = (TextView) findViewById(R.id.textViewCompanyName);
        companyName.setText(brokerClient.employerName);

        TextView enrolled = (TextView) findViewById(R.id.textViewEnrolled);
        enrolled.setText(Integer.toString(brokerClient.employeesEnrolled));
        TextView waived = (TextView) findViewById(R.id.textViewWaived);
        waived.setText(Integer.toString(brokerClient.employessWaived));
        TextView notCompleted = (TextView) findViewById(R.id.textViewNotCompleted);
        notCompleted.setText(Integer.toString(brokerClient.getEmployessNeeded()));
        TextView totalEmployees = (TextView) findViewById(R.id.textViewTotalEmployees);
        totalEmployees.setText(Integer.toString(brokerClient.employeesTotal));
        TextView monthlyEstimatedCost = (TextView) findViewById(R.id.textViewMonthlyEstimatedCost);
        Resources resources = getResources();
        monthlyEstimatedCost.setText(resources.getString(R.string.estimated_cost_label) + nextMonthString());
        if (brokerClientDetails != null) {
            TextView employerContribution = (TextView) findViewById(R.id.textViewEmployerContribution);
            String employerContributionStriing = NumberFormat.getCurrencyInstance().format(brokerClientDetails.employerContribution);
            employerContribution.setText(employerContributionStriing);
            TextView employeeContribution = (TextView) findViewById(R.id.textViewEmployeeContribution);
            employeeContribution.setText(NumberFormat.getCurrencyInstance().format(brokerClientDetails.employeeContribution));
            TextView totalCost = (TextView) findViewById(R.id.textViewTotal);
            totalCost.setText(NumberFormat.getCurrencyInstance().format(brokerClientDetails.totalPremium));
        } else {
            TextView employerContribution = (TextView) findViewById(R.id.textViewEmployerContribution);
            employerContribution.setText("NA");
            TextView employeeContribution = (TextView) findViewById(R.id.textViewEmployeeContribution);
            employeeContribution.setText("NA");
            TextView totalCost = (TextView) findViewById(R.id.textViewTotal);
            totalCost.setText("NA");
        }
    }

    private void fillCoverateInfo(BrokerClient brokerClient){
        TextView textViewCoverageInfoSubheadingLabel = (TextView)findViewById(R.id.textViewCoverageInfoSubheadingLabel);
        Resources resources = getResources();
        Date startDate = brokerClient.planYearBegins;
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.YEAR, 1);
        cal.add(Calendar.HOUR, -24);
        Date endDate = cal.getTime();
        String formatString = resources.getString(R.string.coverage_year);
        String coverageDates = String.format(formatString, DateAsString(startDate), DateAsString(endDate));
        textViewCoverageInfoSubheadingLabel.setText(coverageDates);
        TextView textViewRenewalAvailable = (TextView)findViewById(R.id.textViewRenewalAvailable);
        textViewRenewalAvailable.setText(DateAsString(brokerClient.renewalApplicationAvailable));
        TextView textViewNextCoverageYearBegins = (TextView)findViewById(R.id.textViewNextCoverageYearBegins);
        textViewNextCoverageYearBegins.setText(DateAsString(brokerClient.planYearBegins));
        TextView textViewOpenEnrollmentEnds= (TextView)findViewById(R.id.textViewOpenEnrollmentEnds);
        textViewOpenEnrollmentEnds.setText(DateAsString(brokerClient.openEnrollmentEnds));
    }

    private void fillMinimumParticipation(BrokerClient brokerClient) {
        TextView mustParticipate = (TextView) findViewById(R.id.textViewMustParticipate);
        mustParticipate.setText(Integer.toString(brokerClient.minimumParticipationRequired));
    }

    private void fillOpenEnrollmentFields(BrokerClient brokerClient) {
        TextView enrollmentBegins = (TextView) findViewById(R.id.textViewOpenEnrollmentBegins);
        enrollmentBegins.setText(DateAsString(brokerClient.openEnrollmentBegins));
        TextView enrollmentEnds = (TextView) findViewById(R.id.textViewOpenEnrollmentEnds);
        enrollmentEnds.setText(DateAsString(brokerClient.openEnrollmentEnds));
        TextView daysLeft = (TextView) findViewById(R.id.textViewDaysLeft);
        daysLeft.setText(Long.toString(dateDifference(brokerClient.openEnrollmentBegins, brokerClient.openEnrollmentEnds)));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.BrokerClient  brokerClientEvent) {
        BrokerClient brokerClient = brokerClientEvent.getBrokerClient();

        if (brokerClient.isInOpenEnrollment(new Date())) {
            if (brokerClient.isAlerted()){
                showAlerted(brokerClient, brokerClientEvent.getBrokerClientDetails());
            } else {
                showNotAlerted(brokerClient, brokerClientEvent.getBrokerClientDetails());
            }
        } else {
            showOtherClients(brokerClient, brokerClientEvent.getBrokerClientDetails());
        }
    }
}
