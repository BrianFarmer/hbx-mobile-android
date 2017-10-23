package org.dchbx.coveragehq;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import org.dchbx.coveragehq.models.planshopping.Plan;
import org.dchbx.coveragehq.models.services.Service;

import java.util.ArrayList;
import java.util.List;

/*
    This file is part of DC.

    DC Health Link SmallBiz is free software:you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation,either version 3of the License,or
    (at your option)any later version.

    DC Health Link SmallBiz is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY;without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DC Health Link SmallBiz.If not,see<http://www.gnu.org/licenses/>.
    This statement should go near the beginning of every source file,close to the copyright notices.When using the Lesser GPL,insert the word “Lesser” before “General” in all three places.When using the GNU AGPL,insert the word “Affero” before “General” in all three places.
*/

public class PlanDetailsAdapter extends SummaryAdapter {

    private final Activity planDetailsActivity;
    private final Plan plan;

    public PlanDetailsAdapter(Activity planDetailsActivity, Plan plan, List<Service> services) {
        super(planDetailsActivity);

        this.planDetailsActivity = planDetailsActivity;
        this.plan = plan;
        items = new ArrayList<>();
        items.add(new PlanOverviewWrapper(planDetailsActivity, this.plan));
        items.add(new ResourcesHeaderWrapper());
        if (this.plan.links.summaryOfBenefits != null){
            items.add(new ResourcesItemWrapper(this.plan.links.summaryOfBenefits, R.drawable.pdf_document, activity.getString(R.string.terms_conditions_pdf), activity));
        }
        items.add(new ResourcesItemWrapper(this.plan.links.providerDirectory, R.drawable.physicians, activity.getString(R.string.provider_directory), activity));
        items.add(new SummaryHeaderWrapper());

        if (services != null) {
            for (Service service : services) {
                items.add(new SummaryItemWrapper(service, items.size(), this));
            }
        }

        this.inflater = (LayoutInflater) planDetailsActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
}

class PlanOverviewWrapper extends AdapterItemWrapperBase {
    private final Activity activity;
    private final Plan plan;

    public PlanOverviewWrapper(Activity activity, Plan plan){

        this.activity = activity;
        this.plan = plan;
    }

    @Override
    public int getLayout() {
        return R.layout.plan_details_header;
    }

    @Override
    public void populate(View view) {
        PlanCardPopulation.populateFromHealth(view, plan, activity);
    }

    @Override
    public String getViewType() {
        return "PlanOverviewWrapper";
    }
}
