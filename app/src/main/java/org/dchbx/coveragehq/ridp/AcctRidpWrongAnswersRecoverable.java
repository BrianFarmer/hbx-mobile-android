package org.dchbx.coveragehq.ridp;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import org.dchbx.coveragehq.BaseActivity;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.databinding.AcctRidpConnectionFailureBinding;
import org.dchbx.coveragehq.databinding.AcctRidpWrongAnswersRecoverableBinding;
import org.dchbx.coveragehq.statemachine.EventParameters;
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
public class AcctRidpWrongAnswersRecoverable extends BaseActivity {
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(AcctRidpWrongAnswersRecoverable.class);

    private AcctRidpWrongAnswersRecoverableBinding binding;
    private String transactionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        transactionId = intent.getExtras().getString("transactionId");

        binding = DataBindingUtil.setContentView(this, R.layout.acct_ridp_wrong_answers_recoverable);
        configToolbar();
        binding.setActivity(this);
    }

    public void callExperianClicked(){
        callPhoneNumber("1-866-578-5409");
    }

    public void continueClicked(){
        getMessages().appEvent(StateManager.AppEvents.RidpCheckOverride, EventParameters.build().add("transactionId", transactionId));
    }

    public void comeBackLaterClicked(){
        getMessages().appEvent(StateManager.AppEvents.Close);
    }

    public String getWrongAnswersMessage() {
        return String.format("You have not passed identity validation. To proceed please contact Experian at 1-866-578-5409, and provide them with reference number %s", transactionId);
    }

}
