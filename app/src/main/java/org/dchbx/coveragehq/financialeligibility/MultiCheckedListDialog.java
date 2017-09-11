package org.dchbx.coveragehq.financialeligibility;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.models.fe.Field;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.StateManager;

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
public class MultiCheckedListDialog extends CheckedListDialog {
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(MultiCheckedListDialog.class);

    public static int Saved = 1;

    private Field field;
    private ListView listView;
    private MultiCheckedListAdapter multiCheckedListAdapter;
    private TextView label;
    private ArrayList<Integer> values;

    public MultiCheckedListDialog(){
    }

    private void getValues(Intent intent) {
        values = intent.getExtras().getIntegerArrayList("Value");
    }

    @Override
    protected BaseAdapter getListAdapter() {
        multiCheckedListAdapter = new MultiCheckedListAdapter(field.options, this, values);
        return multiCheckedListAdapter;
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
                                                                                .add("Result", multiCheckedListAdapter.getCurrentIndexes())
                                                                                .add("ResultCode", Saved));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
