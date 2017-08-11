package org.dchbx.coveragehq;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;

import java.util.Timer;

/**
 * Created by plast on 9/23/2016.
 */

public class SessionTimeoutDialog extends BrokerAppCompatDialogFragment {

    private ProgressDialog progressDialog;
    private boolean exittingInProgress = false;

    enum DialogState {
        CountingDown,
        TimedOut
    }

    private static String TAG = "SessionTimeoutDialogo";
    View view;
    SessionTimeoutDialog dialog;
    private Context context;
    private DialogState dialogState;
    private DateTime timeout;
    private Timer timer;
    private Button stayLoggedInButton;
    private TextView textViewSessionTimeout;

    static private String SECURITY_QUESTION = "SecurityQuestion";

    public static SessionTimeoutDialog build() {
        SessionTimeoutDialog dialog = new SessionTimeoutDialog();

        Bundle bundle = new Bundle();
        dialog.setArguments(bundle);
        return dialog;
    }

    public SessionTimeoutDialog(){
        this.dialog = this;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // initializes bases class, mainly for event bus.
        init();

        view = inflater.inflate(R.layout.session_timeout, container, false);
        getDialog().setCanceledOnTouchOutside(false);

        textViewSessionTimeout = (TextView)view.findViewById(R.id.textViewSessionTimeout);
        stayLoggedInButton = (Button) view.findViewById(R.id.buttonStayLoggedIn);
        stayLoggedInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                    getMessages().stayLoggedIn();
            }
        });

        updateStatus(ServiceManager.getServiceManager().enrollConfig().getTimeoutCountdownSeconds());
        getMessages().startSessioniTimeoutCountdown();


        dialogState = DialogState.CountingDown;
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.StayLoggedInResult stayLoggedInResult) {
        if (!stayLoggedInResult.getSuccess()) {
            getMessages().logoutRequest(false);
            Intents.restartApp((Activity) context);
        }
        hideProgress();
        dialog.dismiss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.SessionTimedOut sessionTimedOut) {
        if (!exittingInProgress) {
            exittingInProgress = true;
            getMessages().logoutRequest(false);
            Intents.restartApp(getActivity());
            dismiss();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.SessionTimeoutCountdownTick sessionTimeoutCountdownTick) {
        updateStatus(sessionTimeoutCountdownTick.getSecondsLeft());
    }

    private void updateStatus(int secondsLeft){
        Resources res = getResources();
        String text = String.format(res.getString(R.string.timeout_message), secondsLeft);
        textViewSessionTimeout.setText(text);
    }
}
