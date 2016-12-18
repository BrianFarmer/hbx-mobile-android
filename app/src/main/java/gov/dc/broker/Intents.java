package gov.dc.broker;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by plast on 10/27/2016.
 */

public class Intents {
    private static String TAG = "Intents";

    public static final String BROKER_CLIENT_ID = "BrokerClientId";
    public static final String EMPLOYEE_ID = "EmployeeId";

    static public void launchEmployeeDetails(BrokerActivity activity, String employeeId, String brokerClientId) {
        Intent intent = new Intent(activity, EmployeeDetailsActivity .class);
        intent.putExtra(EMPLOYEE_ID, employeeId);
        intent.putExtra(BROKER_CLIENT_ID, brokerClientId);
        activity.startActivity(intent);
    }

    static public void launchEmployerDetails(MainActivity mainActivity, String id) {
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
}
