package org.dchbx.coveragehq;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.dchbx.coveragehq.models.brokeragency.BrokerAgency;
import org.dchbx.coveragehq.models.brokeragency.BrokerClient;
import org.dchbx.coveragehq.models.employer.Employer;
import org.dchbx.coveragehq.models.gitaccounts.GitAccounts;
import org.dchbx.coveragehq.models.planshopping.Plan;
import org.dchbx.coveragehq.models.roster.Roster;
import org.dchbx.coveragehq.models.roster.RosterEntry;
import org.dchbx.coveragehq.models.services.Service;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;


/**
 * This class manages all of the GreenRobot background work messages.
 */
public class BrokerWorker extends IntentService {
    private static String TAG = "BrokerWorker";

    static EventBus eventBus;
    private final ServiceManager serviceManager;
    private AccountInfo inProgressAccountInfo; // this member is used to store the account info while the user is trying to login.

    private EnrollConfigBase getConfig() {
        return null;
    }

    private FingerprintManager fingerprintManager;

    public BrokerWorker(ServiceManager serviceManager) {
        super("WorkIntentService");
        this.serviceManager = serviceManager;
        Log.d(TAG, "BrokerWorker: In constructor");
    }


    // This causes the background functionality to be initialized.

    /**
     * @param intent
     */
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


