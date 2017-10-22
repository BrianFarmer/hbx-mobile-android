package org.dchbx.coveragehq.financialeligibility;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import org.dchbx.coveragehq.BaseActivity;
import org.dchbx.coveragehq.Events;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.models.fe.PersonForCoverage;
import org.dchbx.coveragehq.models.fe.UqhpDetermination;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.dchbx.coveragehq.util.OnSwipeTouchListener;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

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
public abstract class BaseResultsActivity extends BaseActivity {
    private UqhpDetermination uqhpDetermination;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(getLayoutId());
        messages.getUqhpDetermination();
    }

    abstract protected int getLayoutId();
    abstract protected void populate();
    abstract protected OnSwipeTouchListener getSwipeListener();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetUqhpDeterminationResponse getUqhpDeterminationResponse) throws Exception {
        uqhpDetermination = getUqhpDeterminationResponse.getUqhpDetermination();
        populate();
    }

    protected void populateResults(ArrayList<PersonForCoverage> people, ArrayList<PersonForCoverage> others, int listId, int otherButtonId, int myButtonId, final StateManager.AppEvents moveToOtherEvent) {
        PersonForCoverageAdapter personForCoverageAdapter = new PersonForCoverageAdapter(this, people);
        ListView ineligibleList = (ListView) findViewById(listId);
        ineligibleList.setAdapter(personForCoverageAdapter);
        ImageButton otherButton = (ImageButton) findViewById(otherButtonId);
        ImageButton myButton = (ImageButton) findViewById(myButtonId);
        if (others.size() == 0) {
            configToolbarWithoutBackButton();
            otherButton.setVisibility(View.GONE);
            myButton.setVisibility(View.GONE);
        } else {
            configToolbar();
            otherButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    messages.appEvent(moveToOtherEvent);
                }
            });
            findViewById(getLayoutId()).setOnTouchListener(getSwipeListener());
        }
    }

    protected UqhpDetermination getUqhpDetermination() {
        return uqhpDetermination;
    }
}
