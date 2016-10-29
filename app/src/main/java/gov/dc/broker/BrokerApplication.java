package gov.dc.broker;

import android.app.Application;
import android.util.Log;

import net.danlew.android.joda.JodaTimeAndroid;

import org.greenrobot.eventbus.EventBus;

import static gov.dc.broker.BrokerWorker.eventBus;

/**
 * Created by plast on 7/21/2016.
 */
public class BrokerApplication extends Application {

    private static final String TAG = "BrokerApplication";
    private BrokerWorker brokerWorker = new BrokerWorker();
    private boolean fingerprintSupported;
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
        JodaTimeAndroid.init(this);
        HbxSite.initClients();
        brokerWorker.onHandleIntent(null);
    }

    private Messages messages = null;
    public Messages getMessages(){
        if (messages == null){
            messages = new EventBusMessages();
        }
        return messages;
    }

    public void initActivity(BrokerActivity activity){
        eventBus = EventBus.getDefault();
        eventBus.register(activity);
    }

    public void initFragment(BrokerFragment fragment) {
        eventBus = EventBus.getDefault();
        eventBus.register(fragment);
    }

    public void finish(BrokerActivity activity) {
        EventBus.getDefault().unregister(activity);
    }

    public void finish(BrokerFragment activity) {
        EventBus.getDefault().unregister(activity);
    }
}
