package org.dchbx.coveragehq;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    protected Messages messages = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (messages == null) {
            messages = BrokerApplication.getBrokerApplication().getMessages(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (messages == null) {
            messages = BrokerApplication.getBrokerApplication().getMessages(this);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (messages != null) {
            messages.release();
            messages = null;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (messages != null) {
            messages.release();
            messages = null;
        }
    }


}
