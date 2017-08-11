package org.dchbx.coveragehq;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import org.dchbx.coveragehq.models.planshopping.Plan;
import org.joda.time.LocalDate;

/**
 * Created by plast on 10/27/2016.
 */

public class Intents {
    private static String TAG = "Intents";

    public static final String PLAN_ID = "PlanId";
    public static final String ENROLLMENT_DATE_ID = "BrokerClientId";
    public static final String BROKER_CLIENT_ID = "BrokerClientId";
    public static final String EMPLOYEE_ID = "EmployeeId";
    public static final String COVERAGE_YEAR = "CoverageEYear";
    public static final String SHOW_HEALTH_ID = "ShowHealth";

    static final int REQUEST_IMAGE_CAPTURE = 1;


    static public void launchEmployeeDetails(BrokerActivity activity, String employeeId, String brokerClientId, LocalDate coverageYear) {
        Intent intent = new Intent(activity, EmployeeDetailsActivity .class);
        intent.putExtra(EMPLOYEE_ID, employeeId);
        intent.putExtra(BROKER_CLIENT_ID, brokerClientId);
        String dateStr = coverageYear.toString();
        intent.putExtra(COVERAGE_YEAR, dateStr);
        activity.startActivity(intent);
    }

    static public void launchEmployerDetails(Activity mainActivity, String id) {
        launchEmployerDetailsActivity(mainActivity, id);
    }

    static public void launchEmployerDetailsActivity(Activity activity) {
        Intent intent = new Intent(activity, EmployerDetailsActivity.class);
        activity.startActivity(intent);
    }

    static public void launchEmployerDetailsActivity(Activity mainActivity, String id) {
        Intent intent = new Intent(mainActivity, EmployerDetailsActivity.class);
        intent.putExtra(BROKER_CLIENT_ID, id);
        mainActivity.startActivity(intent);
    }

    public static void launchBrokerActivity(LoginActivity loginActivity) {
        Intent intent = new Intent(loginActivity, MainActivity.class);
        loginActivity.startActivity(intent);
    }

    public static void restartApp(Activity activity){
        Intent intent = new Intent(activity, RootActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    public static void launchBrokerDetailsActivity(RootActivity rootActivity) {
        Intent intent = new Intent(rootActivity, MainActivity.class);
        rootActivity.startActivity(intent);
    }

    public static void launchEmployeeDetailsActivity(RootActivity rootActivity) {
        Intent intent = new Intent(rootActivity, EmployeeDetailsActivity.class);
        rootActivity.startActivity(intent);
    }

    public static void launchInsuredUserDetailsActivity(RootActivity rootActivity) {
        Intent intent = new Intent(rootActivity, InsuredDetailsActivity.class);
        rootActivity.startActivity(intent);
    }

    public static void launchCamera(Activity activity, int requestId, Uri outputFileUri) {

    }

    public static void launchSummaryOfBenefitsActivity(Activity activity, LocalDate date, boolean showHealth) {
        Intent intent = new Intent(activity, SummaryOfBenefitsActivityHacked.class);
        intent.putExtra(ENROLLMENT_DATE_ID, date.toString());
        intent.putExtra(SHOW_HEALTH_ID, showHealth);
        activity.startActivity(intent);
    }

    public static void launchChoosePlan(Activity activity) {
        Intent intent = new Intent(activity, FamilyActivity.class);
        activity.startActivity(intent);
    }

    public static void launchPremiumAndDeductible(Activity activity) {
        Intent intent = new Intent(activity, PremiumAndDeductibleActivity.class);
        activity.startActivity(intent);
    }

    public static void launchPlanSelector(Activity activity) {
        Intent intent = new Intent(activity, PlanSelector.class);
        activity.startActivity(intent);
    }

    public static void launchPlanDetails(Activity activity, Plan plan) {
        Intent intent = new Intent(activity, PlanDetailsActivity.class);
        intent.putExtra(PLAN_ID, plan.id);
        activity.startActivity(intent);
    }

    public static void launchActivity(Class<?> cls, Activity activity) {
        Intent intent = new Intent(activity, cls);
        activity.startActivity(intent);
    }

    public static void launchStateInfo(Activity activity) {
        Intent intent = new Intent(activity, StateInfo.class);
        activity.startActivity(intent);
    }
}
