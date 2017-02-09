package gov.dc.broker;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.lang.reflect.Type;
import java.util.Timer;
import java.util.TimerTask;

import gov.dc.broker.models.brokeragency.BrokerAgency;
import gov.dc.broker.models.brokeragency.BrokerClient;
import gov.dc.broker.models.employer.Employer;
import gov.dc.broker.models.gitaccounts.GitAccounts;
import gov.dc.broker.models.roster.Roster;
import gov.dc.broker.models.roster.RosterEntry;


public class BrokerWorker extends IntentService {
    private static String TAG = "BrokerWorker";

    static EventBus eventBus;
    private AccountInfo inProgressAccountInfo; // this member is used to store the account info while the user is trying to login.

    //static AccountInfoStorage accountInfoStorage = new SharedPreferencesAccountInfoStorage();

    //static HbxSite.ServerSiteConfig enrollFeatureServerSite = new HbxSite.ServerSiteConfig("http", "ec2-54-234-22-53.compute-1.amazonaws.com", 443);
    //static HbxSite.ServerSiteConfig enrollFeatureServerSite = new HbxSite.ServerSiteConfig("http", "54.224.226.203", 443);

    BuildConfig2 config = BuildConfig2.getConfig();
    private Timer countdownTimer;
    private Timer sessionTimeoutTimer;
    private int countdownTimerTicksLeft;
    private FingerprintManager fingerprintManager;

    public BrokerWorker() {
        super("WorkIntentService");
        Log.d(TAG, "BrokerWorker: In constructor");
    }


    // This causes the background functionality to be initialized.

    @Override
    protected void onHandleIntent(Intent intent) {
        try {

            eventBus = EventBus.getDefault();
            Log.d(TAG, "Calling eventbus.register");
            eventBus.register(this);

            fingerprintManager = FingerprintManager.build(eventBus);

            Log.d(TAG, "back from eventbus.register");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up event bus", e);
        }
    }


