package org.dchbx.coveragehq;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;

import org.dchbx.coveragehq.databinding.HelloScreenBinding;
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
public class HelloActivity extends BrokerActivity {
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(HelloActivity.class);
    private static final String TAG = "HelloActivity";
    private HelloScreenBinding binding;

    public HelloActivity(){
        Log.d(TAG, "HelloActivity() called.");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.hello_screen);
        binding.setActivity(this);
    }

    public void onViewMyAccountClicked(){
        getMessages().appEvent(StateManager.AppEvents.ViewMyAccount);
    }

    public void onStartClicked(){
        getMessages().appEvent(StateManager.AppEvents.StartApplication);
    }

    public void onResumeClicked(){
        getMessages().appEvent(StateManager.AppEvents.ResumeApplication);
    }
}
