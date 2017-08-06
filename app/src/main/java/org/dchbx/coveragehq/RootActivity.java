package org.dchbx.coveragehq;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by plast on 2/3/2017.
 */

public class RootActivity extends BrokerActivity {
    private static final String TAG = "RootActivity";
    private static boolean loginRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "**************** onCreate");
        ServiceManager.getServiceManager().enrollConfig().initMobileCenter();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "**************** onResume");
        getMessages().getLogin();
        getMessages().getCurrentActivity();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    private void showLogin() {
        Log.d(TAG, "**************** showLogin");
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
    public void doThis(Events.GetCurrentActivityResult getCurrentActivityResult) {
        StateManager.UiActivity.Info uiActivityType = StateManager.UiActivity.getUiActivityType(getCurrentActivityResult.getUiActivity());
        Intents.launchActivity(uiActivityType.cls, this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetLoginResult getLoginResult){
        if (getLoginResult.getErrorMessagge() != null
            && !getLoginResult.isTimedout()){
            if (!loginRunning) {
                showLogin();
            } else {
                restartApp(this);
            }
            return;
        }
    }

    public static void restartApp(Context context) {
        try {
            Intent mStartActivity = new Intent(context, RootActivity.class);
            mStartActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            int mPendingIntentId = 123456;
            PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
            System.exit(0);
        } catch (Throwable t){
            Log.d(TAG, "Throw during restart");
        }
    }
}