    //
    // Gets the test account information from git.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetGitAccounts getGitAccounts) {
        try {
            Log.d(TAG, "Getting git accounts");
            ServerConfiguration serverConfiguration = config.getServerConfiguration();
            UrlHandler urlHandler = config.getUrlHandler();
            String urlRoot = getGitAccounts.getUrlRoot();
            GitAccounts gitAccounts = config.getCoverageConnection().getGitAccounts(urlRoot);
            Log.d(TAG, "got git accounts");
            BrokerWorker.eventBus.post(new Events.GitAccounts(gitAccounts));

        } catch (Exception e) {
            Log.e(TAG, "Exception processing getGitAaccounts", e);
            e.printStackTrace();
            BrokerWorker.eventBus.post(new Events.Error("Error getting git account information"));
        }
    }


    //
    //  login with fingerprint credentials
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.FingerprintLogin fingerprintLogin){
        try {
            ServerConfiguration serverConfiguration = config.getServerConfiguration();
            CoverageConnection.LoginResult result = config.getCoverageConnection().loginAfterFingerprintAuthenticated();
            Log.d(TAG, "LoginRequest: got sessionid");
            switch (result) {
                case Success:
                    ServerConfiguration.UserType userType = config.getCoverageConnection().determineUserType();
                    BrokerWorker.eventBus.post(new Events.LoginRequestResult(Events.LoginRequestResult.Success, userType));
                    updateSessionTimer();
                    return;
                case Failure:
                    BrokerWorker.eventBus.post(new Events.LoginRequestResult(Events.LoginRequestResult.Failure, null));
                    return;
                case Error:
                    BrokerWorker.eventBus.post(new Events.Error("Error logging in"));
                    return;
            }
            BrokerWorker.eventBus.post(new Events.GetSecurityAnswer(serverConfiguration.securityQuestion));
        } catch (Exception e){
            BrokerWorker.eventBus.post(new Events.Error("Error logging in"));
        }
    }


    //
    // Send user name & password to the server.

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.LoginRequest loginRequest) {

        try {
            ServerConfiguration serverConfiguration = config.getServerConfiguration();
            Log.d(TAG, "Received LoginRequest message");

            String accountName = loginRequest.getAccountName().toString();
            String password = loginRequest.getPassword().toString();
            boolean rememberMe = loginRequest.getRememberMe();
            boolean useFingerprintSensor = loginRequest.useFingerprintSensor();
            Log.d(TAG, "LoginRequest: Getting sessionid");
            CoverageConnection.LoginResult result = config.getCoverageConnection().validateUserAndPassword(accountName, password, rememberMe, useFingerprintSensor);
            Log.d(TAG, "LoginRequest: got sessionid");
            switch (result) {
                case Success:
                    ServerConfiguration.UserType userType = config.getCoverageConnection().determineUserType();
                    BrokerWorker.eventBus.post(new Events.LoginRequestResult(Events.LoginRequestResult.Success, userType));
                    updateSessionTimer();
                    return;
                case Failure:
                    BrokerWorker.eventBus.post(new Events.LoginRequestResult(Events.LoginRequestResult.Failure, null));
                    return;
                case Error:
                    BrokerWorker.eventBus.post(new Events.Error("Error logging in"));
                    return;
            }
            BrokerWorker.eventBus.post(new Events.GetSecurityAnswer(serverConfiguration.securityQuestion));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing LoginReqeust", e);
            e.printStackTrace();
            BrokerWorker.eventBus.post(new Events.Error("Error logging in"));
        }
        return;
    }


    //
    // This message sends the security answer to the server.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.SecurityAnswer securityAnswer) {

        try {
            ServerConfiguration serverConfiguration = config.getServerConfiguration();

            Log.d(TAG, "Received SecurityAnswer message");
            String securityAnswerString = securityAnswer.getSecurityAnswer();
            Log.d(TAG, "LoginRequest: Getting sessionid");
            config.getCoverageConnection().checkSecurityAnswer(securityAnswerString);
            Log.d(TAG, "LoginRequest: got sessionid");
            ServerConfiguration.UserType userType = config.getCoverageConnection().determineUserType();
            if (userType == ServerConfiguration.UserType.Unknown) {
                BrokerWorker.eventBus.post(new Events.LoginRequestResult(Events.LoginRequestResult.Failure, userType));
            } else {
                BrokerWorker.eventBus.post(new Events.LoginRequestResult(Events.LoginRequestResult.Success, userType));
            }
            updateSessionTimer();
        } catch (Exception e) {
            Log.e(TAG, "Exception processing SecurityAnswer");
            e.printStackTrace();
            BrokerWorker.eventBus.post(new Events.Error("Error logging in"));
        }
    }


    /**
     * Retrieves the stored login information.
     * <p/>
     * This method handles the event GetLogin which is sent
     * when something wants the current login information.
     * The login information is returned in the event
     * GetLoginRequest.
     *
     * @param getLogin
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetLogin getLogin) {
        try {
            Log.d(TAG, "Received GetLogin message");

            ServerConfiguration serverConfiguration = config.getCoverageConnection().getLogin();
            BrokerWorker.eventBus.post(new Events.GetLoginResult(serverConfiguration.accountName, serverConfiguration.password,
                    serverConfiguration.securityAnswer, serverConfiguration.rememberMe, serverConfiguration.useFingerprintSensor,
                    config.getCoverageConnection().userTypeFromAccountInfo()));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetLogin");
            BrokerWorker.eventBus.post(new Events.Error("Error logging in"));
        }
    }

    //
    // Logs the user out. Mainly this means any information about the user should
    // be cleared.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.LogoutRequest logoutRequest) {
        Log.d(TAG, "Received LogoutRequest message");
        config.getCoverageConnection().logout(logoutRequest.getClearAccount());
        BrokerWorker.eventBus.post(new Events.LoggedOutResult());
    }

    //
    // Get employer detail data.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetEmployer getEmployer) {
        try {
            Log.d(TAG, "Received GetEmployer");

            DateTime now = DateTime.now();
            CoverageConnection coverageConnection = config.getCoverageConnection();
            String employerId = getEmployer.getEmployerId();
            BrokerClient brokerClient = null;
            Employer employer = null;
            if (employerId != null) {
                brokerClient = BrokerUtilities.getBrokerClient(coverageConnection.getBrokerAgency(now), employerId);
                employer = coverageConnection.getEmployer(employerId, now);
            } else {
                employer = coverageConnection.getDefaultEmployer(now);
            }
            BrokerWorker.eventBus.post(new Events.BrokerClient(getEmployer.getId(), brokerClient, employer));
            updateSessionTimer();
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetEmployer");
            BrokerWorker.eventBus.post(new Events.Error("Error getting employer details"));
        }
    }

    //
    // Get the roster data.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetRoster getRoster) {
        try {
            Log.d(TAG, "Received GetRoster");
            DateTime now = DateTime.now();
            String employerId = getRoster.getEmployerId();
            Roster roster;

            if (employerId == null) {
                roster = config.getCoverageConnection().getRoster(now);
            } else {
                roster = config.getCoverageConnection().getRoster(employerId, now);
            }
            BrokerWorker.eventBus.post(new Events.RosterResult(getRoster.getId(), roster));
            updateSessionTimer();
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetEmployer");
            BrokerWorker.eventBus.post(new Events.RosterResult(getRoster.getId(), null));
        }
    }

    //
    // Get the employer information. Could be called get borker details.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetBrokerAgency getBrokerAgency) {
        try {
            Log.d(TAG, "Received GetBrokerAgency message.");
            BrokerAgency brokerAgency = config.getCoverageConnection().getBrokerAgency(DateTime.now());
            BrokerWorker.eventBus.post(new Events.GetBrokerAgencyResult(getBrokerAgency.getId(), brokerAgency));
            updateSessionTimer();
        } catch (Throwable e) {
            Log.e(TAG, "Exception processing GetBrokerAgency");
            BrokerWorker.eventBus.post(new Events.Error("Error getting employer list"));
        }
    }

    //
    // Get the carrier information.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetCarriers getCarriers) {
        try {
            Log.d(TAG, "Received GetCarriers message");
            Carriers carriers = config.getCoverageConnection().getCarriers();
            BrokerWorker.eventBus.post(new Events.Carriers(getCarriers.getId(), carriers));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetCarriers");
            BrokerWorker.eventBus.post(new Events.Error("Error getting carriers"));
        }
    }

    //
    // Get the employee details.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetEmployee getEmployee) {
        try {
            Log.d(TAG, "Received GetEmployee message");

            RosterEntry rosterEntry;
            String employerId = getEmployee.getEmployerId();
            if (employerId == null) {
                rosterEntry = config.getCoverageConnection().getEmployee(getEmployee.getEmployeeId());
            } else {
                rosterEntry = config.getCoverageConnection().getEmployee(employerId, getEmployee.getEmployeeId());
            }
            BrokerWorker.eventBus.post(new Events.Employee(getEmployee.getId(), getEmployee.getEmployeeId(), getEmployee.getEmployerId(), rosterEntry));
            updateSessionTimer();
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetEmployee");
            BrokerWorker.eventBus.post(new Events.Error("Error getting employee"));
        }
    }

    //
    // This function resets the timer that tells the UI when the
    // server session is about to expire. It should be call be
    // every function the connects to the server and the server
    // resets the clock on its side.
    //

    private void updateSessionTimer() {
        if (sessionTimeoutTimer != null) {
            sessionTimeoutTimer.cancel();
        }
        sessionTimeoutTimer = new Timer();
        sessionTimeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                BrokerWorker.eventBus.post(new Events.SessionAboutToTimeout());
                sessionTimeoutTimer.cancel();
                sessionTimeoutTimer = null;
            }
        }, BuildConfig2.getSessionTimeoutSeconds() * 1000);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.StartSessionTimeout startSessionTimeout) {
        countdownTimerTicksLeft = BuildConfig2.getTimeoutCountdownSeconds();
        countdownTimer = new Timer();
        countdownTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                countdownTimerTicksLeft --;
                if (countdownTimerTicksLeft > 0) {
                    BrokerWorker.eventBus.post(new Events.SessionTimeoutCountdownTick(countdownTimerTicksLeft));
                } else {
                     BrokerWorker.eventBus.post(new Events.SessionTimedOut());
                    countdownTimer.cancel();
                }
            }
        }, 1000, 1000);

    }


    //
    // This message tells the server to reset the timer without
    // doing any real work.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.StayLoggedIn stayLoggedIn) {
        boolean success = false;
        try {
            config.getCoverageConnection().stayLoggedIn();
            success = true;
        } catch (Exception e) {

        }
        BrokerWorker.eventBus.post(new Events.StayLoggedInResult(success));
        updateSessionTimer();
    }

    //
    // This message is called when an activity is interested in
    // fingerprint scanner status.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetFingerprintStatus getFingerprintStatus){
        if (getFingerprintStatus.isWatching()){
            FingerprintManager.InitializationResult initializationResult = fingerprintManager.init(getFingerprintStatus);
            switch (initializationResult.requestStatus){
                case NotInitialized:
                    eventBus.post(new Events.FingerprintStatus(false, false, false));
                    break;
                case Success:
                    eventBus.post(new Events.FingerprintStatus(true, initializationResult.enrolledFingerprints, initializationResult.keyguardSecure));
                    break;
                case Error:
                    eventBus.post(new Events.FingerprintStatus("Error initializing fingerprint manager"));
                    break;
            }
        } else {
            fingerprintManager.release();
        }
    }

    //
    // The message is sent to authenticate the users fingerprint.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.AuthenticateFingerprint authenticateFingerprint) {
        fingerprintManager.authenticate(new FingerprintManager.IAuthenticationResult() {
            @Override
            public void error(CharSequence errString) {
                eventBus.post(new Events.FingerprintAuthenticationUpdate(Events.FingerprintStatus.Messages.AuthenticationError, null));
            }

            @Override
            public void help(CharSequence helpString) {
                eventBus.post(new Events.FingerprintAuthenticationUpdate(Events.FingerprintStatus.Messages.AuthenticationError, null));
            }

            @Override
            public void success() {
                eventBus.post(new Events.FingerprintAuthenticationUpdate(Events.FingerprintStatus.Messages.AuthenticationSucceeded, null));
            }

            @Override
            public void failed() {
                eventBus.post(new Events.FingerprintAuthenticationUpdate(Events.FingerprintStatus.Messages.AuthenticationFailed, null));
            }
        });
    }

    //
    // The message is sent to authenticate the users fingerprint.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.Relogin relogin) {
        try {
            ServerConfiguration serverConfiguration = config.getServerConfiguration();
            CoverageConnection.LoginResult loginResult = config.getCoverageConnection().revalidateUserAndPassword();
            Log.d(TAG, "login result: " + loginResult.toString());
            switch (loginResult) {
                case NeedSecurityQuestion:
                    eventBus.post(new Events.ReloginResult(Events.ReloginResult.ReloginResultEnum.Success, serverConfiguration.securityQuestion));
                    break;
                case Failure:
                    eventBus.post(new Events.ReloginResult(Events.ReloginResult.ReloginResultEnum.Failed, null));
                    break;
                case Error:
                    eventBus.post(new Events.ReloginResult(Events.ReloginResult.ReloginResultEnum.Error, null));
                    break;
            }
        } catch (Exception e){
            eventBus.post(new Events.ReloginResult(Events.ReloginResult.ReloginResultEnum.Error, null));
        }
    }
}

class DateTimeDeserializer implements JsonDeserializer<DateTime> {
    public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonPrimitive primitive = json.getAsJsonPrimitive();
        if (primitive.isString()){
            String s = primitive.toString();
            if (s.length() == 0){
                return null;
            }
        }
        return new DateTime(primitive.getAsString());
    }
}

class LocalDateDeserializer implements JsonDeserializer<LocalDate> {
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonPrimitive primitive = json.getAsJsonPrimitive();
        if (primitive.isString()){
            String s = primitive.toString();
            if (s.length() == 0){
                return null;
            }
        }
        return new LocalDate(primitive.getAsString());
    }
}
