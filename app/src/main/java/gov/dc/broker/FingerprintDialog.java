package gov.dc.broker;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by plast on 1/31/2017.
 */

public class FingerprintDialog extends BrokerAppCompatDialogFragment {
    private final String TAG = "FingerprintDialog";
    private View view;
    private Context context;
    private boolean fingerprintManagerAttached = false;
    private boolean relogin = false;

    public static void build(boolean relogin, LoginActivity loginActivity){
        FingerprintDialog fingerprintDialog = new FingerprintDialog();
        Bundle args = new Bundle();
        args.putBoolean("relogin", relogin);
        fingerprintDialog.setArguments(args);
        fingerprintDialog.show(loginActivity.getSupportFragmentManager(), "FingerprintDialog");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            relogin = savedInstanceState.getBoolean("relogin");
        }


        view = inflater.inflate(R.layout.fingerprint,container, false);
        getDialog().setCanceledOnTouchOutside(false);
        View textViewFingerprintStatus = view.findViewById(R.id.textViewFingerprintStatus);
        textViewFingerprintStatus.setVisibility(View.GONE);
        Button buttonCancel = (Button) view.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LoginActivity)getActivity()).fingerprintCanceled();
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // initializes bases class, mainly for event bus.
        init();

        this.context = context;
        getMessages().getFingerprintStatus(true);
        fingerprintManagerAttached = true;
    }



    @Override
    public void onPause(){
        super.onPause();

        if (fingerprintManagerAttached) {
            getMessages().getFingerprintStatus(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.FingerprintStatus fingerprintStatus){
        getMessages().authenticateFingerprint(true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.FingerprintAuthenticationUpdate fingerprintAuthenticationUpdate){
        TextView textViewFingerprintStatus = (TextView) view.findViewById(R.id.textViewFingerprintStatus);

        Vibrator vibratorService = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);
        try {
            vibratorService.vibrate(300);
        } catch (Exception e){
            Log.d(TAG, "vibration failed, not very important");
        }

        switch (fingerprintAuthenticationUpdate.getMessage()){
            case AuthenticationError:
                textViewFingerprintStatus.setText("Authentication error, please try again.");
                break;
            case AuthenticationSucceeded:
                textViewFingerprintStatus.setText("Fingerprint authenticated.");
                ((LoginActivity)getActivity()).authenticated();
                dismiss();
                break;
            case AuthenticationHelp:
                textViewFingerprintStatus.setText("Authentication help???");
                break;
            case AuthenticationFailed:
                textViewFingerprintStatus.setText("Authentication failed, please try again.");
                break;
        }
    }
}