    /**
     * Gets the test account information from git.
     *
     * @param getGitAccounts Get git test data account information. Only used during git build.
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetGitAccounts getGitAccounts) {
        try {
            Log.d(TAG, "Getting git accounts");

            if (!SystemUtilities.detectNetwork()){
                Log.e(TAG, "no network, returning from BrokerWorker");
                BrokerWorker.eventBus.post(new Events.Error("No Networking", null));
                return;
            }

            ServerConfiguration serverConfiguration = serviceManager.getServerConfiguration();
            UrlHandler urlHandler = serviceManager.enrollConfig().getUrlHandler();
            String urlRoot = serverConfiguration.GitAccountsUrl;
            GitAccounts gitAccounts = serviceManager.getCoverageConnection().getGitAccounts(urlRoot);
                Log.d(TAG, "got git accounts");
            BrokerWorker.eventBus.post(new Events.GitAccounts(gitAccounts));

        } catch (Exception e) {
            Log.e(TAG, "Exception processing getGitAaccounts", e);
            e.printStackTrace();
            BrokerWorker.eventBus.post(new Events.Error("Error getting git account information", "GetGitAccounts"));
        }
    }

    /**
     * login with fingerprint credentials
     *
     * @param fingerprintLogin Sent to login with existinig data after fingerprint authorization has happened.
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.FingerprintLogin fingerprintLogin){
        try {
            Log.d(TAG, "In processing Events.FingerprintLogin");

            if (!SystemUtilities.detectNetwork()){
                Log.e(TAG, "no network, returning from BrokerWorker");
                BrokerWorker.eventBus.post(new Events.Error("No Networking", null));
                return;
            }

            ServerConfiguration serverConfiguration = serviceManager.getServerConfiguration();
            CoverageConnection.LoginResult result = serviceManager.getCoverageConnection().loginAfterFingerprintAuthenticated();
            Log.d(TAG, "FingerprintLogin: got sessionid");
            switch (result) {
                case Success:
                    ServerConfiguration.UserType userType = serviceManager.getCoverageConnection().determineUserType();
                    BrokerWorker.eventBus.post(new Events.FingerprintLoginResult(Events.LoginRequestResult.Success, userType));
                    serviceManager.getAppStatusService().updateSessionTimer();
                    return;
                case Failure:
                    BrokerWorker.eventBus.post(new Events.FingerprintLoginResult(Events.LoginRequestResult.Failure));
                    return;
                case Error:
                    BrokerWorker.eventBus.post(new Events.FingerprintLoginResult(Events.FingerprintLoginResult.Error, "Error logging in"));
                    return;
            }
            BrokerWorker.eventBus.post(new Events.GetSecurityAnswer(serverConfiguration.securityQuestion));
        } catch (Exception e){
            BrokerWorker.eventBus.post(new Events.Error("Error logging in", "FingerprintLogin"));
        }
    }

    /**
     * Send user name & password to the server.
     *
     * @param loginRequest
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.LoginRequest loginRequest) {

        try {
            ServerConfiguration serverConfiguration = serviceManager.getServerConfiguration();
            Log.d(TAG, "Received LoginRequest message");

            if (!SystemUtilities.detectNetwork()){
                Log.e(TAG, "no network, returning from BrokerWorker");
                BrokerWorker.eventBus.post(new Events.Error("No Networking", null));
                return;
            }

            String accountName = loginRequest.getAccountName().toString();
            String password = loginRequest.getPassword().toString();
            boolean rememberMe = loginRequest.getRememberMe();
            if (!rememberMe){
                serviceManager.getCoverageConnection().clearStorageHandler.clear();
            }

            boolean useFingerprintSensor = loginRequest.useFingerprintSensor();
            Log.d(TAG, "LoginRequest: Getting sessionid");
            CoverageConnection.LoginResult result = serviceManager.getCoverageConnection().validateUserAndPassword(accountName, password, rememberMe, useFingerprintSensor);
            Log.d(TAG, "LoginRequest: got sessionid");
            switch (result) {
                case Success:
                    Log.i(TAG, "*****Successful login for: " + accountName);
                    ServerConfiguration.UserType userType = serviceManager.getCoverageConnection().determineUserType();
                    BrokerWorker.eventBus.post(new Events.LoginRequestResult(Events.LoginRequestResult.Success, userType));
                    serviceManager.getAppStatusService().updateSessionTimer();
                    return;
                case Failure:
                    Log.i(TAG, "*****Failure to  login for (bad account or pswd): " + accountName);
                    BrokerWorker.eventBus.post(new Events.LoginRequestResult(Events.LoginRequestResult.Failure, null));
                    return;
                case Error:
                    Log.i(TAG, "*****Error trying to  login for: " + accountName);
                    BrokerWorker.eventBus.post(new Events.Error("Error logging in", "Events.LoginRequest"));
                    return;
            }
            BrokerWorker.eventBus.post(new Events.GetSecurityAnswer(serverConfiguration.securityQuestion));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing LoginReqeust", e);
            e.printStackTrace();
            BrokerWorker.eventBus.post(new Events.Error("Error logging in", "Events.LoginRequest"));
        }
        return;
    }


    //
    // This message sends the security answer to the server.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.SecurityAnswer securityAnswer) {

        try {
            ServerConfiguration serverConfiguration = serviceManager.getServerConfiguration();

            Log.d(TAG, "Received SecurityAnswer message");

            if (!SystemUtilities.detectNetwork()){
                Log.e(TAG, "no network, returning from BrokerWorker");
                BrokerWorker.eventBus.post(new Events.Error("No Networking", null));
                return;
            }

            String securityAnswerString = securityAnswer.getSecurityAnswer();
            Log.d(TAG, "LoginRequest: Getting sessionid");
            serviceManager.getCoverageConnection().checkSecurityAnswer(securityAnswerString);
            Log.d(TAG, "LoginRequest: got sessionid");
            ServerConfiguration.UserType userType = serviceManager.getCoverageConnection().determineUserType();
            if (userType == ServerConfiguration.UserType.Unknown) {
                BrokerWorker.eventBus.post(new Events.LoginRequestResult(Events.LoginRequestResult.Failure, userType));
            } else {
                BrokerWorker.eventBus.post(new Events.LoginRequestResult(Events.LoginRequestResult.Success, userType));
            }
            serviceManager.getAppStatusService().updateSessionTimer();
        }  catch (Exception e) {
            Log.e(TAG, "Exception processing SecurityAnswer");
            e.printStackTrace();
            BrokerWorker.eventBus.post(new Events.Error("Error logging in", "Events.SecurityAnswer"));
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

            boolean timedOut = serviceManager.getAppStatusService().timedOut();

            ServerConfiguration serverConfiguration = serviceManager.getCoverageConnection().getLogin();
            AppStatusService appStatusService = serviceManager.getAppStatusService();
            /*
            if (appStatusService.getUserStatus() == SignUpIndividual){
                ImprovedStateManager stateManager = serviceManager.getStateManager();
                stateManager.start();
                return;
            }*/

