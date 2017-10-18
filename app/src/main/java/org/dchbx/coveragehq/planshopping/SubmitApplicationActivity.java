package org.dchbx.coveragehq.planshopping;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.models.planshopping.Plan;
import org.dchbx.coveragehq.ridp.ValidatedActivityBase;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.StateManager;

import java.util.List;

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
public class SubmitApplicationActivity extends ValidatedActivityBase {
    private static String TAG = "SubmitApplicationActivity";
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(SubmitApplicationActivity.class);
    private Plan plan;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Intent intent = getIntent();
        plan = PlanShoppingService.getPlanFromIntent(intent);

        setContentView(R.layout.submit_application);
        htmlifyTextControl(R.id.firstText);
        htmlifyTextControl(R.id.secondText);
        messages.getUqhpDetermination();
        configToolbar();
        final Button submitMyApplication = (Button) findViewById(R.id.submitMyApplication);
        submitMyApplication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox firstCheck = (CheckBox) findViewById(R.id.firstCheck);
                CheckBox secondCheck = (CheckBox) findViewById(R.id.secondCheck);
                if (!firstCheck.isChecked()){
                    simpleAlert("Submit Your Application", "");
                    return;
                }
                if (!secondCheck.isChecked()){

                }
                messages.appEvent(StateManager.AppEvents.SubmitMyApplicationClicked, EventParameters.build().add(PlanShoppingService.Plan, plan));
            }
        });
    }

    @Override
    protected boolean validate(List<String> issues) {
        //note the use of non-short-circuiting "&" instead of "&&" to collect all errors.
        return validateRequiredTextField(R.id.firstName, R.string.firstName, issues)
                & validateRequiredTextField(R.id.lastName, R.string.lastName, issues)
                & validateRequiredCheckBox(R.id.firstCheck, R.string.firstCheckBox, issues)
                & validateRequiredCheckBox(R.id.secondCheck, R.string.emailValidationError, issues);
    }
}
