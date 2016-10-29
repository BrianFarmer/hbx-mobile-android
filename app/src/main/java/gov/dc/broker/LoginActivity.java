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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

    private ProgressDialog progressDialog;

    private CountingIdlingResource idlingResource = new CountingIdlingResource("Login");


    public CountingIdlingResource getIdlingResource() {
        return idlingResource;
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        // Set up the login form.
        emailAddress = (EditText) findViewById(R.id.editTextAcountName);
        password = (EditText) findViewById(R.id.editTextPassword);
        rememberMe = (Switch)findViewById(R.id.switchRememberMe);
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

        Button logInButton = (Button) findViewById(R.id.buttonLogIn);
        logInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        idlingResource.increment();
        messages.getLogin();
    }

    private void attemptLogin() {
        showProgress();
        messages.loginRequest(new Events.LoginRequest(emailAddress.getText(), password.getText(), rememberMe.isChecked()));
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
    public void doThis(Events.LoginRequestResult loginRequestResult) {
        hideProgress();
        if (loginRequestResult.getLoginResult() == Events.LoginRequestResult.Success){
            BrokerManager.getDefault().setLoggedIn(true);
            finish();
            return;
        }
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
            messages.securityAnswer(securityAnswer);
        } catch (Exception e) {
            Log.e(TAG, "Exception posting SecurityAnswer", e);
        }
    }
}
