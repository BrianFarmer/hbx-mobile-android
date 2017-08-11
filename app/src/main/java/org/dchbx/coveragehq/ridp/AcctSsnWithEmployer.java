package org.dchbx.coveragehq.ridp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.dchbx.coveragehq.databinding.AcctSsnWithEmployerBinding;
import org.dchbx.coveragehq.models.ridp.Employer;
import org.dchbx.coveragehq.models.ridp.VerifyIdentityResponse;

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

public class AcctSsnWithEmployer extends PostVerifiedActivity {
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(AcctSsnWithEmployer.class);
    private AcctSsnWithEmployerBinding binding;
    private VerifyIdentityResponse verificationResponse;
    private FoundEmployersAdapter foundEmployersAdapter;
    private ListView employersListView;
    private Button needIndividual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acct_ssn_with_employer);
        configToolbar();
        employersListView = (ListView) findViewById(R.id.employersListView);
        needIndividual = (Button) findViewById(R.id.needIndividual);
        needIndividual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void populate(VerifyIdentityResponse verificationResponse) {
        ArrayList<Employer> employers = new ArrayList<>(verificationResponse.employers);
        FoundEmployersAdapter foundEmployersAdapter = new FoundEmployersAdapter(this, employers);
        employersListView.setAdapter(foundEmployersAdapter);

    }
}
