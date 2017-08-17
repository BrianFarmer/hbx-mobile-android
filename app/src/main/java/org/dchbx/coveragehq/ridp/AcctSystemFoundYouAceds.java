package org.dchbx.coveragehq.ridp;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.databinding.AcctSystemFoundYouAcedsBinding;
import org.dchbx.coveragehq.models.ridp.VerifyIdentityResponse;
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
public class AcctSystemFoundYouAceds extends PostVerifiedActivity {
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(AcctSystemFoundYouAceds.class);
    private ImageButton continueButton;

    private AcctSystemFoundYouAcedsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.acct_system_found_you_aceds);
        TextView toMakeChanges = (TextView) findViewById(R.id.toMakeChanges);
        toMakeChanges.setText(Html.fromHtml(getString(R.string.to_make_changes)));
        configToolbar();
    }

    public void continueClicked(){
        Toast toast = Toast.makeText(this, "continue clicked", Toast.LENGTH_LONG);
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