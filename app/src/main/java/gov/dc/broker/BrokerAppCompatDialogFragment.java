package gov.dc.broker;

import android.support.v7.app.AppCompatDialogFragment;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by plast on 1/27/2017.
 */

public abstract class BrokerAppCompatDialogFragment extends AppCompatDialogFragment {
    private EventBus eventBus;
    private Messages messages;

    protected BrokerActivity getBrokerActivity(){
        return (BrokerActivity) getActivity();
    }

    protected Messages getMessages(){
        return messages;
    }

    public void init() {
        messages = BrokerApplication.getBrokerApplication().getMessages(this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (messages != null) {
            messages.release();
            messages = null;
        }
    }

}