            BrokerWorker.eventBus.post(new Events.GetLoginResult(serverConfiguration.accountName, serverConfiguration.password,
                    serverConfiguration.securityAnswer, serverConfiguration.rememberMe, serverConfiguration.useFingerprintSensor,
                    serviceManager.getCoverageConnection().userTypeFromAccountInfo(), timedOut));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetLogin");
            BrokerWorker.eventBus.post(new Events.GetLoginResult("Exception in Events.GetLogin", e));
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetCurrentActivity getCurrentActivity) {
        try {
            Log.d(TAG, "Received GetCurrentActivity message");
            BrokerWorker.eventBus.post(new Events.GetCurrentActivityResult(serviceManager.getStateManager().getCurrentActivity()));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetLogin");
            BrokerWorker.eventBus.post(new Events.GetLoginResult("Exception in Events.GetLogin", e));
        }
    }



    //
    // Logs the user out. Mainly this means any information about the user should
    // be cleared.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.LogoutRequest logoutRequest) {
        Log.d(TAG, "Received LogoutRequest message");

        try {
            serviceManager.getCoverageConnection().logout(logoutRequest.getClearAccount());
        } catch (Exception e) {
            e.printStackTrace();
        }
        serviceManager.getAppStatusService().cancelSessionTimer();
        BrokerWorker.eventBus.post(new Events.LoggedOutResult());
    }

    //
    // Get employer detail data.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetEmployer getEmployer) {
        try {
            Log.d(TAG, "Received GetEmployer");

            if (!SystemUtilities.detectNetwork()){
                Log.e(TAG, "no network, returning from BrokerWorker");
                BrokerWorker.eventBus.post(new Events.Error("No Networking", null));
                return;
            }

            DateTime now = DateTime.now();
            CoverageConnection coverageConnection = serviceManager.getCoverageConnection();
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
            serviceManager.getAppStatusService().updateSessionTimer();
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetEmployer");
            BrokerWorker.eventBus.post(new Events.Error("Error getting employer details", "Events.GetEmployer"));
        }
    }

    //
    // Get the roster data.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetRoster getRoster) {
        try {
            Log.d(TAG, "Received GetRoster");

            if (!SystemUtilities.detectNetwork()){
                Log.e(TAG, "no network, returning from BrokerWorker");
                BrokerWorker.eventBus.post(new Events.Error("No Networking", null));
                return;
            }

            DateTime now = DateTime.now();
            String employerId = getRoster.getEmployerId();
            Roster roster;

            if (employerId == null) {
                roster = serviceManager.getCoverageConnection().getRoster(now);
            } else {
                roster = serviceManager.getCoverageConnection().getRoster(employerId, now);
            }
            BrokerWorker.eventBus.post(new Events.RosterResult(getRoster.getId(), roster));
            serviceManager.getAppStatusService().updateSessionTimer();
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

            if (!SystemUtilities.detectNetwork()){
                Log.e(TAG, "no network, returning from BrokerWorker");
                BrokerWorker.eventBus.post(new Events.Error("No Networking", null));
                return;
            }

            BrokerAgency brokerAgency = serviceManager.getCoverageConnection().getBrokerAgency(DateTime.now());
            BrokerWorker.eventBus.post(new Events.GetBrokerAgencyResult(getBrokerAgency.getId(), brokerAgency));
            serviceManager.getAppStatusService().updateSessionTimer();
        } catch (Throwable e) {
            Log.e(TAG, "Exception processing GetBrokerAgency");
            BrokerWorker.eventBus.post(new Events.Error("Error getting employer list", "Events.GetBrokerAgency"));
        }
    }

    //
    // Get the carrier information.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetCarriers getCarriers) {
        try {
            Log.d(TAG, "Received GetCarriers message");

            if (!SystemUtilities.detectNetwork()){
                Log.e(TAG, "no network, returning from BrokerWorker");
                BrokerWorker.eventBus.post(new Events.Error("No Networking", null));
                return;
            }

            Carriers carriers = serviceManager.getCoverageConnection().getCarriers();
            BrokerWorker.eventBus.post(new Events.Carriers(getCarriers.getId(), carriers));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetCarriers");
            BrokerWorker.eventBus.post(new Events.Error("Error getting carriers", "Events.GetCarriers"));
        }
    }

    //
    // Get the employee details.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetEmployee getEmployee) {
        try {
            Log.d(TAG, "Received GetEmployee message");

            if (!SystemUtilities.detectNetwork()){
                Log.e(TAG, "no network, returning from BrokerWorker");
                BrokerWorker.eventBus.post(new Events.Error("No Networking", null));
                return;
            }

            RosterEntry rosterEntry;
            String employerId = getEmployee.getEmployerId();
            if (employerId == null) {
                rosterEntry = serviceManager.getCoverageConnection().getEmployee(getEmployee.getEmployeeId());
            } else {
                rosterEntry = serviceManager.getCoverageConnection().getEmployee(employerId, getEmployee.getEmployeeId());
            }
            BrokerWorker.eventBus.post(new Events.Employee(getEmployee.getId(), getEmployee.getEmployeeId(), getEmployee.getEmployerId(), rosterEntry));
            serviceManager.getAppStatusService().updateSessionTimer();
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetEmployee");
            BrokerWorker.eventBus.post(new Events.Error("Error getting employee", "Events.GetEmployee"));
        }
    }

    //
    // This function resets the timer that tells the UI when the
    // server session is about to expire. It should be call be
    // every function the connects to the server and the server
    // resets the clock on its side.
    //


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.StartSessionTimeout startSessionTimeout) {
        Log.d(TAG, "processing Events.StartSessionTimeout");

        AppStatusService appStatusService = serviceManager.getAppStatusService();
        appStatusService.startSessionTimeout();

    }



    // This message tells the server to reset the timer without
    // doing any real work.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.StayLoggedIn stayLoggedIn) {
        boolean success = false;

        try {
            if (!SystemUtilities.detectNetwork()){
                Log.e(TAG, "no network, returning from BrokerWorker");
                BrokerWorker.eventBus.post(new Events.Error("No Networking", null));
                return;
            }

            serviceManager.getCoverageConnection().stayLoggedIn();
            success = true;
        } catch (Exception e) {

        }
        BrokerWorker.eventBus.post(new Events.StayLoggedInResult(success));
        serviceManager.getAppStatusService().updateSessionTimer();
    }

    //
    // This message is called when an activity is interested in
    // fingerprint scanner status.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetFingerprintStatus getFingerprintStatus){
        Log.d(TAG, "processing Events.GetFingerprintStatus");
        FingerprintManager.DetectFingerprintResult detect = fingerprintManager.detect();
        eventBus.post(new Events.FingerprintStatus(detect.osSupportsFingerprint, detect.hardwarePresent, detect.fingerprintRegistered));
    }

    //
    // The message is sent to authenticate the users fingerprint.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.AuthenticateFingerprintEncrypt authenticateFingerprint) {
        ServerConfiguration serverConfiguration = serviceManager.getServerConfiguration();
        try {
            Log.d(TAG, "processing AuthenticateFingerprintEncrypt");

            if (!SystemUtilities.detectNetwork()){
                Log.e(TAG, "no network, returning from BrokerWorker");
                BrokerWorker.eventBus.post(new Events.Error("No Networking", null));
                return;
            }

            fingerprintManager.authenticate(serverConfiguration.accountName, serverConfiguration.password, new FingerprintManager.IAuthenticationEncryptResult() {
                @Override
                public void error(CharSequence errString) {
                    eventBus.post(new Events.FingerprintAuthenticationEncryptResult(null, null, errString));
                }

                @Override
                public void help(CharSequence helpString) {
                    eventBus.post(new Events.FingerprintAuthenticationEncryptResult(null, helpString, null));
                }

                @Override
                public void success(String encryptedText) {
                    try {
                        ServerConfiguration serverConfiguration = serviceManager.enrollConfig().getServerConfiguration();
                        serverConfiguration.encryptedString = encryptedText;
                        IServerConfigurationStorageHandler serverConfigurationStorageHandler = serviceManager.enrollConfig().getServerConfigurationStorageHandler();
                        serverConfigurationStorageHandler.store(serverConfiguration);
                        serviceManager.getCoverageConnection().saveLoginInfo(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    eventBus.post(new Events.FingerprintAuthenticationEncryptResult(encryptedText, null, null));
                }

                @Override
                public void failed() {
                    eventBus.post(new Events.FingerprintAuthenticationEncryptResult(null, null, null));
                }
            });
        } catch (Exception e) {
            eventBus.post(new Events.FingerprintAuthenticationEncryptResult(null, null, "Exception encrypting"));
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.AuthenticateFingerprintDecrypt authenticateFingerprint) {
        try {
            final ServerConfiguration serverConfiguration = serviceManager.enrollConfig().getServerConfiguration();

            if (!SystemUtilities.detectNetwork()){
                Log.e(TAG, "no network, returning from BrokerWorker");
                BrokerWorker.eventBus.post(new Events.Error("No Networking", null));
                return;
            }

            IServerConfigurationStorageHandler serverConfigurationStorageHandler = serviceManager.enrollConfig().getServerConfigurationStorageHandler();
            serverConfigurationStorageHandler.read(serverConfiguration);

            fingerprintManager.authenticate(serverConfiguration.encryptedString, new FingerprintManager.IAuthenticationDecryptResult() {
                @Override
                public void error(CharSequence errString) {
                    eventBus.post(new Events.FingerprintAuthenticationEncryptResult(null, null, errString));
                }

                @Override
                public void help(CharSequence helpString) {
                    eventBus.post(new Events.FingerprintAuthenticationEncryptResult(null, helpString, null));
                }

                @Override
                public void success(String accountName,  String password) {
                    try {
                        serverConfiguration.accountName = accountName;
                        serverConfiguration.password = password;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    eventBus.post(new Events.FingerprintAuthenticationDecryptResult(accountName, password, null, null));
                }

                @Override
                public void failed() {
                    eventBus.post(new Events.FingerprintAuthenticationEncryptResult(null, null, null));
                }
            });
        } catch (Exception e) {
            eventBus.post(new Events.FingerprintAuthenticationEncryptResult(null, null, "Exception decrypting"));
        }
    }

    //
    // The message is sent to authenticate the users fingerprint.
    //

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.Relogin relogin) {
        try {
            Log.d(TAG, "processing relogin");

            if (!SystemUtilities.detectNetwork()){
                Log.e(TAG, "no network, returning from BrokerWorker");
                BrokerWorker.eventBus.post(new Events.Error("No Networking", null));
                return;
            }

            ServerConfiguration serverConfiguration = serviceManager.getServerConfiguration();
            CoverageConnection.LoginResult loginResult = serviceManager.getCoverageConnection().revalidateUserAndPassword();
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

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.TestTimeout testTimeout) {
        eventBus.post(new Events.TestTimeoutResult(serviceManager.getAppStatusService().testTimeout()));
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetUserEmployee getUserEmployee) {
        eventBus.post(new Events.GetUserEmployeeResults(serviceManager.getCoverageConnection().getUserEmployee()));
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.MoveImageToData moveImageToData){
        try{
            serviceManager.getCoverageConnection().moveImageToData(moveImageToData.isFrontOfCard(), moveImageToData.getUri());
            eventBus.post(new Events.MoveImageToDataResult(true));
        } catch (Exception e){
            eventBus.post(new Events.MoveImageToDataResult(false, e.getMessage()));
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.RemoveInsuraceCardImage removeInsuraceCardImage){
        try{
            serviceManager.getCoverageConnection().removeInsuraceCardImage(removeInsuraceCardImage.isFront());
            eventBus.post(new Events.RemoveInsuraceCardImageResult(true));
        } catch (Exception e){
            eventBus.post(new Events.RemoveInsuraceCardImageResult(false));
        }
    }

    /*
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetInsuredAndBenefits getInsuredAndBenefits) {
        try {
            Log.d(TAG, "Received GetInsuredAndBenefits message");

            if (!SystemUtilities.detectNetwork()){
                Log.e(TAG, "no network, returning from BrokerWorker");
                BrokerWorker.eventBus.post(new Events.Error("No Networking", null));
                return;
            }

            CoverageConnection.InsuredAndSummary insuredAndSummaryOfBenefits = getServiceManager.getCoverageConnection().getInsuredAndSummaryOfBenefits(getInsuredAndBenefits.getCurrentDate());

            BrokerWorker.eventBus.post(new Events.GetInsuredAndSummaryOfBenefitsResult(insuredAndSummaryOfBenefits.getInsured(), insuredAndSummaryOfBenefits.getSummaries()));
            updateSessionTimer();
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetEmployee");
            BrokerWorker.eventBus.post(new Events.Error("Error getting employee", "Events.GetEmployee"));
        }
    }
    */

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetInsuredAndServices getInsuredAndServices) {
        try {
            Log.d(TAG, "Received GetInsuredAndBenefits message");

            if (!SystemUtilities.detectNetwork()){
                Log.e(TAG, "no network, returning from BrokerWorker");
                BrokerWorker.eventBus.post(new Events.Error("No Networking", null));
                return;
            }


            CoverageConnection.InsuredAndServices insuredAndServices = serviceManager.getCoverageConnection().getInsuredAndServices(getInsuredAndServices.getEnrollmentDate());
            if (insuredAndServices == null){
                Log.d(TAG, "getInsuredAndServices returned null!");
                BrokerWorker.eventBus.post(new Events.GetInsuredAndServicesResult(null, null));
            } else {
                Log.d(TAG, "sending GetInsuredAndServicesResult");
                BrokerWorker.eventBus.post(new Events.GetInsuredAndServicesResult(insuredAndServices.getInsured(), insuredAndServices.getServices()));
                Log.d(TAG, "sendt GetInsuredAndServicesResult");
                serviceManager.getAppStatusService().updateSessionTimer();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetEmployee");
            BrokerWorker.eventBus.post(new Events.Error("Error getting employee", "Events.GetEmployee"));
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.SignUp signUp) {
        try {
            serviceManager.getStateManager().process(StateManager.AppEvents.SignUpIndividual);
            serviceManager.getCoverageConnection().configureForSignUp(signUp.getEndPointUrl());
            BrokerWorker.eventBus.post(new Events.SignUpResult());
        } catch (Exception e) {
            Log.e(TAG, "Exception processing SignUp");
            BrokerWorker.eventBus.post(new Events.Error("Error processing SignUp", "Events.SignUp"));
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetPlanShopping getPlanShopping){
        BrokerWorker.eventBus.post(new Events.GetPlanShoppingResult(serviceManager.getCoverageConnection().getPlanShoppingParameters()));
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.ResetPlanShopping resetPlanShopping){
        try {
            serviceManager.getCoverageConnection().configureForSignUp(null);
            BrokerWorker.eventBus.post(new Events.ResetPlanShoppingResult());
        } catch (Exception e) {
            Log.e(TAG, "Exception processing ResetPlanShopping");
            BrokerWorker.eventBus.post(new Events.Error("Error processing ResetPlanShopping", "Events.ResetPlanShopping"));
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.UpdatePlanShopping updatePlanShopping){
        try {
            serviceManager.getCoverageConnection().updatePlanShopping(updatePlanShopping);
        } catch (Exception e) {
            e.printStackTrace();
        }
        BrokerWorker.eventBus.post(new Events.UpdatePlanShoppingResult());
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetPlans getPlans){
        try {
            CoverageConnection coverageConnection = serviceManager.getCoverageConnection();
            List<Plan> plans = coverageConnection.getPlans();
            BrokerWorker.eventBus.post(new Events.GetPlansResult(plans, coverageConnection.getPremiumFilter(), coverageConnection.getDeductibleFilter()));
        } catch (Exception e) {
            Log.e(TAG, "Exception getting plans: " + e.getMessage());
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.SetPlanFilter setPlanFilter) {
        try {
            serviceManager.getCoverageConnection().updatePlanFilters(setPlanFilter.getPremiumFilter(), setPlanFilter.getDeductibleFilter());
        } catch (Exception e) {
            // edit these exceptions since we are doing this asyncronously to the user.
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetPlan getPlan) {
        try {
            Plan plan = serviceManager.getCoverageConnection().getPlan(getPlan.getPlanId());

            List<Service> services = null;
            if (getPlan.isGetSummaryAndBenefits()){
                services = serviceManager.getCoverageConnection().getSummaryForPlan(plan);
            }

            BrokerWorker.eventBus.post(new Events.GetPlanResult(plan, services));
        } catch (Exception e) {
            // edit these exceptions since we are doing this asyncronously to the user.
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetAppConfig getAppConfig) {
        try {
            BrokerWorker.eventBus.post(new Events.GetAppConfigResult(serviceManager.getAppConfig()));
        } catch (Exception e) {
            // edit these exceptions since we are doing this asyncronously to the user.
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.UpdateAppConfig updateAppConfig) {
        serviceManager.update(updateAppConfig.getAppConfig());
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetRidpQuestions getRidpQuestions) {
        try {
            RidpService.QuestionsAndAnswers questions = serviceManager.getRidpService().getRidpQuestions();
            if (questions == null) {
                BrokerWorker.eventBus.post(new Events.GetRidpQuestionsResult(null, null));
                return;
            }
            BrokerWorker.eventBus.post(new Events.GetRidpQuestionsResult(questions.questions, questions.answers));
        } catch (Exception e) {
            Log.e(TAG, "exception get ridp questions: " + e.getMessage());
            // edit these exceptions since we are doing this asyncronously to the user.
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.UpdateAnswers updateAnswers) {
        try {
            serviceManager.getRidpService().updateAnswers(updateAnswers.getAnswers());
        } catch (Exception e) {
            Log.e(TAG, "exception updating answers: " + e.getMessage());
            // edit these exceptions since we are doing this asyncronously to the user.
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.AccountButtonClicked accountButtonClicked) {
        try {
            serviceManager.getConfigurationStorageHandler().store(accountButtonClicked.getAccount());
            if (accountButtonClicked.getAnswers() != null) {
                serviceManager.getConfigurationStorageHandler().store(accountButtonClicked.getAnswers());
            }
            serviceManager.getStateManager().process(accountButtonClicked.getAppEvent());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CoverageException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.ButtonClicked buttonClicked) {
        try {
            serviceManager.getStateManager().process(buttonClicked.getAppEvent());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CoverageException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetCreateAccountInfo getCreateAccountInfo){
        try {
            org.dchbx.coveragehq.models.account.Account account = serviceManager.getRidpService().getCreateAccountInfo();
            BrokerWorker.eventBus.post(new Events.GetCreateAccountInfoResult(account));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing Events.GetCreateAccountInfo");
            BrokerWorker.eventBus.post(new Events.Error("Error processing Events.GetCreateAccountInfo", "Events.GetCreateAccountInfo"));
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetVerificationResponse getVerificationResponse){
        BrokerWorker.eventBus.post(new Events.GetVerificationResponseResponse(serviceManager.getRidpService().getVerificationResponse()));
    }
}

