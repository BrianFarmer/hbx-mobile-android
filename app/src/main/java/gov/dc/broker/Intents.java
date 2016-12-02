package gov.dc.broker;

import android.content.Intent;

/**
 * Created by plast on 10/27/2016.
 */

public class Intents {
    private static String TAG = "Intents";

    public static final String BROKER_CLIENT_ID = "BrokerClientId";
    public static final String EMPLOYEE_ID = "EmployeeId";

    static public void launchEmployeeDetails(BrokerActivity activity, int employeeId, int brokerClientId) {
        Intent intent = new Intent(activity, EmployeeDetailsActivity .class);
        intent.putExtra(EMPLOYEE_ID, employeeId);
        intent.putExtra(BROKER_CLIENT_ID, brokerClientId);
        activity.startActivity(intent);
    }

    static public void launchEmployerDetails(MainActivity mainActivity, int index) {
        launchEmployerDetailsActivity(mainActivity, index);
    }

    static public void launchEmployerDetailsActivity(MainActivity mainActivity, int index) {
        Intent intent = new Intent(mainActivity, EmployerDetailsActivity.class);
        intent.putExtra(BROKER_CLIENT_ID, index);
        mainActivity.startActivity(intent);
    }
}
