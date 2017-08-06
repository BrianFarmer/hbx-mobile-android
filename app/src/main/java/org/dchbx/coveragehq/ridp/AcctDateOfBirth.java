package org.dchbx.coveragehq.ridp;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.widget.ImageButton;

import org.dchbx.coveragehq.AcctActivity;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.databinding.AcctDateOfBirthBinding;
import org.dchbx.coveragehq.models.account.Account;
import org.dchbx.coveragehq.models.ridp.Questions;

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
public class AcctDateOfBirth extends AcctActivity {
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(AcctDateOfBirth.class);
    private static String TAG = "AcctDateOfBirth";

    private Questions ridpQuestions;
    private ImageButton continueButton;
    private AcctDateOfBirthBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.acct_date_of_birth);
        configToolbar();
    }

    @Override
    protected void populate(final Account account) {
        binding.setAccount(account);
        binding.setActivity(this);
    }

    public void onClick(Account account){
        getMessages().accountButtonClicked(StateManager.AppEvents.Continue, account, null);
    }
}
