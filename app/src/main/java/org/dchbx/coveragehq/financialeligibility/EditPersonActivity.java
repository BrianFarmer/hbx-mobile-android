package org.dchbx.coveragehq.financialeligibility;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.dchbx.coveragehq.Events;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.models.fe.Schema;
import org.dchbx.coveragehq.statemachine.EventParameters;
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
public class EditPersonActivity extends ApplicationQuestionsActivity {
    private static String TAG = "AddPersonActivity";
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(EditPersonActivity.class);
    private Schema schema;

    public void onCreate(Bundle bundle){
        super.onCreate(bundle);

        setContentView(R.layout.financial_eligibility_questions);
        Toolbar toolbar = configToolbar();
        Menu menu = toolbar.getMenu();

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        showProgress();
        getMessages().getUqhpSchema();
        Intent intent = getIntent();
        String stringExtra = intent.getStringExtra("FamilyMember");
        jsonObject = new Gson().fromJson(stringExtra, JsonObject.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.save:
                HashMap<String, Object> values = getValues();
                getMessages().appEvent(StateManager.AppEvents.UserSaved, EventParameters.build()
                        .add("Result", values)
                        .add("ResultCode", StateManager.ActivityResultCodes.Saved));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetFinancialEligibilityJsonResponse getFinancialEligibilityJsonResponse) throws Exception {
        schema = getFinancialEligibilityJsonResponse.getSchema();
        this.schemaFields = schema.Person;
        populate();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetApplicationPersonResponse getApplicationPersonResponse) throws Exception {
        jsonObject = getApplicationPersonResponse.getPerson();
        populate();
    }
}
