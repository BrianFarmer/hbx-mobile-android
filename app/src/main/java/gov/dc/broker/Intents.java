package gov.dc.broker;

import android.content.Intent;

/**
 * Created by plast on 10/27/2016.
 */

public class Intents {
    private static String TAG = "Intents";

    public static final String BROKER_CLIENT_ID = "BrokerClientId";

    static public void launchEmployerDetails(MainActivity mainActivity, int index) {
        launchEmployerDetailsActivity(mainActivity, index);
    }


    static public void launchClientDetailsActivity(MainActivity mainActivity, int index) {
        Intent intent = new Intent(mainActivity, ClientDetailsActivity.class);
        intent.putExtra(BROKER_CLIENT_ID, index);
        mainActivity.startActivity(intent);
    }

    static public void launchEmployerDetailsActivity(MainActivity mainActivity, int index) {
        Intent intent = new Intent(mainActivity, EmployerDetailsActivity.class);
        intent.putExtra(BROKER_CLIENT_ID, index);
        mainActivity.startActivity(intent);
    }
}
