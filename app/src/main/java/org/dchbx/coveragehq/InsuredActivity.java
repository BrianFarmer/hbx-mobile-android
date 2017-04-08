package org.dchbx.coveragehq;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.microsoft.azure.mobile.analytics.Analytics;

import org.dchbx.coveragehq.models.roster.RosterEntry;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by plast on 4/6/2017.
 */

public abstract class InsuredActivity extends BrokerActivity {
    private static final String TAG = "InsuredActivity";
    protected String employeeId;
    protected String employerId;
    protected LocalDate currentDate;
    protected RosterEntry employee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        employeeId = intent.getStringExtra(Intents.EMPLOYEE_ID);
        employerId = intent.getStringExtra(Intents.BROKER_CLIENT_ID);
        String coverageYearString = intent.getStringExtra(Intents.COVERAGE_YEAR);
        if (coverageYearString == null){
            currentDate = null;
        } else {
            currentDate = LocalDate.parse(intent.getStringExtra(Intents.COVERAGE_YEAR));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Employee  employeeEvent) throws Exception {
        this.employee = employeeEvent.getEmployee();

        Map<String,String> properties=new HashMap<String,String>();
        Analytics.trackEvent("Employee Details", properties);

        if (currentDate == null){
            currentDate = BrokerUtilities.getMostRecentPlanYear(employee);
        }

        try {
            populate();
        } catch (Exception e){
            Log.e(TAG, "exception populating activity", e);
        }
    }

    protected abstract void populate();
}
