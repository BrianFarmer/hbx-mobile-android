package org.dchbx.coveragehq;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by plast on 9/23/2016.
 */

public class NetworkErrorDialog extends AppCompatDialogFragment {
    private static Handler handler;
    View view;
    NetworkErrorDialog dialog;
    private Context context;

    public static NetworkErrorDialog build(BrokerActivity activity, int title, String question) {
        NetworkErrorDialog dialog = new NetworkErrorDialog();

        Bundle bundle = new Bundle();
        bundle.putCharSequence("question", question);
        dialog.setArguments(bundle);
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
        dialog.show(activity.getSupportFragmentManager(), activity.getString(title));
        return dialog;
    }

    public static NetworkErrorDialog build(BrokerActivity activity, int title, int question, Handler handler) {
        NetworkErrorDialog.handler = handler;
        NetworkErrorDialog dialog = new NetworkErrorDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("questionid", question);
        dialog.setArguments(bundle);
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
        dialog.show(activity.getSupportFragmentManager(), activity.getString(title));
        return dialog;
    }

    public NetworkErrorDialog() {
        this.dialog = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        CharSequence question = arguments.getCharSequence("question");
        if (question == null) {
            question = getString(arguments.getInt("questionid"));
        }

        view = inflater.inflate(R.layout.error_message, container, false);
        TextView textViewErrorMessage = (TextView) view.findViewById(R.id.textViewErrorMessage);
        textViewErrorMessage.setText(question);

        Button buttonExit = (Button) view.findViewById(R.id.buttonRetry);
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemUtilities.detectNetwork()) {
                    if (handler != null){
                        handler.finished();
                    }
                    dismiss();
                }
            }
        });
        Button buttonSettings = (Button) view.findViewById(R.id.buttonSettings);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemUtilities.isAirplaneModeOn(BrokerApplication.getBrokerApplication())) {
                    startActivity(new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS));
                } else {
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public static abstract class Handler {
        abstract public void finished();
    }
}
