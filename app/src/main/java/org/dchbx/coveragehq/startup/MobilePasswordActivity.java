package org.dchbx.coveragehq.startup;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.dchbx.coveragehq.BaseActivity;
import org.dchbx.coveragehq.R;
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
public class MobilePasswordActivity extends BaseActivity {
    private static String TAG = "MobilePasswordActivity";
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(MobilePasswordActivity.class);

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.mobile_password);
        ((TextView)findViewById(R.id.mobilePasswordContent)).setText(Html.fromHtml(getString(R.string.mobile_password_content)));
        configToolbar();
        ((Button)findViewById(R.id.buttonYes)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.appEvent(StateManager.AppEvents.Yes);
            }
        });
        ((Button)findViewById(R.id.buttonNo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.appEvent(StateManager.AppEvents.No);
            }
        });
    }



    public void onClick(){

    }
}