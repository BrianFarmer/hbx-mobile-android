package gov.dc.broker;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.method.PasswordTransformationMethod;
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
    private ArrayList<String> urls;

    private ProgressDialog progressDialog;

    private CountingIdlingResource idlingResource = new CountingIdlingResource("Login");
    private ArrayList<String> urlLabels;
    private GitAccounts gitAccounts;


    public CountingIdlingResource getIdlingResource() {
        return idlingResource;
    }


    @TargetApi(Build.VERSION_CODES.M)
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
        }


        Button logInButton = (Button) findViewById(R.id.buttonLogIn);
        logInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        idlingResource.increment();
        if (!BuildConfig2.isGit()) {
            getMessages().getLogin();
        }
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
            if (accountId > gitAccounts.brokers.size()) {
                accountId = accountId - gitAccounts.brokers.size();
                accountName = gitAccounts.employers.get(accountId);
                isBroker = false;
            } else {
                accountName = gitAccounts.brokers.get(accountId);
                isBroker = true;
            }

            getMessages().loginRequest(new Events.LoginRequest(urls.get((int)urlItemId - 1), accountName, isBroker));
            return;
        }

        showProgress();
        getMessages().loginRequest(new Events.LoginRequest(emailAddress.getText(), password.getText(), rememberMe.isChecked()));
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
            emailAddress.setText(loginResult.getAccountName());
            password.setText(loginResult.getPassword());
            rememberMe.setChecked(loginResult.getRememberMe());
        } else {
            emailAddress.setText("");
            password.setText("");
            rememberMe.setChecked(true);

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
    public void doThis(Events.LoginRequestResult loginRequestResult) {
        hideProgress();
        if (loginRequestResult.getLoginResult() == Events.LoginRequestResult.Success){
            BrokerManager.getDefault().setLoggedIn(true);
            finish();
            return;
        }

        // Did the use enter bad info?

        if (loginRequestResult.getLoginResult() == Events.LoginRequestResult.Failure){
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Log In")
                    .setMessage("Incorrect username or password?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        // did an error happen?

        if (loginRequestResult.getLoginResult() == Events.LoginRequestResult.Error){
            hideProgress();
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Log In")
                    .setMessage("An error happened trying to log in. Please try again later.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Error error) {
        hideProgress();
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Log In")
                .setMessage("An error happened trying to log in. Please try again later.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
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
        SecurityQuestionDialog dialog = SecurityQuestionDialog.build(getSecurityAnswer.question);
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
        dialog.show(this.getSupportFragmentManager(), "SecurityQuestionDialog");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.SecurityAnswerResult securityAnswerResult){
        hideProgress();
        finish();
        return;
    }

    public void securityDialogOkClicked(String securityAnswer) {
        try{
            showProgress();
            getMessages().securityAnswer(securityAnswer);
        } catch (Exception e) {
            Log.e(TAG, "Exception posting SecurityAnswer", e);
        }
    }
}
