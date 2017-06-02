package org.dchbx.coveragehq;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.text.Editable;
import android.text.TextWatcher;
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
    private BrokerWorkerConfig.AppConfig appConfig;
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

        serverGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId != -1
                    && appConfig != null){
                    switch (checkedId){
                        case R.id.github:
                            appConfig.DataSource = BrokerWorkerConfig.DataSource.GitHub;
                            break;
                        case R.id.mobileServer:
                            appConfig.DataSource = BrokerWorkerConfig.DataSource.MobileServer;
                            break;
                    }
                }
            }
        });

        gitHubUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (appConfig == null){
                    return;
                }
                appConfig.GithubUrl = String.valueOf(gitHubUrl.getText());
            }
        });

        hbxMobileServerUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (appConfig == null){
                    return;
                }
                appConfig.MobileServerUrl = String.valueOf(hbxMobileServerUrl.getText());
            }
        });

        getMessages().getAppConfig();
        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetAppConfigResult getAppConfigResult) {
        appConfig = getAppConfigResult.getAppConfig();

        gitHubUrl.setText(appConfig.GithubUrl);
        hbxMobileServerUrl.setText(appConfig.MobileServerUrl);

        switch (appConfig.DataSource){
            case GitHub:
                serverGroup.check(R.id.github);
                break;
            case MobileServer:
                serverGroup.check(R.id.mobileServer);
        }
    }
}
