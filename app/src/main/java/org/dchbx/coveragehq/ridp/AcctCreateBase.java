package org.dchbx.coveragehq.ridp;

import android.content.Intent;
import android.os.Bundle;

import org.dchbx.coveragehq.BaseActivity;
import org.dchbx.coveragehq.models.account.Account;
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

public abstract class AcctCreateBase extends BaseActivity {
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(AcctCreate.class);
    private static String TAG = "AcctCreate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView();
        configToolbar();

        Intent intent = getIntent();
        int newStateInt = intent.getExtras().getInt("NewState");
        Account account = RidpService.getAccountFromIntent(intent);
        StateManager.AppStates state = StateManager.AppStates.values()[newStateInt];
        bind(account);
    }

    protected abstract void setContentView();

    protected abstract void bind(Account account);

    protected abstract int getLayoutId();

    public void onClick(Account account){
        getMessages().appEvent(StateManager.AppEvents.Continue, EventParameters.build().add("Account", account));
    }

    public void onSkip(){
        getMessages().buttonClicked(StateManager.AppEvents.Skip);
    }
}
