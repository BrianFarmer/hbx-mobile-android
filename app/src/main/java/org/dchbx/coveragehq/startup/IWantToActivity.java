package org.dchbx.coveragehq.startup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.dchbx.coveragehq.BaseActivity;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.Utilities;
import org.dchbx.coveragehq.models.startup.EffectiveDate;
import org.dchbx.coveragehq.statemachine.StateManager;

/*
    This file is part of DC.

    DC Health Link SmallBiz is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DC Health Link SmallBiz is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DC Health Link SmallBiz.  If not, see <http://www.gnu.org/licenses/>.
    This statement should go near the beginning of every source file, close to the copyright notices. When using the Lesser GPL, insert the word “Lesser” before “General” in all three places. When using the GNU AGPL, insert the word “Affero” before “General” in all three places.
*/
public class IWantToActivity extends BaseActivity {
    private static String TAG = "IWantToActivity";
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(IWantToActivity.class);

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Intent intent = getIntent();
        EffectiveDate effectiveDate = StartUpService.getEffectiveDate(intent);
        String effectiveDateStr;
        try {
            effectiveDateStr = Utilities.DateAsMonthStringDayYear(effectiveDate.effectiveDate);
        } catch (Throwable t){
            String jsonString = intent.getStringExtra("EffectiveDate");
            Log.d(TAG, "json: " + jsonString);
            Log.d(TAG, "throowable: " + t.getMessage());
            throw t;
        }

        setContentView(R.layout.i_want_to);
        configToolbar();

        TextView nextYearCoverage = (TextView) findViewById(R.id.nextYearCoverage);
        nextYearCoverage.setText(getString(R.string.coverage_after, effectiveDateStr));
        TextView earlyCoverage = (TextView) findViewById(R.id.earlyCoverage);
        earlyCoverage.setText(getString(R.string.coverage_before, effectiveDateStr));
        ((Button)findViewById(R.id.dentalCoverage)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.appEvent(StateManager.AppEvents.GetDentalCoverage);
            }
        });
        ((Button) nextYearCoverage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.appEvent(StateManager.AppEvents.GetCoverageNextYear);
            }
        });
        ((Button) earlyCoverage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.appEvent(StateManager.AppEvents.GetCoverageThisYear);
            }
        });
    }
}
