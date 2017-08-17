package org.dchbx.coveragehq.financialeligibility;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.dchbx.coveragehq.BrokerActivity;
import org.dchbx.coveragehq.Events;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.models.fe.Field;
import org.dchbx.coveragehq.models.fe.Schema;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

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
public class ApplicationQuestionsActivity extends BrokerActivity {
    private static String TAG = "ApplicationQuestions";
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(ApplicationQuestionsActivity.class);

    private Schema schema;
    private String eapersonid;
    private HashMap<String, Object> person;
    private LinearLayout linearLayout;

    public ApplicationQuestionsActivity(){
        schema = null;
        person = null;
    }

    public void onCreate(Bundle bundle){
        super.onCreate(bundle);

        setContentView(R.layout.financial_eligibility_questions);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        showProgress();
        getMessages().getFinancialEligibilityJson();
        Intent intent = getIntent();
        eapersonid = intent.getStringExtra("eapersonid");
        getMessages().getFinancialApplicationPerson(eapersonid);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetFinancialEligibilityJsonResponse getFinancialEligibilityJsonResponse){
        schema = getFinancialEligibilityJsonResponse.getSchema();
        populate();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetApplicationPersonResponse getApplicationPersonResponse){
        person = getApplicationPersonResponse.getPerson();
        populate();
    }

    private void populate() {
        if (schema == null
            || person == null){
            return;
        }
        hideProgress();

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Field field : schema.Person) {
            if (field.type.toLowerCase().compareTo("text") == 0){
                Log.d(TAG, "found text field");
                fillTextField(inflater, field);
            } else {

            }
        }

    }

    private void fillTextField(LayoutInflater inflater, Field field) {
        View view = inflater.inflate(R.layout.app_text_field, linearLayout, false);
        linearLayout.addView(view);
        TextView label = (TextView) view.findViewById(R.id.label);
        label.setText(field.label);
        EditText value = (EditText) view.findViewById(R.id.value);
        if (person.containsKey(field.field)){
            String str = (String)person.get(field.field);
            value.setText(str);
        } else {
            if (field.defaultValue != null){
                value.setText((String)field.defaultValue);
            }
        }
    }

}
