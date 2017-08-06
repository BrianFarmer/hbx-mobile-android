package org.dchbx.coveragehq;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.dchbx.coveragehq.models.roster.Enrollment;
import org.dchbx.coveragehq.models.roster.Health;
import org.dchbx.coveragehq.models.roster.RosterEntry;
import org.dchbx.coveragehq.models.services.Service;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

import java.util.List;

/**
 * Created by plast on 4/13/2017.
 */

public class SummaryOfBenefitsActivityHacked extends AppCompatActivity {
    static private String TAG = "SummaryAndBenefitsAct";
    private WalletSummaryAdapter summaryAdapter;
    private ListView summaryList;
    private LocalDate currentDate;
    private RosterEntry insured;
    private Enrollment currentEnrollment;
    private List<Service> servicesList;
    private boolean showHealth;
    private boolean shuttingDown = false;


    protected Messages messages = null;

    public Messages getMessages() {
        return messages;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (messages == null) {
            Log.d(TAG, "in BaseActivity.onCreate");
            messages = BrokerApplication.getBrokerApplication().getMessages(this);
        }

        try {
            Log.d(TAG, "in SummaryAndBenefitsActivity.onCreate");
            Intent intent = getIntent();
            currentDate = LocalDate.parse(intent.getStringExtra(Intents.ENROLLMENT_DATE_ID));
            showHealth = intent.getBooleanExtra(Intents.SHOW_HEALTH_ID, true);


            setContentView(R.layout.summary_and_benefits_activity);
            summaryList = (ListView) findViewById(R.id.summaryList);
            Log.d(TAG, "Sending: getInsuredAndServices");
            getMessages().getInsuredAndServices(currentDate);
            Log.d(TAG, "SummaryAndBenefitsAct.onCreate finished.");
            //configToolbar();
        } catch (Throwable t){
            Log.d(TAG, "Caught exception in SummaryOfBenefits ctor: " + t.getMessage());
        }
    }

    @Override
    public void onPause(){
        Log.d(TAG, "In SummaryOfBenefitsActivity.onPause");
        super.onPause();
        if (messages != null) {
            Log.d(TAG, "releasing messages in BaseActivity.onPause");
//            messages.release();
            messages = null;
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (messages != null) {
            Log.d(TAG, "releasing messages in BaseActivity.onDestroy");
            messages.release();
            messages = null;
        }
    }

    @Override
    public void onStop(){
        Log.d(TAG, "In SummaryOfBenefitsActivity.onStop()");
        super.onStop();
    }

    @Override
    protected void onResume(){
        Log.d(TAG, "In BrokerActivity.onResume()");
        super.onResume();
        getMessages().testTimeOut();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.TestTimeoutResult testTimeoutResult) {
        if (testTimeoutResult.timedOut && !shuttingDown){
            shuttingDown = true;
            Intents.restartApp(this);
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.SessionAboutToTimeout sessionAboutToTimeout){
        SessionTimeoutDialog sessionTimeoutDialog = SessionTimeoutDialog.build();
        sessionTimeoutDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
        sessionTimeoutDialog.show(this.getSupportFragmentManager(), "SecurityQuestionDialog");

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.StateAction stateAction) {
        StateManager.UiActivity.Info uiActivityType = StateManager.UiActivity.getUiActivityType(stateAction.getUiActivityId());
        Intents.launchActivity(uiActivityType.cls, this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Finish eventFinish){
        finish();
    }

    private void configToolbar() {
        // Initializing Toolbar and setting it as the actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.app_header_vector);
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

    private void populateCoverageYearDependencies(Enrollment enrollment, Resources resources) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    void doThis(Events.GetInsuredAndServicesResult getInsuredAndServicesResult){
        Log.d(TAG, "In SummaryOfBenefitsActivity.GetInsuredAndServicesResult");
        try {
            insured = getInsuredAndServicesResult.getInsured();
            servicesList = getInsuredAndServicesResult.getServicesList();
        } catch (Throwable t){
            Log.e(TAG, "Caught exception in SummaryOfBenefitsActivity.GetInsuredAndServicesResult: " + t.getMessage());
        }
        try {
            populate();
        } catch (Throwable t){
            Log.e(TAG, "Caught exception in population: " + t.getMessage());
        }
    }

    private void populate(){
        Log.d(TAG, "In c.populate()");
        if (insured == null){
            Log.d(TAG, "insured is null!!!");
        }
        if (insured.enrollments == null
            || insured.enrollments.size() == 0) {
            if (insured.enrollments == null){
                Log.d(TAG, "enrollments is null");
            } else {
                Log.d(TAG, "enrollments is empty");
            }
            Toast toast = Toast.makeText(this, insured.enrollments == null ? "Enrollments were null, is this a problem?" : "No enrollments were returned, is this a problem?", Toast.LENGTH_LONG);
            toast.show();;
            return;
        }


        currentEnrollment = BrokerUtilities.getEnrollment(insured, currentDate);
        if (currentEnrollment == null){
            Log.d(TAG, "no enrollment found for currentDate");
        }
        Health plan = showHealth ? currentEnrollment.health : currentEnrollment.dental;
        if (plan == null){
            Log.d(TAG, "plan is null");
        }

        if (servicesList == null){
            Log.d(TAG, "services is null");
        } else {
            if (servicesList.size() == 0){
                Log.d(TAG, "serviceslist is empty");
            }
        }

        this.summaryAdapter = new WalletSummaryAdapter(this, servicesList, plan);
        summaryList.setAdapter(summaryAdapter);

        ImageView carrierLogo = (ImageView) findViewById(R.id.carrierLogo);

        //Glide
        //    .with(this)
        //    .load(plan..links.carrierLogo)
        // .into(carrierLogo);

        TextView planName = (TextView) findViewById(R.id.planName);
        planName.setText(plan.planName);

        Log.d(TAG, "populate complete");
    }
}
