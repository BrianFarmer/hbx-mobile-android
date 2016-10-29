package gov.dc.broker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by plast on 10/27/2016.
 */

public class BrokerActivity extends AppCompatActivity {

    protected EventBus eventBus;
    protected Messages messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BrokerApplication brokerApplication = BrokerApplication.getBrokerApplication();
        brokerApplication.initActivity(this);
        messages = brokerApplication.getMessages();
    }

    @Override
    public void finish(){
        BrokerApplication.getBrokerApplication().finish(this);
        super.finish();
    }

    public Messages getMessages() {
        return messages;
    }
}
