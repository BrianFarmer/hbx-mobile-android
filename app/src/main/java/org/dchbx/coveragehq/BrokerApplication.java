package org.dchbx.coveragehq;

import android.app.Application;
import android.util.Log;

import net.danlew.android.joda.JodaTimeAndroid;

import org.greenrobot.eventbus.EventBus;

import static org.dchbx.coveragehq.BrokerWorker.eventBus;

/**
 * Created by plast on 7/21/2016.
 */
public class BrokerApplication extends Application {

    private static final String TAG = "BrokerApplication";
    private BrokerWorker brokerWorker = null;
    private boolean fingerprintSupported;
    private static BrokerApplication brokerApplication;

    public BrokerApplication() {
        brokerApplication = this;
    }

    public static BrokerApplication getBrokerApplication() {
        if (brokerApplication == null){
            Log.e(TAG, "BrokerApplication is not initialized yet.");
        }
        return brokerApplication;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG, "In BrokerApplicaiton.onCreate");
        //Intent intent = new Intent(this, LoginActivity.class);
        JodaTimeAndroid.init(this);
        // ConnectionHandler.initClients();
        brokerWorker = new BrokerWorker();
        brokerWorker.onHandleIntent(null); // this causes the background working to be initialized.
    }

    public void finish(BrokerActivity activity) {
        if (eventBus != null){
            EventBus.getDefault().unregister(activity);
        }
    }

    public void finish(BrokerFragment fragment) {
        if (eventBus != null) {
            eventBus.unregister(fragment);
        }
    }

    public void pauseActivity(BrokerActivity activity) {
        if (eventBus != null){
            eventBus.unregister(activity);
        }
    }

    public Messages getMessages(BaseActivity activity) {
        return new EventBusMessages(activity);
    }

    public Messages getMessages(BrokerFragment fragment) {
        return new EventBusMessages(fragment);
    }

    public Messages getMessages(BrokerAppCompatDialogFragment brokerAppCompatDialogFragment) {
            return new EventBusMessages(brokerAppCompatDialogFragment);
    }
}
