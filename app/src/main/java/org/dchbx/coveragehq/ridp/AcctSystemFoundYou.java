package org.dchbx.coveragehq.ridp;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.dchbx.coveragehq.databinding.AcctSystemFoundYouBinding;
import org.dchbx.coveragehq.models.ridp.VerifyIdentityResponse;

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
public class AcctSystemFoundYou extends PostVerifiedActivity {
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(AcctSystemFoundYou.class);
    private ImageButton continueButton;

    private AcctSystemFoundYouBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.acct_system_found_you);
        configToolbar();
    }

    public void pleaseLogInClicked(){
        Toast toast = Toast.makeText(this, "please sign in clicked", Toast.LENGTH_LONG);
        toast.show();
    }

    public void reviewYourAnswersClicked(){
        Toast toast = Toast.makeText(this,"reviewYourAnswersClicked in clicked", Toast.LENGTH_LONG);
        toast.show();
    }

    public void comeBackLaterClicked(){
        Toast toast = Toast.makeText(this,"come back later clicke", Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    protected void populate(VerifyIdentityResponse verificationResponse) {

    }
}
