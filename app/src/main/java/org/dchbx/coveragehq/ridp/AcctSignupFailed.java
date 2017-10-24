package org.dchbx.coveragehq.ridp;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.dchbx.coveragehq.BaseActivity;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.databinding.AcctSignupFailedBinding;
import org.dchbx.coveragehq.databinding.AcctSystemFoundYouAcedsBinding;
import org.dchbx.coveragehq.models.account.Account;
import org.dchbx.coveragehq.models.ridp.VerifyIdentityResponse;
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
public class AcctSignupFailed extends ValidatedActivityBase {
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(AcctSignupFailed.class);
    //private ImageButton continueButton;

    private AcctSignupFailedBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.acct_signup_failed);
        Intent intent = getIntent();
        Account account = RidpService.getAccountFromIntent(intent);
        binding.setAccount(account);
        binding.setActivity(this);
        String error = intent.getExtras().getString("error_msg");
        if (error != null) {
            TextView problemText = (TextView) findViewById(R.id.problemText);
            problemText.setText(error);
        }
        configToolbar();
    }

    public void comeBackLaterClicked(){
        getMessages().appEvent(StateManager.AppEvents.Close);
    }

}
