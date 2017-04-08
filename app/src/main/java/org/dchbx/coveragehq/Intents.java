package org.dchbx.coveragehq;

import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;

import org.joda.time.LocalDate;

/**
 * Created by plast on 10/27/2016.
 */

public class Intents {
    private static String TAG = "Intents";

    public static final String BROKER_CLIENT_ID = "BrokerClientId";
    public static final String EMPLOYEE_ID = "EmployeeId";
    public static final String COVERAGE_YEAR = "CoverageEYear";

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
        Intent intent = new Intent(rootActivity, EmployeeDetailsActivity.class);
        rootActivity.startActivity(intent);
    }

    public static void launchCamera(Activity activity, int requestId) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(BrokerApplication.getBrokerApplication().getPackageManager()) != null) {
            activity.startActivityForResult(takePictureIntent, requestId);
        }
    }
}
