package gov.dc.broker;

import android.support.v4.app.Fragment;

import org.greenrobot.eventbus.EventBus;

public class BrokerFragment extends Fragment {
    private EventBus eventBus;
    private Messages messages;

    protected BrokerActivity getBrokerActivity(){
        return (BrokerActivity) getActivity();
    }

    protected Messages getMessages(){
        return BrokerApplication.getBrokerApplication().getMessages();
    }

    public void init() {
        BrokerApplication.getBrokerApplication().initFragment(this);
    }
}
