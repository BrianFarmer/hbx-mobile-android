package org.dchbx.coveragehq;

import android.os.Bundle;

/**
 * Created by plast on 4/13/2017.
 */

public class SummaryAndBenefitsActivity extends InsuredActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.summary_and_benefits_activity);
        getMessages().getInsured();
    }


    @Override
    protected void populate() {

    }
}
