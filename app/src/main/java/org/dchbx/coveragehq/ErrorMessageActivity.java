package org.dchbx.coveragehq;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import org.dchbx.coveragehq.databinding.IvlErrorMessageBinding;
import org.dchbx.coveragehq.financialeligibility.FinancialEligibilityService;
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
public class ErrorMessageActivity extends BaseActivity {
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(ErrorMessageActivity.class);

    private IvlErrorMessageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.ivl_error_message);
        configToolbar();
        binding.setActivity(this);
        binding.setServerError(FinancialEligibilityService.getServerErrorFromIntent(getIntent()));
    }

    public void back(){
        getMessages().appEvent(StateManager.AppEvents.Back);
    }


}
