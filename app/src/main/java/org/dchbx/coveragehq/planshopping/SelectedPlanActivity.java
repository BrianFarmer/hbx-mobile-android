package org.dchbx.coveragehq.planshopping;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.dchbx.coveragehq.BaseActivity;
import org.dchbx.coveragehq.PlanCardPopulation;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.ServiceManager;
import org.dchbx.coveragehq.Utilities;
import org.dchbx.coveragehq.models.planshopping.Plan;
import org.dchbx.coveragehq.models.startup.EffectiveDate;
import org.dchbx.coveragehq.startup.StartUpService;
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
public class SelectedPlanActivity extends BaseActivity{
    private static String TAG = "SelectedPlanActivity";
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(SelectedPlanActivity.class);
    private Plan plan;
    private ImageView carrierLogo;
    private TextView planName;
    private TextView planType;
    private ImageView planMetalRing;
    private TextView metalType;
    private TextView monthlyPremium;
    private TextView annualPremium;
    private TextView deductible;
    private Button confirmButton;
    private EffectiveDate effectiveDate;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Intent intent = getIntent();
        plan = PlanShoppingService.getPlanFromIntent(intent);
        effectiveDate = StartUpService.getEffectiveDate(intent);
        if (effectiveDate == null){
            try {
                effectiveDate = ServiceManager.getServiceManager().getConfigurationStorageHandler().readEffectiveDate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setContentView(R.layout.selected_plan);
        configToolbar();
        populate();
    }

    private void populate() {
        View header = findViewById(R.id.planDetailsHeader);
        PlanCardPopulation.populateFromHealth(header, plan, this);

        TextView effectiveDateField = (TextView)findViewById(R.id.effectiveDate);
        effectiveDateField.setText(Utilities.DateAsMonthDayYear(effectiveDate.effectiveDate));

        TextView termAndConditions = (TextView) findViewById(R.id.termAndCondidtions);
        termAndConditions.setText(Html.fromHtml(getString(R.string.terms_and_conditions_content)));
        confirmButton = (Button) findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.appEvent(StateManager.AppEvents.BuyPlanConfirmed, EventParameters.build().add(PlanShoppingService.Plan, plan));
            }
        });
    }
}
