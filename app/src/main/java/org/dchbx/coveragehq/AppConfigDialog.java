package org.dchbx.coveragehq;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;

import org.dchbx.coveragehq.databinding.AppConfigActivityBinding;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AppConfigDialog extends BrokerAppCompatDialogFragment {
    private static String TAG = "AppConfigDialog";

    private View view;
    private ServiceManager.AppConfig appConfig;
    RadioGroup serverGroup;
    private EditText gitHubUrl;
    private EditText hbxMobileServerUrl;
    private Context context;
    private AppConfigActivityBinding binding;
    private DialogInterface.OnClickListener okButtonListener;

    public static void build(LoginActivity loginActivity, DialogInterface.OnClickListener onClickListener){
        AppConfigDialog appConfigDialog = new AppConfigDialog();
        appConfigDialog.okButtonListener = onClickListener;
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

    public void ok(){
        Log.d(TAG, "AppConfigDialog.ok clicked");
        getMessages().updateAppConfig(appConfig);
        dismiss();
        if (okButtonListener != null){
            okButtonListener.onClick(null, 0);
        }
    }

    public void cancel(){
        Log.d(TAG, "AppConfigDialog.cancel clicked");
        super.dismiss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle bundle = getArguments();

        binding = DataBindingUtil.inflate(inflater, R.layout.app_config_activity, container, false);
        view = binding.getRoot();
        getDialog().setCanceledOnTouchOutside(false);
        getMessages().getAppConfig();
        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetAppConfigResult getAppConfigResult) {
        appConfig = getAppConfigResult.getAppConfig();

        binding.setAppConfig(appConfig);
        binding.setDialog(this);
    }

    public void setDataSource(ServiceManager.DataSource dataSource){
        appConfig.DataSource = dataSource;
    }
}
