package org.dchbx.coveragehq.financialeligibility;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.models.fe.Field;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.StateManager;

import java.lang.reflect.Type;
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
public class SectionActivity extends ApplicationQuestionsActivity {
    private static String TAG = "SectionActivity";
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(SectionActivity.class);

    public void onCreate(Bundle bundle){
        super.onCreate(bundle);

        setContentView(R.layout.financial_eligibility_questions);
        Toolbar toolbar = configToolbar();
        Menu menu = toolbar.getMenu();

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        showProgress();
        Gson gson = new Gson();
        Intent intent = getIntent();
        String schemaExtra = intent.getStringExtra("Schema");
        Type schemaType = new TypeToken<ArrayList<Field>>(){}.getType();
        try{
            schemaFields = gson.fromJson(schemaExtra, schemaType);
        }catch (Throwable t){
            Log.e(TAG, "Exception getting json; " + t);
        }
        String stringExtra = intent.getStringExtra("Section");
        jsonObject = gson.fromJson(stringExtra, JsonObject.class);

        try {
            populate();
        } catch (Exception e) {
            Log.e(TAG, "exception in populate(): " + e);
        }
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
                JsonObject values = getValues();
                getMessages().appEvent(StateManager.AppEvents.UserSaved, EventParameters.build()
                        .add("Result", values)
                        .add("ResultCode", StateManager.ActivityResultCodes.Saved));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
