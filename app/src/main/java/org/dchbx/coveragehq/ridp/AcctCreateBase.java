package org.dchbx.coveragehq.ridp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
        Account account = RidpService.getAccountFromIntent(intent);
        bind(account);
    }

    protected abstract void setContentView();

    protected abstract void bind(Account account);

    protected abstract int getLayoutId();


    private String getTextFromField(int fieldId){
        TextView field = (TextView)findViewById(fieldId);
        return field.getText().toString();
    }

    protected boolean validateRequiredTextField(int fieldId, String fieldName, StringBuffer issues) {
        if (getTextFromField(fieldId).length() == 0) {
            issues.append(fieldName + " is a required field");
            return false;
        }
        return true;
    }

    protected boolean validateRequiredTextFieldsMatch(int fieldId, String fieldName, int fieldId2,
                                                      String fieldName2, StringBuffer issues) {
        if (!getTextFromField(fieldId).equals(getTextFromField(fieldId2))) {
            issues.append(fieldName + " and " + fieldName2 + " must match.");
            return false;
        }
        return true;
    }

    protected boolean validate(StringBuffer issues) {
      return false;
    }

    public void onClick(Account account){
        StringBuffer issues = new StringBuffer();
        if (!validate(issues)) {
            simpleAlert("Please review your entries",  issues.toString());
        } else {
            getMessages().appEvent(StateManager.AppEvents.Continue, EventParameters.build().add("Account", account));
        }
    }
}
