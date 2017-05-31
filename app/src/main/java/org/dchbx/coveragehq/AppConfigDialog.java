package org.dchbx.coveragehq;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AppConfigDialog extends BrokerAppCompatDialogFragment {
    private View view;
    private BuildConfig2.AppConfig appConfig;
    RadioGroup serverGroup;
    private EditText gitHubUrl;
    private EditText hbxMobileServerUrl;
    private Context context;

    public static void build(LoginActivity loginActivity){
        AppConfigDialog appConfigDialog = new AppConfigDialog();
        Bundle args = new Bundle();
        appConfigDialog.setArguments(args);
        appConfigDialog.show(loginActivity.getSupportFragmentManager(), "AppConfigDialog");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // initializes bases class, mainly for event bus.
        init();

        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle bundle = getArguments();

        view = inflater.inflate(R.layout.app_config_activity,container, false);
        getDialog().setCanceledOnTouchOutside(false);
        Button buttonCancel = (Button) view.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LoginActivity)getActivity()).fingerprintCanceled();
                dismiss();
            }
        });


        Button ok = (Button) view.findViewById(R.id.buttonOk);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMessages().updateAppConfig(appConfig);
                dismiss();
            }
        });
        serverGroup = (RadioGroup) view.findViewById(R.id.serverGroup);
        gitHubUrl = (EditText)view.findViewById(R.id.gutHubUrl);
        hbxMobileServerUrl = (EditText) view.findViewById(R.id.hbxMobileServerUrl);

        getMessages().getAppConfig();
        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetAppConfigResult getAppConfigResult) {
        appConfig = getAppConfigResult.getAppConfig();

        switch (appConfig.DataSource){
            case GitHub:
                serverGroup.check(R.id.github);
                gitHubUrl.setText(appConfig.GithubUrl);
                break;
            case MobileServer:
                serverGroup.check(R.id.mobileServer);
                hbxMobileServerUrl.setText(appConfig.MobileServerUrl);
        }
    }
}
