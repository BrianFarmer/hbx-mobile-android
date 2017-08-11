package org.dchbx.coveragehq;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import org.dchbx.coveragehq.models.Glossary;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    protected ProgressDialog progressDialog;
    protected Messages messages = null;
    private Glossary.GlossaryItem glossaryItem;
    private String glossaryItemName;
    private static BaseActivity currentActivity;

    public Messages getMessages() {
        return messages;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentActivity = this;
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
                finish();
                break;
            case LaunchActivity:
                StateManager.UiActivity.Info uiActivityType = StateManager.UiActivity.getUiActivityType(stateAction.getUiActivityId());
                Intents.launchActivity(uiActivityType.cls, this);
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

    protected void configToolbar() {
        // Initializing Toolbar and setting it as the actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.app_header_vector);
        toolbar.setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMessages().appEvent(StateManager.AppEvents.Back);
            }
        });
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

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            messages.appEvent(StateManager.AppEvents.Back);
        }
        return super.onKeyDown(keyCode, event);
    }*/
}
