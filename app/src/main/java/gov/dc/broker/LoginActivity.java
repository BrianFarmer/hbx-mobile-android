package gov.dc.broker;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import gov.dc.broker.models.gitaccounts.GitAccounts;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BrokerActivity {

    private static String TAG = "LoginActivity";
    private static final int FINGERPRINT_PERMISSION_REQUEST_CODE = 15;

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
    //private FingerprintDialog fingerprintDialog;

    private ProgressDialog progressDialog;

    private boolean waitingForFingerprint = false;
    private boolean fingerprintHardwarePresent = false;
    private boolean haveLoginInfo = false;
    private boolean lastLoginUsedFingerprint = false;
    static private boolean pastFingerprint = false;


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

        setContentView(BuildConfig2.getLoginLayout());

        // Set up the login form.
        if (BuildConfig2.isGit()){
            urls = BuildConfig2.getUrls();
            urlLabels = BuildConfig2.getUrlLabels();
            urlsSpinner = (Spinner)findViewById(R.id.spinnerUrlRoot);
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, urlLabels);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            urlsSpinner.setAdapter(dataAdapter);
            urlsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (i == 0){
                        return;
                    }
                    getMessages().getGitAccounts(urls.get(i));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            accountsSpinner = (Spinner)findViewById(R.id.spinnerAccounts);
        } else {

            switchEnableFingerprintLogin = (Switch)findViewById(R.id.switchEnableFingerprintLogin);

            emailAddress = (EditText) findViewById(R.id.editTextAcountName);
            password = (EditText) findViewById(R.id.editTextPassword);
            rememberMe = (Switch) findViewById(R.id.switchRememberMe);
            password.setTypeface(Typeface.DEFAULT_BOLD);
            //password.setTypeface(password.getTypeface(), Typeface.DEFAULT);
            password.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
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
        }

        Button logInButton = (Button) findViewById(R.id.buttonLogIn);
        logInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        idlingResource.increment();
    }


    @Override
    public void onResume(){
        super.onResume();
        fingerprintHardwarePresent = false;
        getMessages().getFingerprintStatus(true);
    }

    @Override
    public void onPause(){
        if (fingerprintHardwarePresent) {
            getMessages().getFingerprintStatus(false);
        }
        super.onPause();
    }

    private void attemptLogin() {
        if (BuildConfig2.isGit()){
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
            if (accountId >= gitAccounts.brokers.size()) {
                accountId = accountId - gitAccounts.brokers.size();
                accountName = gitAccounts.employers.get(accountId);
                isBroker = false;
            } else {
                accountName = gitAccounts.brokers.get(accountId);
                isBroker = true;
            }

            RootActivity.loginDone();
            if (switchEnableFingerprintLogin == null){
                getMessages().loginRequest(new Events.LoginRequest(urls.get((int) urlItemId - 1), accountName, isBroker, false));
            } else {
                getMessages().loginRequest(new Events.LoginRequest(urls.get((int) urlItemId - 1), accountName, isBroker, switchEnableFingerprintLogin.isChecked()));
            }
            return;
        }

        showProgress();
        getMessages().loginRequest(new Events.LoginRequest(emailAddress.getText(), password.getText(), rememberMe.isChecked(), switchEnableFingerprintLogin.isChecked()));
    }

    private void showProgress() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getString(R.string.logging_in));
        }
        progressDialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetLoginResult loginResult){
        if (loginResult != null)
        {
            haveLoginInfo = true;
            if (loginResult.useFingerprintSensor()){
                lastLoginUsedFingerprint = loginResult.useFingerprintSensor();
                checkShowFingerprintDialog();
            } else {
                emailAddress.setText(loginResult.getAccountName());
                password.setText(loginResult.getPassword());
                rememberMe.setChecked(loginResult.getRememberMe());
            }
        } else {
            emailAddress.setText("");
            password.setText("");
            rememberMe.setChecked(true);

        }
    }

    private void checkShowFingerprintDialog(){
        if (fingerprintHardwarePresent
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
        accounts.addAll(this.gitAccounts.brokers);
        accounts.addAll(this.gitAccounts.employers);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, accounts);
        accountsSpinner.setAdapter(arrayAdapter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.FingerprintStatus fingerprintStatus){
        //getMessages().getLogin();
        if (!fingerprintStatus.isHardwareDetected()){
            switchEnableFingerprintLogin.setVisibility(View.GONE);
            fingerprintHardwarePresent = false;
            return;
        }

        fingerprintHardwarePresent = true;
        checkShowFingerprintDialog();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.FingerprintAuthenticationUpdate authenticationUpdate) {
        switch (authenticationUpdate.getMessage()){
            case AuthenticationError:
                break;
            case AuthenticationHelp:
                break;
            case AuthenticationFailed:
                break;
            case AuthenticationSucceeded:
                break;
        }
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
                    BrokerManager.getDefault().setLoggedIn(true);
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
        alertDialog(R.string.generic_error_message, R.string.ok);
    }

    private void hideProgress() {
        if (progressDialog != null){
            progressDialog.dismiss();
        }
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

    public void authenticated() {
        if (haveLoginInfo) {
            pastFingerprint = true;
            showProgress();
            getMessages().relogin();
        } else {
            RootActivity.loginDone();
            finish();
        }
    }

    private void showSecurityDialog(String securityQuestion){
        SecurityQuestionDialog dialog = SecurityQuestionDialog.build(securityQuestion);
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
        dialog.show(this.getSupportFragmentManager(), "SecurityQuestionDialog");
    }
}
