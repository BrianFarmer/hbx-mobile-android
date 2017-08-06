package org.dchbx.coveragehq;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.method.PasswordTransformationMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.dchbx.coveragehq.models.gitaccounts.GitAccounts;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BrokerActivity {
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(LoginActivity.class);

    private static String TAG = "LoginActivity";
    private static final int FINGERPRINT_PERMISSION_REQUEST_CODE = 15;
    private int getLoginErrorCount = 0;

    private enum FingerprintHardwareState {
        NotChecked,
        NoHardware,
        HaveHardware
    }


    // UI references.
    private EditText emailAddress;
    private EditText password;
    private Switch rememberMe;
    private View mProgressView;
    private View mLoginFormView;
    private Spinner urlsSpinner;
    private Spinner accountsSpinner;
    Switch switchEnableFingerprintLogin;
    private ArrayList<String> urls;


    private boolean waitingForFingerprint = false;
    private FingerprintHardwareState fingerprintHardwareState = FingerprintHardwareState.NotChecked;
    private boolean haveLoginInfo = false;
    private boolean lastLoginUsedFingerprint = false;
    static private boolean pastFingerprint;
    private boolean actionKnown = false;



    private CountingIdlingResource idlingResource = new CountingIdlingResource("Login");
    private ArrayList<String> urlLabels;
    private GitAccounts gitAccounts;

    public CountingIdlingResource getIdlingResource() {
        return idlingResource;
    }


//    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(ServiceManager.getServiceManager().enrollConfig().getLoginLayout());
        pastFingerprint = false;

        View configButton = findViewById(R.id.configButton);
        if (BuildVariant.showLoginConfig()){
            if (configButton == null){
                Toast.makeText(this, "Failed to find getServiceManager button", Toast.LENGTH_LONG).show();
            }
            configButton.setVisibility(View.VISIBLE);
            configButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppConfigDialog.build(LoginActivity.this, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                }
            });
        } else {
            if (configButton != null) {
                configButton.setVisibility(View.GONE);
            }
        }
        TextView textViewVersion = (TextView) findViewById(R.id.textViewVersion);
        String version = ServiceManager.getServiceManager().enrollConfig().getVersion();
        textViewVersion.setVisibility(View.GONE);
        if (version != null) {
            try {
                PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                textViewVersion.setVisibility(View.VISIBLE);
                textViewVersion.setText(getString(R.string.app_name) + "-" + version + "-" + packageInfo.versionName);
            } catch (PackageManager.NameNotFoundException e) {
            }

        }

        // Set up the login form.
        if (ServiceManager.getServiceManager().enrollConfig().isGit()){
            accountsSpinner = (Spinner)findViewById(R.id.spinnerAccounts);
            getMessages().getGitAccounts(null);
        } else {

            switchEnableFingerprintLogin = (Switch)findViewById(R.id.switchEnableFingerprintLogin);

            emailAddress = (EditText) findViewById(R.id.editTextAcountName);
            password = (EditText) findViewById(R.id.editTextPassword);
            rememberMe = (Switch) findViewById(R.id.switchRememberMe);
            password.setTypeface(Typeface.DEFAULT_BOLD);
            //password.setTypeface(password.getTypeface(), Typeface.DEFAULT);
            password.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            password.setTransformationMethod(new PasswordTransformationMethod());
            password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                    if (keyEvent == null
                            || keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                        return false;
                    }
                    attemptLogin();
                    return true;
                }
            });
            haveLoginInfo = false;
            getMessages().getLogin();
            getMessages().getFingerprintStatus();
        }

        Button signUp = (Button)findViewById(R.id.signUp);
        if (signUp != null){
            signUp.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ServiceManager.getServiceManager().enrollConfig().isGit()){
                        int selectedItemPosition = accountsSpinner.getSelectedItemPosition();
                        if (selectedItemPosition == 0){
                            return;
                        }
                        selectedItemPosition = selectedItemPosition - 1;
                        GitAccounts.NamedAccountInfo namedAccountInfo = gitAccounts.accountInfoList.get(selectedItemPosition);
                        if (namedAccountInfo.accountInfo.endpoints != null) {
                            getMessages().signUp(namedAccountInfo.accountInfo.endpoints);
                        }
                    } else {
                        getMessages().signUp();
                    }
                }
            });
        }

        Button logInButton = (Button) findViewById(R.id.buttonResume);
        logInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        idlingResource.increment();
    }


    @Override
    public void onBackPressed(){
        System.exit(0);
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG, "!!!!!!!!!!! onResume");

        fingerprintHardwareState = FingerprintHardwareState.NotChecked;
        if (haveLoginInfo) {
            getMessages().getFingerprintStatus();
        }
    }

    private void attemptLogin() {
        if (ServiceManager.getServiceManager().enrollConfig().isGit()){
            if (urlsSpinner == null
                || accountsSpinner == null){
                return;
            }
            long urlItemId = urlsSpinner.getSelectedItemId();
            long accountsItemId = accountsSpinner.getSelectedItemId();
            if (urlItemId == 0
                || accountsItemId == 0) {
                return;
            }
            int accountId = (int)accountsItemId - 1;
            String accountName;
            boolean isBroker;
            GitAccounts.NamedAccountInfo accountInfo = GitAccountUtilities.getAccountInfo(gitAccounts, accountId);
            accountName = accountInfo.name;
            int accountType = GitAccountUtilities.getAccountType(accountInfo.accountInfo);

            RootActivity.loginDone();
            if (switchEnableFingerprintLogin == null){
                getMessages().loginRequest(new Events.LoginRequest(urls.get((int) urlItemId), accountName, false, false));
            } else {
                getMessages().loginRequest(new Events.LoginRequest(urls.get((int) urlItemId), accountName, switchEnableFingerprintLogin.isChecked(), false));
            }
            return;
        }

        showProgress();
        getMessages().loginRequest(new Events.LoginRequest(emailAddress.getText(), password.getText(), rememberMe.isChecked(), switchEnableFingerprintLogin.isChecked()));
    }

    @Override
    protected void showProgress() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getString(R.string.logging_in));
        }
        progressDialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.SignUpResult signUpResult) {
        RootActivity.loginDone();
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetLoginResult loginResult){

        if (loginResult != null)
        {
            if (loginResult.getErrorMessagge() != null
                && getLoginErrorCount != 2
                && !BuildVariant.showLoginConfig()){
                getLoginErrorCount ++;
                if (getLoginErrorCount == 1){
                    // first error just restart
                    getMessages().getLogin();
                    return;
                }
                if (getLoginErrorCount == 2){
                    RootActivity.restartApp(this);
                    getLoginErrorCount = 0;
                    return;
                }
            } else {
                haveLoginInfo = true;
                if (loginResult.useFingerprintSensor()) {
                    lastLoginUsedFingerprint = loginResult.useFingerprintSensor();
                    if (!SystemUtilities.detectNetwork()){
                        NetworkErrorDialog.build(this, R.string.app_title, R.string.network_access_error, null);
                        return;
                    }
                    checkShowFingerprintDialog();
                } else {
                    emailAddress.setText(loginResult.getAccountName());
                    password.setText("");
                    rememberMe.setChecked(loginResult.getRememberMe());
                }
            }
        } else {
            emailAddress.setText("");
            password.setText("");
            rememberMe.setChecked(true);
        }
        if (!SystemUtilities.detectNetwork()){
            NetworkErrorDialog.build(this, R.string.app_title, R.string.network_access_error, null);
            return;
        }
    }

    private void checkShowFingerprintDialog(){
        if (fingerprintHardwareState == FingerprintHardwareState.HaveHardware
            && haveLoginInfo
            && lastLoginUsedFingerprint
            && !waitingForFingerprint
            && !pastFingerprint) {
            FingerprintDialog.build(true, this);
            waitingForFingerprint = true;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(final Events.GitAccounts gitAccounts) {
        this.gitAccounts = gitAccounts.getGitAccounts();

        ArrayList<String> accounts = new ArrayList<>();
        accounts.add("Choose Account");
        accounts.addAll(GitAccountUtilities.getAccountNames(this.gitAccounts));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, accounts);
        accountsSpinner.setAdapter(arrayAdapter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.FingerprintStatus fingerprintStatus){
        Log.d(TAG, "!!!!!!!!!!! doThis(Events.FingerprintStatus)");

        //getMessages().getLogin();
        if (!fingerprintStatus.isHardwareDetected()){
            switchEnableFingerprintLogin.setVisibility(View.GONE);
            fingerprintHardwareState = FingerprintHardwareState.NoHardware;
            return;
        }
        fingerprintHardwareState = FingerprintHardwareState.HaveHardware;
        checkShowFingerprintDialog();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.LoginRequestResult loginRequestResult) {
        hideProgress();

        switch (loginRequestResult.getLoginResult()) {
            case Events.LoginRequestResult.Success:
                Switch switchEnableFingerprintLogin = (Switch)findViewById(R.id.switchEnableFingerprintLogin);
                if (switchEnableFingerprintLogin != null && switchEnableFingerprintLogin.isChecked()){
                    FingerprintDialog.build(false, this);
                } else {
                    RootActivity.loginDone();
                    finish();
                }
                return;
            case Events.LoginRequestResult.Error:
                alertDialog(R.string.generic_error_message, R.string.ok);
                break;
            case Events.LoginRequestResult.Failure:
                hideProgress();
                SpannableString s = new SpannableString(getString(R.string.bad_login_message));
                Linkify.addLinks(s, Linkify.ALL);
                haveLoginInfo = false;
                getMessages().logoutRequest();
                alertDialog(s, R.string.ok);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.FingerprintLoginResult fingerprintLoginResult) {
        hideProgress();

        switch (fingerprintLoginResult.getLoginResult()) {
            case Events.LoginRequestResult.Success:
                Switch switchEnableFingerprintLogin = (Switch)findViewById(R.id.switchEnableFingerprintLogin);
                if (switchEnableFingerprintLogin != null && switchEnableFingerprintLogin.isChecked()){
                    FingerprintDialog.build(false, this);
                } else {
                    RootActivity.loginDone();
                    finish();
                }
                return;
            case Events.LoginRequestResult.Error:
                alertDialog(R.string.generic_error_message, R.string.ok);
                break;
            case Events.LoginRequestResult.Failure:
                hideProgress();
                SpannableString s = new SpannableString(getString(R.string.bad_login_message));
                Linkify.addLinks(s, Linkify.ALL);
                haveLoginInfo = false;
                getMessages().logoutRequest();
                alertDialog(s, R.string.ok);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Error error) {
        hideProgress();
        NetworkErrorDialog.build(this, R.string.app_title, R.string.network_access_error, null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetSecurityAnswer getSecurityAnswer){
        hideProgress();
        if (getSecurityAnswer.question == null){
            Toast.makeText(this, R.string.no_security_question, Toast.LENGTH_LONG);
            return;
        }
        showSecurityDialog(getSecurityAnswer);
    }

    private void showSecurityDialog(Events.GetSecurityAnswer getSecurityAnswer) {
        SecurityQuestionDialog dialog = SecurityQuestionDialog.build(getSecurityAnswer.question);
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
        dialog.show(this.getSupportFragmentManager(), "SecurityQuestionDialog");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.SecurityAnswerResult securityAnswerResult){
        if (securityAnswerResult.getLoginResult() == Events.SecurityAnswerResult.Success) {
            hideProgress();
            Switch switchEnableFingerprintLogin = (Switch)findViewById(R.id.switchEnableFingerprintLogin);
            if (switchEnableFingerprintLogin != null && switchEnableFingerprintLogin.isChecked()){
                FingerprintDialog.build(false, this);
            }
            RootActivity.loginDone();
            finish();
            return;
        } else {
            alertDialog(R.string.bad_login_message, R.string.ok);
        }
    }

    public void securityDialogOkClicked(String securityAnswer) {
        try{
            showProgress();
            getMessages().securityAnswer(securityAnswer);
        } catch (Exception e) {
            Log.e(TAG, "Exception posting SecurityAnswer", e);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.ReloginResult reloginResult) {
        hideProgress();
        switch (reloginResult.getReloginResultEnum()){
            case Success:
                showSecurityDialog(reloginResult.getSecurityQuestion());
                break;
            case Error:
                pastFingerprint = false;
                Log.e(TAG, "loginactivity received ReloginResult w/error");
                alertDialog("An error happend, please login again.", R.string.ok);
                break;
            case Failed:
                pastFingerprint = false;
                alertDialog("Your stored credentials weren't accepted , please login again.", R.string.ok);
                break;
        }
    }

    private void alertDialog(int messageId, int buttonTextId) {
        alertDialog(getString(messageId), buttonTextId);
    }

    private void alertDialog(String message, int buttonTextId){
        AlertDialog alert = new AlertDialog.Builder(this)
            .setTitle(R.string.app_title)
            .setMessage(message)
            .setPositiveButton(buttonTextId, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    private void alertDialog(SpannableString message, int buttonTextId){
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(R.string.app_title)
                .setMessage(message)
                .setPositiveButton(buttonTextId, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void authenticationUpdate(String string, Boolean success) {
        alertDialog(string, R.string.ok);
    }

    public void fingerprintCanceled(){
        getMessages().logoutRequest(true);

        waitingForFingerprint = false;
        haveLoginInfo = false;
        lastLoginUsedFingerprint = false;
        pastFingerprint = false;

    }

    public void authenticated(String accountName, String password) {
        pastFingerprint = true;
        showProgress();
        getMessages().relogin(accountName, password);
    }


    public void authenticated(String encryptedText) {
        RootActivity.loginDone();
        finish();
    }

    private void showSecurityDialog(String securityQuestion){
        SecurityQuestionDialog dialog = SecurityQuestionDialog.build(securityQuestion);
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
        dialog.show(this.getSupportFragmentManager(), "SecurityQuestionDialog");
    }
}
