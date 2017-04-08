package org.dchbx.coveragehq;

import android.os.Bundle;

import org.dchbx.coveragehq.models.roster.RosterEntry;

/**
 * Created by plast on 4/6/2017.
 */

public class PlanResourcesActivity extends InsuredActivity {
    private RosterEntry employee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.plan_resources);
        getMessages().getEmployee(employeeId, employerId);
    }

    @Override
    protected void populate() {
    }
}
