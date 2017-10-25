package org.dchbx.coveragehq;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import org.dchbx.coveragehq.models.Errors.ServerError;
import org.dchbx.coveragehq.models.Glossary;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.OnActivityResultListener;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.UUID;


public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    protected ProgressDialog progressDialog;
    protected Messages messages = null;
    private Glossary.GlossaryItem glossaryItem;
    private String glossaryItemName;
    private static BaseActivity currentActivity;
    protected static OnActivityResultListener onActivityResultListener;

    public static void setOnActivityResultListener(OnActivityResultListener onActivityResultListener) {
        BaseActivity.onActivityResultListener = onActivityResultListener;
    }

    public Messages getMessages() {
        return messages;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentActivity = this;
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = getIntent();
        String actionId = intent.getStringExtra("ActionId");
        if (actionId != null) {
            UUID uuid = UUID.fromString(actionId);
            ServiceManager.getServiceManager().getStateManager().setActivityForId(uuid, this);
        }
        if (messages == null) {
            Log.d(TAG, "in BaseActivity.onCreate");
            messages = BrokerApplication.getBrokerApplication().getMessages(this);
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "In BaseActivity.onResume()");

        super.onResume();
        if (messages == null) {
            messages = BrokerApplication.getBrokerApplication().getMessages(this);
        }
        hideProgress();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (messages != null) {
            Log.d(TAG, "releasing messages in BaseActivity.onDestroy");
            messages.release();
            messages = null;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (messages != null) {
            Log.d(TAG, "releasing messages in BaseActivity.onPause");
            messages.release();
            messages = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.StateAction stateAction) {
        switch (stateAction.getAction()){
            case Finish:
            case Pop:
                Intent intent = new Intent();
                EventParameters eventParameters = stateAction.getEventParameters();
                eventParameters.initIntent(intent);
                try {
                    setResult(eventParameters.getActivityResultCode("ResultCode").ordinal(), intent);
                } catch (Exception e) {
                    Log.e(TAG, "Can't find ResultCode in EventParameters");
                }
                finish();
                break;
            case PopAndLaunchActivity:
                hideProgress();
                StateManager.UiActivity.Info uiActivityType = StateManager.UiActivity.getUiActivityType(stateAction.getUiActivityId());
                Intents.launchActivity(uiActivityType.cls, this, stateAction.getEventParameters());
                finish();
                break;
            case LaunchActivity:
                StateManager.UiActivity.Info uiActivityType2 = StateManager.UiActivity.getUiActivityType(stateAction.getUiActivityId());
                if (onActivityResultListener == null){
                    Intents.launchActivity(uiActivityType2.cls, this, stateAction.getEventParameters());
                } else {
                    Intents.launchActivity(uiActivityType2.cls, this, stateAction.getEventParameters(), onActivityResultListener);
                }
                break;
            case LaunchDialog:
                StateManager.UiDialog.getUiDialogType(stateAction.getUiActivityId()).dialogBuilder.build(stateAction.getEventParameters(), this);
                break;
            case ShowWait:
                showProgress();
                break;
            case HideWait:
                hideProgress();
                break;
            case PopAndServerErrorMessage:
                hideProgress();
                EventParameters serverErrorEventParameters = stateAction.getEventParameters();
                ServerError serverErr = (ServerError) serverErrorEventParameters.getObject("Error", ServerError.class);
                simpleAlert(R.string.app_name, formatErrorMessage(serverErr.error.message));
                break;
        }
    }

    private String formatErrorMessage(String message) {
        return message;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (messages == null) {
            Log.d(TAG, "in BaseActivity.onCreate");
            messages = BrokerApplication.getBrokerApplication().getMessages(this);
        }
        HashMap<Integer, OnActivityResultListener> resultListeners = Intents.getResultListeners();
        if (resultListeners.containsKey(requestCode)){
            OnActivityResultListener onActivityResultListener = resultListeners.get(requestCode);
            onActivityResultListener.onActivityResult(data);
        }
    }

    protected void hideProgress() {
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    protected void showProgress() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getString(R.string.waiting));
        }
        progressDialog.show();
    }


    protected Toolbar configToolbarWithoutBackButton() {
        return configToolbar(false);
    }

    protected Toolbar configToolbar() {
        return configToolbar(true);
    }

    @NonNull
    private Toolbar configToolbar(boolean showBackButton) {
        // Initializing Toolbar and setting it as the actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.app_header_vector);
        toolbar.setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(showBackButton);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMessages().appEvent(StateManager.AppEvents.Back);
            }
        });
        return toolbar;
    }

    protected void showGlossaryItem(String itemName){
        glossaryItemName = itemName;
        getMessages().getGlossary();
    }

    @Override
    public void onBackPressed(){
        messages.appEvent(StateManager.AppEvents.Back);
    }

    static public BaseActivity getCurrentActivity() {
        return currentActivity;
    }

    protected void htmlifyTextControl(int id) {
        TextView view = (TextView)findViewById(id);
        view.setText(Html.fromHtml(view.getText().toString()));
        view.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void simpleAlert(int titleId, String message) {
        Resources r = getResources();
        String title = r.getString(titleId);
        simpleAlert(title, message);
    }

    protected void simpleAlert(int titleId, int textId) {
        Resources r = getResources();
        String title = r.getString(titleId);
        String text = r.getString(textId);
        simpleAlert(title, text);
    }

    protected void simpleAlert(String title, String text) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        WebView webView = new WebView(this);
        webView.loadData(text, "text/html", "utf-8");

        alert.setTitle(title)
                .setView(webView)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    protected View.OnClickListener clickForSimpleAlert(final int title, final int text) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpleAlert(title, text);
            }
        };
    }

    protected void installSimpleAlertClickListener(final int id, final int title, final int text) {
        findViewById(id).setOnClickListener(clickForSimpleAlert(title, text));
    }

    public void callPhoneNumber(String phoneNumber){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            messages.appEvent(StateManager.AppEvents.Back);
        }
        return super.onKeyDown(keyCode, event);
    }*/
}
