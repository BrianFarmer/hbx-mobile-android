package org.dchbx.coveragehq;

import org.dchbx.coveragehq.models.planshopping.Plan;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by plast on 5/12/2017.
 */

class PlanUtilities {
    public static double getMaxPremium(List<Plan> planList) {
        double maxPremium = 0;
        for (Plan plan : planList) {
            if (plan.cost != null){
                if (plan.cost.monthlyPremium > maxPremium){
                    maxPremium = plan.cost.monthlyPremium;
                }
            }
        }
        return maxPremium;
    }

    public static double getMaxDeductible(List<Plan> planList) {
        double maxDeductible = 0;
        for (Plan plan : planList) {
            if (plan.cost != null){
                if (plan.cost.deductible > maxDeductible){
                    maxDeductible = plan.cost.deductible;
                }
            }
        }
        return maxDeductible;
    }

    public static List<Plan> getPlansInRange(List<Plan> planList, double maxPremium, double maxDeductible) {
        List<Plan> filtered = new ArrayList<>();
        for (Plan plan : planList) {
            if (plan.cost.monthlyPremium <= maxPremium
                && plan.cost.deductible <= maxDeductible){
                filtered.add(plan);
            }
        }
        return filtered;
    }
}
