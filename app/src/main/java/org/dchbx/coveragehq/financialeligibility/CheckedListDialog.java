package org.dchbx.coveragehq.financialeligibility;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.dchbx.coveragehq.BaseActivity;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.models.fe.Field;
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
public class CheckedListDialog extends BaseActivity {
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(CheckedListDialog.class);


    private Field field;
    private ListView listView;
    private CheckedListAdapter checkedListAdapter;
    private BaseAdapter baseAdapter;
    private TextView label;
    private String value;
    private int currentIndex;


    public CheckedListDialog(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.app_list_activity);
        configToolbar();
        Intent intent = getIntent();
        Gson gson = new Gson();
        CharSequence jsonString = intent.getExtras().getCharSequence("Field");
        field = gson.fromJson(jsonString.toString(), Field.class);
        getValues(intent);
        populate();
    }

    private void getValues(Intent intent) {
        value = (String)intent.getExtras().getCharSequence("Value");
    }

    private void populate() {
        label = (TextView)findViewById(R.id.label);
        label.setText(field.label);
        listView = (ListView) findViewById(R.id.listView);

        currentIndex = -1;
        for (int i = 0 ; i < field.options.size(); i++) {
            if (field.options.get(i).value.equals(field.defaultValue)){
                currentIndex=i;
            }
        }

        baseAdapter = getListAdapter();
        listView.setAdapter(checkedListAdapter);
    }

    protected BaseAdapter getListAdapter() {
        checkedListAdapter = new CheckedListAdapter(field.options, this, currentIndex);
        return checkedListAdapter;
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
                getMessages().appEvent(StateManager.AppEvents.DropdownSaved, EventParameters.build()
                                                                                .add("Result", checkedListAdapter.getCurrentIndex())
                                                                                .add("ResultCode", StateManager.ActivityResultCodes.Saved));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
