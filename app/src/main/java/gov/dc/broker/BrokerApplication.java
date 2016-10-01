package gov.dc.broker;

import android.app.Application;
import android.util.Log;

/**
 * Created by plast on 7/21/2016.
 */
public class BrokerApplication extends Application {

    private static final String TAG = "BrokerApplication";
    private BrokerWorker brokerWorker = new BrokerWorker();
    private static BrokerApplication brokerApplication;

    public BrokerApplication() {
        brokerApplication = this;
    }

    public static BrokerApplication getBrokerApplication() {
        return brokerApplication;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG, "In BrokerApplicaiton.onCreate");
        //Intent intent = new Intent(this, LoginActivity.class);
        HbxSite.initClients();
        brokerWorker.onHandleIntent(null);
    }
}
