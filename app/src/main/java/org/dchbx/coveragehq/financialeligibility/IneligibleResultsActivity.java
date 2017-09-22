package org.dchbx.coveragehq.financialeligibility;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import org.dchbx.coveragehq.BaseActivity;
import org.dchbx.coveragehq.Events;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.models.fe.UqhpDetermination;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
public class IneligibleResultsActivity extends BaseActivity {
    private static String TAG = "IneligibleResultsActivity";
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(IneligibleResultsActivity.class);
    private UqhpDetermination uqhpDetermination;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.ineligible_applicants);
        messages.getUqhpDetermination();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetUqhpDeterminationResponse getUqhpDeterminationResponse) throws Exception {
        uqhpDetermination = getUqhpDeterminationResponse.getUqhpDetermination();
        populate();
    }

    private void populate() {
        PersonForCoverageAdapter personForCoverageAdapter = new PersonForCoverageAdapter(this, uqhpDetermination.ineligibleForQhp);
        ListView ineligibleList = (ListView) findViewById(R.id.ineligibleList);
        ineligibleList.setAdapter(personForCoverageAdapter);
        ImageButton rightButton = (ImageButton) findViewById(R.id.rightButton);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.appEvent(StateManager.AppEvents.ShowEligible);
            }
        });
    }
}
