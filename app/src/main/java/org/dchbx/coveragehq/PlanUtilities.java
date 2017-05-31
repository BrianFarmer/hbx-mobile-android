package org.dchbx.coveragehq;

import org.dchbx.coveragehq.models.planshopping.Plan;
import org.dchbx.coveragehq.models.roster.Health;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;



class PlanUtilities {


    public static int roundToHundress(double number) {
        return (((int)number + 99) / 100 ) * 100;
    }

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

    public static String getPlanMetalLevel(Plan plan){
        return plan.metalLevel;
    }

    public static int getPlanMetalResource(Plan plan){
        if (plan.metalLevel.equalsIgnoreCase("silver")){
            return R.drawable.metal_silver;
        }
        if (plan.metalLevel.equalsIgnoreCase("gold")){
            return R.drawable.metal_gold;
        }
        if (plan.metalLevel.equalsIgnoreCase("platinum")){
            return R.drawable.metal_platinum;
        }
        return R.drawable.metal_bronze;

    }

    public static String getFormattedMonthlyPremium(Plan plan) {
        return NumberFormat.getCurrencyInstance().format(plan.cost.monthlyPremium);
    }

    public static String getFormattedYearPremium(Plan plan) {
        return NumberFormat.getCurrencyInstance().format(plan.cost.monthlyPremium * 12);
    }

    public static String getFormattedDeductible(Plan plan) {
        return NumberFormat.getCurrencyInstance().format(plan.cost.deductible);
    }

    public static String getFormattedMonthlyPremium(Health plan) {
        return NumberFormat.getCurrencyInstance().format(plan.totalPremium);
    }

    public static String getFormattedAnnualPremium(Health plan) {
        return NumberFormat.getCurrencyInstance().format(plan.totalPremium*12);
    }


    public static String getFormattedDeductible(Health plan) {
        //return NumberFormat.getCurrencyInstance().format(plan.deductible);
        return plan.deductible;
    }
}
