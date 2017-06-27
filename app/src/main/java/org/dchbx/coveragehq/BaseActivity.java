package org.dchbx.coveragehq;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    protected Messages messages = null;

    public Messages getMessages() {
        return messages;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (messages == null) {
            Log.d(TAG, "in BaseActivity.onCreate");
            messages = BrokerApplication.getBrokerApplication().getMessages(this);
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "In BaseActivity.onResume()");

        super.onResume();
        if (messages == null) {
            messages = BrokerApplication.getBrokerApplication().getMessages(this);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (messages != null) {
            Log.d(TAG, "releasing messages in BaseActivity.onDestroy");
            messages.release();
            messages = null;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (messages != null) {
            Log.d(TAG, "releasing messages in BaseActivity.onPause");
            messages.release();
            messages = null;
        }
    }


}
