package org.dchbx.coveragehq;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.dchbx.coveragehq.models.planshopping.Plan;

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
public class PlanCardPopulation {
    private static String TAG = "PlanCardPopulation";

    public static void populateFromHealth(View view, Plan plan, Activity activity){
        TextView planName = (TextView) view.findViewById(R.id.planName);
        planName.setText(plan.name);
        if (plan.links != null
                && plan.links.carrierLogo != null) {
            final ImageView carrierLogo = (ImageView) view.findViewById(R.id.carrierLogo);
            try {
                Glide
                        .with(activity)
                        .load(plan.links.carrierLogo)
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                carrierLogo.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(carrierLogo);
            } catch (Throwable t){
                Log.e(TAG, "t: " + t.getMessage());
            }
        }

        TextView planType = (TextView) view.findViewById(R.id.planType);
        if (plan.planType != null){
            planType.setText(plan.planType);
        } else {
            planType.setText("");
        }
        TextView planLocation = (TextView) view.findViewById(R.id.planLocation);
        if (plan.nationwide != null
                && plan.nationwide) {
            planLocation.setText(R.string.nationwide);
        } else {
            planLocation.setText("nationwide false");
        }

        TextView metalLevel = (TextView) view.findViewById(R.id.metalType);
        if (plan.metalLevel != null){
            metalLevel.setText(PlanUtilities.getPlanMetalLevel(plan));
            ImageView planMetalRing = (ImageView) view.findViewById(R.id.planMetalRing);
            planMetalRing.setImageResource(PlanUtilities.getPlanMetalResource(plan));
        } else {
            metalLevel.setText("");
        }

        TextView monthlyPremium = (TextView) view.findViewById(R.id.monthlyPremium);
        monthlyPremium.setText(PlanUtilities.getFormattedMonthlyPremium(plan));
        TextView annualPremium = (TextView) view.findViewById(R.id.annualPremium);
        annualPremium.setText(PlanUtilities.getFormattedYearPremium(plan));
        TextView deductible = (TextView) view.findViewById(R.id.deductible);
        deductible.setText(PlanUtilities.getFormattedDeductible(plan));
    }
}
