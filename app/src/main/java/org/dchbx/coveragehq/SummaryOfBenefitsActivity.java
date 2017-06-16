package org.dchbx.coveragehq;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.dchbx.coveragehq.models.roster.Enrollment;
import org.dchbx.coveragehq.models.roster.Health;
import org.dchbx.coveragehq.models.roster.RosterEntry;
import org.dchbx.coveragehq.models.services.Service;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

import java.util.List;

/**
 * Created by plast on 4/13/2017.
 */

public class SummaryOfBenefitsActivity extends BrokerActivity {
    static private String TAG = "SummaryAndBenefitsAct";
    private WalletSummaryAdapter summaryAdapter;
    private ListView summaryList;
    private LocalDate currentDate;
    private RosterEntry insured;
    private Enrollment currentEnrollment;
    private List<Service> servicesList;
    private boolean showHealth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "in SummaryAndBenefitsActivity.onCreate");
        Intent intent = getIntent();
        currentDate = LocalDate.parse(intent.getStringExtra(Intents.ENROLLMENT_DATE_ID));
        showHealth = intent.getBooleanExtra(Intents.SHOW_HEALTH_ID, true);


        setContentView(R.layout.summary_and_benefits_activity);
        summaryList = (ListView) findViewById(R.id.summaryList);
        getMessages().getInsuredAndServices(currentDate);
        configToolbar();
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
        insured = getInsuredAndServicesResult.getInsured();
        servicesList = getInsuredAndServicesResult.getServicesList();
        populate();
    }

    private void populate(){
        Log.d(TAG, "In SummaryOfBenefitsActivity.populate()");
        if (insured.enrollments == null
            || insured.enrollments.size() == 0) {
            return;
        }

        currentEnrollment = BrokerUtilities.getEnrollment(insured, currentDate);
        Health plan = showHealth ? currentEnrollment.health : currentEnrollment.dental;
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
