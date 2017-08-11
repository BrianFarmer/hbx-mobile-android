package org.dchbx.coveragehq;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatDialogFragment;

import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by plast on 1/27/2017.
 */

public abstract class BrokerAppCompatDialogFragment extends AppCompatDialogFragment {
    private EventBus eventBus;
    private Messages messages;
    private ProgressDialog progressDialog;

    protected BrokerActivity getBrokerActivity(){
        return (BrokerActivity) getActivity();
    }

    protected Messages getMessages(){
        return messages;
    }

    public void init() {
        messages = BrokerApplication.getBrokerApplication().getMessages(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.StateAction stateAction){
        switch (stateAction.getAction()){
            case Dismiss:
                this.dismiss();
                break;
            case Finish:
                break;
            case LaunchActivity:
                break;
            case LaunchDialog:
                StateManager.UiDialog.getUiDialogType(stateAction.getUiActivityId()).dialogBuilder.build(stateAction.getEventParameters(), (BaseActivity) this.getActivity());
                break;
            case ShowWait:
                showProgress();
                break;
            case HideWait:
                hideProgress();
                break;
        }

    }


    protected void hideProgress() {
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    protected void showProgress() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(this.getBrokerActivity());
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getString(R.string.waiting));
        }
        progressDialog.show();
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
