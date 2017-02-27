package org.dchbx.coveragehq;

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

    static public void loginDone(){
        loginRunning = false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetLoginResult getLoginResult){
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
                default:
                    if (!loginRunning) {
                        showLogin();
                    }
                    return;
            }
        } else {
            showLogin();
            return;
        }
    }
}
