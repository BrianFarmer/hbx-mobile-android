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
        Bundle bundle = getArguments();
        if (bundle != null) {
            relogin = bundle.getBoolean("relogin");
        }


        view = inflater.inflate(R.layout.fingerprint,container, false);
        getDialog().setCanceledOnTouchOutside(false);
        View textViewFingerprintStatus = view.findViewById(R.id.textViewFingerprintStatus);
        textViewFingerprintStatus.setVisibility(View.GONE);
        if (relogin){
            getMessages().decryptAccountAndPassword();
        } else {
            getMessages().validateLogin();
        }

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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.FingerprintStatus fingerprintStatus){
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.FingerprintAuthenticationDecryptResult fingerprintAuthenticationUpdate){
        TextView textViewFingerprintStatus = (TextView) view.findViewById(R.id.textViewFingerprintStatus);

        Vibrator vibratorService = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);
        try {
            vibratorService.vibrate(300);
        } catch (Exception e){
            Log.d(TAG, "vibration failed, not very important");
        }

        if (fingerprintAuthenticationUpdate.getErrorMessage()!= null) {
            textViewFingerprintStatus.setText("Authentication error, please try again.");
            return;
        }

        if (fingerprintAuthenticationUpdate.getHelpString() != null) {
            textViewFingerprintStatus.setText("Authentication help???");
            return;
        }

        textViewFingerprintStatus.setText("Fingerprint authenticated.");
        ((LoginActivity)getActivity()).authenticated(fingerprintAuthenticationUpdate.getAccountName(), fingerprintAuthenticationUpdate.getPassword());
        dismiss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.FingerprintAuthenticationEncryptResult fingerprintAuthenticationUpdate){
        TextView textViewFingerprintStatus = (TextView) view.findViewById(R.id.textViewFingerprintStatus);

        Vibrator vibratorService = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);
        try {
            vibratorService.vibrate(300);
        } catch (Exception e){
            Log.d(TAG, "vibration failed, not very important");
        }

        if (fingerprintAuthenticationUpdate.getErrorMessage()!= null) {
            textViewFingerprintStatus.setText("Authentication error, please try again.");
            return;
        }

        if (fingerprintAuthenticationUpdate.getHelpString() != null) {
            textViewFingerprintStatus.setText("Authentication help???");
            return;
        }

        textViewFingerprintStatus.setText("Fingerprint authenticated.");
        ((LoginActivity)getActivity()).authenticated(fingerprintAuthenticationUpdate.getEncryptedText());
        dismiss();
    }
}
