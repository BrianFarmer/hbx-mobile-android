package org.dchbx.coveragehq;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.joda.time.LocalDate;

/**
 * Created by plast on 10/27/2016.
 */

public class Intents {
    private static String TAG = "Intents";

    public static final String BROKER_CLIENT_ID = "BrokerClientId";
    public static final String EMPLOYEE_ID = "EmployeeId";
    public static final String COVERAGE_YEAR = "CoverageEYear";

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
        try {
            Intent mStartActivity = new Intent(activity, RootActivity.class);
            mStartActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            int mPendingIntentId = 123456;
            PendingIntent mPendingIntent = PendingIntent.getActivity(activity, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        } catch (Throwable t){
            Log.d(TAG, "Throw during restart");
        }


/*
        Intent intent = new Intent(activity, RootActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);*/
    }

    public static void launchBrokerDetailsActivity(RootActivity rootActivity) {
        Intent intent = new Intent(rootActivity, MainActivity.class);
        rootActivity.startActivity(intent);
    }
}
