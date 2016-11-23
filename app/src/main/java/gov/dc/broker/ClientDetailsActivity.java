package gov.dc.broker;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import gov.dc.broker.models.brokerclient.BrokerClientDetails;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ClientDetailsActivity extends AppCompatActivity {
    private static final String TAG = "ClientDetailsActivity";

    private EventBus eventBus;
    private Toolbar toolbar;
    private int clientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ClientDetailsActivity _this = this;
        Intent intent = getIntent();
        clientId = intent.getIntExtra(Intents.BROKER_CLIENT_ID, -1);

        if (clientId == -1){
            Log.d(TAG, "onCreate: no client id found");
            return;
        }
        eventBus = EventBus.getDefault();
        eventBus.register(this);
        eventBus.post(new Events.GetEmployer(clientId));

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
        return Utilities.DateAsString(new DateTime());
    }

    private void showRenewalClients(BrokerClient brokerClient, BrokerClientDetails brokerClientDetails) {
        setContentView(R.layout.client_details_other);

        // Initializing Toolbar and setting it as the actionbar
        configToolbar();
        configButtons(brokerClient);
        TextView status = (TextView)findViewById(R.id.textViewCompanyInOpenEnrollment);
        status.setText(R.string.renewal_in_progress);
        fillCoverageInfo(brokerClient);
        fillOpenScreen(brokerClient, brokerClientDetails);
    }
    private void showOtherClients(BrokerClient brokerClient, BrokerClientDetails brokerClientDetails) {
        setContentView(R.layout.client_details_other);

        // Initializing Toolbar and setting it as the actionbar
        configToolbar();
        configButtons(brokerClient);
        fillCoverageInfo(brokerClient);
        fillOpenScreen(brokerClient, brokerClientDetails);
    }

    private void showNotAlerted(BrokerClient brokerClient, BrokerClientDetails brokerClientDetails) {
        setContentView(R.layout.client_details_minimum_met);

        // Initializing Toolbar and setting it as the actionbar
        configToolbar();

        configButtons(brokerClient);
        fillCoverageHeading(brokerClient);
        fillMinimumParticipation(brokerClient);
        fillOpenEnrollmentFields(brokerClient);
        fillOpenScreen(brokerClient, brokerClientDetails);
    }

    private void configButtons(final BrokerClient brokerClient){
        ContactInfo curContactInfo = brokerClient.contactInfo.get(0);

        ImageButton emailButton = (ImageButton)findViewById(R.id.imageButtonEmail);
        if (brokerClient.anyEmailAddresses()) {
            emailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactDialog contactDialog = ContactDialog.build(brokerClient, ContactListAdapter.ListType.Email);
                    contactDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
                    contactDialog.show(getSupportFragmentManager(), "ContactDialog");
                }
            });
        } else {
            emailButton.setEnabled(false);
        }
        ImageButton chatButton = (ImageButton) findViewById(R.id.imageButtonChat);
        if (brokerClient.anyMobileNumbers()) {
            chatButton.setEnabled(true);
            chatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactDialog contactDialog = ContactDialog.build(brokerClient, ContactListAdapter.ListType.Chat);
                    contactDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
                    contactDialog.show(getSupportFragmentManager(), "ContactDialog");
                }
            });
        } else {
            chatButton.setEnabled(false);
        }

        ImageButton phoneButton = (ImageButton) findViewById(R.id.imageButtonPhone);
        if (brokerClient.anyPhoneNumbers()) {
            phoneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactDialog contactDialog = ContactDialog.build(brokerClient, ContactListAdapter.ListType.Phone);
                    contactDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
                    contactDialog.show(getSupportFragmentManager(), "ContactDialog");
                }
            });
        } else {
            phoneButton.setEnabled(false);
        }
        ImageButton locationButton = (ImageButton) findViewById(R.id.imageButtonLocation);
        if (brokerClient.anyAddresses()) {
            locationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactDialog contactDialog = ContactDialog.build(brokerClient, ContactListAdapter.ListType.Directions);
                    contactDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
                    contactDialog.show(getSupportFragmentManager(), "ContactDialog");
                }
            });
        } else {
            locationButton.setEnabled(false);
        }
    }

    private void showAlerted(BrokerClient brokerClient, BrokerClientDetails brokerClientDetails) {
        setContentView(R.layout.client_details_minimum_not_met);
        configToolbar();


        configButtons(brokerClient);
        fillCoverageHeading(brokerClient);
        fillMinimumParticipation(brokerClient);
        fillOpenEnrollmentFields(brokerClient);
        fillOpenScreen(brokerClient, brokerClientDetails);
    }

    private void configToolbar() {
        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.app_header);
        toolbar.setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void fillOpenScreen(BrokerClient brokerClient, BrokerClientDetails brokerClientDetails) {
        TextView companyName = (TextView) findViewById(R.id.textViewCompanyName);
        companyName.setText(brokerClient.employerName);

        TextView enrolled = (TextView) findViewById(R.id.textViewEnrolled);
        enrolled.setText(Integer.toString(brokerClient.employeesEnrolled));
        TextView waived = (TextView) findViewById(R.id.textViewWaived);
        waived.setText(Integer.toString(brokerClient.employessWaived));
        TextView notCompleted = (TextView) findViewById(R.id.textViewNotCompleted);
        notCompleted.setText(Integer.toString(brokerClient.getEmployessNotCompleted()));
        TextView totalEmployees = (TextView) findViewById(R.id.textViewTotalEmployees);
        totalEmployees.setText(Integer.toString(brokerClient.employeesTotal));
        TextView monthlyEstimatedCost = (TextView) findViewById(R.id.textViewMonthlyEstimatedCost);
        Resources resources = getResources();
        monthlyEstimatedCost.setText(resources.getString(R.string.estimated_cost_label) + nextMonthString());
        /*
        if (brokerClientDetails != null) {
            TextView employerContribution = (TextView) findViewById(R.id.textViewEmployerContribution);
            String employerContributionStriing = NumberFormat.getCurrencyInstance().format(brokerClientDetails.);
            employerContribution.setText(employerContributionStriing);
            TextView employeeContribution = (TextView) findViewById(R.id.textViewEmployeeContribution);
            employeeContribution.setText(NumberFormat.getCurrencyInstance().format(brokerClientDetails.employeeContribution));
            TextView totalCost = (TextView) findViewById(R.id.textViewTotal);
            totalCost.setText(NumberFormat.getCurrencyInstance().format(brokerClientDetails.totalPremium));
        } else {*/
        TextView employerContribution = (TextView) findViewById(R.id.textViewEmployerContribution);
        employerContribution.setText("NA");
        TextView employeeContribution = (TextView) findViewById(R.id.textViewEmployeeContribution);
        employeeContribution.setText("NA");
        TextView totalCost = (TextView) findViewById(R.id.textViewTotal);
        totalCost.setText("NA");
    }

    private void fillCoverageInfo(BrokerClient brokerClient){
        fillCoverageHeading(brokerClient);
        TextView textViewRenewalAvailable = (TextView)findViewById(R.id.textViewRenewalAvailable);
        if (brokerClient.renewalApplicationAvailable != null) {
            textViewRenewalAvailable.setText(Utilities.DateAsString(brokerClient.renewalApplicationAvailable));
        }
        TextView textViewNextCoverageYearBegins = (TextView)findViewById(R.id.textViewNextCoverageYearBegins);
        if (brokerClient.planYearBegins != null) {
            textViewNextCoverageYearBegins.setText(Utilities.DateAsString(brokerClient.planYearBegins));
        }
        TextView textViewOpenEnrollmentEnds= (TextView)findViewById(R.id.textViewOpenEnrollmentEnds);
        if (brokerClient.openEnrollmentEnds != null) {
            textViewOpenEnrollmentEnds.setText(Utilities.DateAsString(brokerClient.openEnrollmentEnds));
        }
    }

    private void fillCoverageHeading(BrokerClient brokerClient) {
        TextView textViewCoverageInfoSubheadingLabel = (TextView)findViewById(R.id.textViewCoverageInfoSubheadingLabel);
        Resources resources = getResources();
        if (brokerClient.planYearBegins != null) {
            DateTime startDate = brokerClient.planYearBegins;
            DateTime endDate = Utilities.calculateOneYearOut(startDate);
            String formatString = resources.getString(R.string.coverage_year);
            String coverageDates = String.format(formatString, Utilities.DateAsString(startDate), Utilities.DateAsString(endDate));
            textViewCoverageInfoSubheadingLabel.setText(coverageDates);
        } else {
            textViewCoverageInfoSubheadingLabel.setText(R.string.NoPlanYearBeginDateMessagge);
        }
    }

    private void fillMinimumParticipation(BrokerClient brokerClient) {
        TextView mustParticipate = (TextView) findViewById(R.id.textViewMustParticipate);
        mustParticipate.setText(Integer.toString(brokerClient.minimumParticipationRequired));
    }

    private void fillOpenEnrollmentFields(BrokerClient brokerClient) {
        TextView enrollmentBegins = (TextView) findViewById(R.id.textViewOpenEnrollmentBegins);
        enrollmentBegins.setText(Utilities.DateAsString(brokerClient.openEnrollmentBegins));
        TextView enrollmentEnds = (TextView) findViewById(R.id.textViewOpenEnrollmentEnds);
        enrollmentEnds.setText(Utilities.DateAsString(brokerClient.openEnrollmentEnds));
        TextView daysLeft = (TextView) findViewById(R.id.textViewDaysLeft);
        daysLeft.setText(Long.toString(Utilities.dateDifferenceDays(brokerClient.openEnrollmentBegins, brokerClient.openEnrollmentEnds)));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.BrokerClient  brokerClientEvent) {
        BrokerClient brokerClient = brokerClientEvent.getBrokerClient();

        if (brokerClient.isInOpenEnrollment(new DateTime())) {
            if (brokerClient.isAlerted()){
                showAlerted(brokerClient, brokerClientEvent.getBrokerClientDetails());
            } else {
                showNotAlerted(brokerClient, brokerClientEvent.getBrokerClientDetails());
            }
        } else {
            if (brokerClient.renewalInProgress){
                showRenewalClients(brokerClient, brokerClientEvent.getBrokerClientDetails());
            } else {
                showOtherClients(brokerClient, brokerClientEvent.getBrokerClientDetails());
            }
        }
    }
}
