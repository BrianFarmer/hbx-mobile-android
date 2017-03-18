package org.dchbx.coveragehq;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by plast on 2/3/2017.
 */

public class RootActivity extends BrokerActivity {
    private final String TAG = "RootActivity";
    private static boolean loginRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BuildConfig2.initMobileCenter();
    }


    @Override
    protected void onResume() {
        super.onResume();
        getMessages().getLogin();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    private void showLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        loginRunning = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Error error) {
        showLogin();
    }



    static public void loginDone(){
        loginRunning = false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetLoginResult getLoginResult){
        if (getLoginResult.getErrorMessagge() != null){
            if (!loginRunning) {
                showLogin();
            } else {
                restartApp(this);
            }
            return;
        }

        if (getLoginResult.isLoggedIn()) {
            switch (getLoginResult.getUserType()) {
                case Broker:
                    Log.d(TAG, "requesting employer list");
                    Intents.launchBrokerDetailsActivity(this);
                    finish();
                    break;
                case Employer:
                    Intents.launchEmployerDetailsActivity(this);
                    finish();
                    break;
                case Employee:
                    Intents.launchEmployeeDetailsActivity(this);
                    finish();
                    break;
                default:
                    if (!loginRunning) {
                        showLogin();
                    }
            }
        } else {
            showLogin();
        }
    }

    public static void restartApp(Context context) {
        Intent mStartActivity = new Intent(context, RootActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);

    }
}
