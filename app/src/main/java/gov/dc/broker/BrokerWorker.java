package gov.dc.broker;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.spec.DESKeySpec;

import gov.dc.broker.models.roster.Employee;
import gov.dc.broker.models.roster.Roster;


public class BrokerWorker extends IntentService {
    private static String TAG = "BrokerWorker";
    static EventBus eventBus;
    private AccountInfo inProgressAccountInfo; // this member is used to store the account info while the user is trying to login.

    static AccountInfoStorage accountInfoStorage = new SharedPreferencesAccountInfoStorage();

    //static HbxSite.ServerSiteConfig enrollFeatureServerSite = new HbxSite.ServerSiteConfig("http", "ec2-54-234-22-53.compute-1.amazonaws.com", 443);
    static HbxSite.ServerSiteConfig enrollFeatureServerSite = new HbxSite.ServerSiteConfig("http", "54.224.226.203", 443);

    static int gitSite = 0;
    static int backdoorSite = 1;
    static int enrollFeatureBackdoorSite = 2;
    static int enrollFeature = 3;
    static int devTest = 4;

    private final boolean forcedAccount = false; // This is here to protech mistyping accounts & passwords.
    private final String forcedAccountName = "bill.murray@example.com";
    private final String forcedPassword = "Test123!";
    private final String forcedSecurityAnswer = "Test";

    private EmployerList employerList = null;
    private Roster currentRoster = null;

    static Site[] sites = {
            new GitSite(),
            new BackdoorSite(new HbxSite.ServerSiteConfig("http", "ec2-54-234-22-53.compute-1.amazonaws.com", 3001)),
            new BackdoorSite(new HbxSite.ServerSiteConfig("https", "enroll-feature.dchbx.org", 443)),
            new MobileServerSite(new HbxSite.ServerSiteConfig("http", "hbx-mobile.dchbx.org")),
            //new MobileServerSite(new HbxSite.ServerSiteConfig("http", "ec2-54-234-22-53.compute-1.amazonaws.com", 3003))
            new MobileServerSite(new HbxSite.ServerSiteConfig("http", "54.224.226.203", 3003))
    };

    Site currentSite = sites[BuildConfig2.getDataSourceIndex()];
    JsonParser parser = new JsonParser();

    private abstract static class AccountInfoStorage {
        private DESKeySpec keySpec;
        private SecretKey key;

        public abstract void storeAccountInfo(AccountInfo accountInfo) throws KeyStoreException;

        public abstract AccountInfo getAccountInfo();

        public abstract void logout();

        protected AccountInfoStorage() {
            //DESKeySpec keySpec = new DESKeySpec(BrokerApplication.getBrokerApplication().getResources().getString(R.string.dchbx_des_encryption_key).getBytes("UTF8"));
            //SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            //key = keyFactory.generateSecret(keySpec);
        }

        /*protected String decrypt(String string) throws Exception {
            // ENCODE plainTextPassword String
            byte[] cleartext = string.getBytes("UTF8");
            Cipher cipher = Cipher.getInstance("AES"); // cipher is not thread safe
            cipher.init(Cipher.ENCRYPT_MODE,key);
            return Base64.encodeToString(cipher.doFinal(cleartext), Base64.DEFAULT);
        }


        protected String encrypt(String string) throws GeneralSecurityException {
            byte[] encrypedPwdBytes = Base64.decode(string, Base64.DEFAULT);
            Cipher cipher = Cipher.getInstance("AES");// cipher is not thread safe
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] plainTextPwdBytes = (cipher.doFinal(encrypedPwdBytes));
            return new String(plainTextPwdBytes);
        }*/

    }

    private static class FileAccountInfoStorage extends AccountInfoStorage {

        protected FileAccountInfoStorage() throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
        }

        @Override
        public void storeAccountInfo(AccountInfo accountInfo) throws KeyStoreException {

        }

        @Override
        public AccountInfo getAccountInfo() {
            return null;
        }

        @Override
        public void logout() {

        }
    }

    private static class MemoryAccountInfoStorage extends AccountInfoStorage {
        private AccountInfo accountInfo;

        protected MemoryAccountInfoStorage() throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
        }

        @Override
        public void storeAccountInfo(AccountInfo accountInfo) throws KeyStoreException {
            this.accountInfo = accountInfo;
        }

        @Override
        public AccountInfo getAccountInfo() {
            return accountInfo;
        }

        @Override
        public void logout() {
            accountInfo = null;
        }
    }

    private static class SharedPreferencesAccountInfoStorage extends AccountInfoStorage {

        protected SharedPreferencesAccountInfoStorage() {
        }

        @Override
        public void storeAccountInfo(AccountInfo accountInfo) {
            BrokerApplication brokerApplication = BrokerApplication.getBrokerApplication();
            SharedPreferences sharedPref = brokerApplication.getSharedPreferences(brokerApplication.getString(R.string.sharedpreferencename), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(brokerApplication.getString(R.string.shared_preference_account_name), accountInfo.accountName);
            //editor.putString(brokerApplication.getString(R.string.shared_preference_password), accountInfo.password);
            editor.putString(brokerApplication.getString(R.string.shared_preference_session_id), accountInfo.sessionId);
            editor.putString("securityanswer", accountInfo.securityAnswer);
            editor.putBoolean(brokerApplication.getString(R.string.shared_preference_remember_me), accountInfo.rememberMe);
            editor.putString("enrollserver", accountInfo.enrollServer);
            editor.commit();
        }

        @Override
        public AccountInfo getAccountInfo() {
            AccountInfo accountInfo = new AccountInfo();

            BrokerApplication brokerApplication = BrokerApplication.getBrokerApplication();
            SharedPreferences sharedPref = brokerApplication.getSharedPreferences(brokerApplication.getString(R.string.sharedpreferencename), Context.MODE_PRIVATE);
            accountInfo.accountName = sharedPref.getString(brokerApplication.getString(R.string.shared_preference_account_name), null);
            //accountInfo.password = sharedPref.getString(brokerApplication.getString(R.string.shared_preference_password), null);
            accountInfo.sessionId = sharedPref.getString(brokerApplication.getString(R.string.shared_preference_session_id), null);
            accountInfo.securityAnswer = sharedPref.getString("securityanswer", null);
            accountInfo.rememberMe = sharedPref.getBoolean(brokerApplication.getString(R.string.shared_preference_remember_me), true);
            accountInfo.enrollServer = sharedPref.getString("enrollserver", null);

            return accountInfo;
        }

        @Override
        public void logout() {
            BrokerApplication brokerApplication = BrokerApplication.getBrokerApplication();
            SharedPreferences sharedPref = brokerApplication.getSharedPreferences(brokerApplication.getString(R.string.sharedpreferencename), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.commit();
        }
    }

    static class BodyAndSession {
        public String body;
        public String sessionId;
    }

    public BrokerWorker() {
        super("WorkIntentService");
        Log.d(TAG, "BrokerWorker: In constructor");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        try {

        eventBus = EventBus.getDefault();
        Log.d(TAG, "Calling eventbus.register");
        eventBus.register(this);
        Log.d(TAG, "back from eventbus.register");
        }
        catch (Exception e){
            Log.e(TAG, "Error setting up event bus", e);
        }
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.LoginRequest loginRequest) {

        try {
            Log.d(TAG, "Received LoginRequest message");
            AccountInfo accountInfo = new AccountInfo();
            if (forcedAccount) {
                // This is here so accounts and passwords aren't mis-typed when testing against the production
                // which locks up accounts after a couple of bad tries.
                accountInfo.accountName = forcedAccountName;
                accountInfo.password = forcedPassword;
            } else {
                accountInfo.accountName = loginRequest.getAccountName().toString();
                accountInfo.password = loginRequest.getPassword().toString();
            }
            accountInfo.rememberMe = loginRequest.getRememberMe();
            Log.d(TAG,"LoginRequest: Getting sessionid");
            getSessionId(accountInfo);
            inProgressAccountInfo = accountInfo;
            Log.d(TAG,"LoginRequest: got sessionid");
            BrokerWorker.eventBus.post(new Events.GetSecurityAnswer(accountInfo.securityQuestion));
        }
        catch (Exception e) {
            Log.e(TAG, "Exception processing LoginReqeust", e);
            e.printStackTrace();
            BrokerWorker.eventBus.post(new Events.Error("Error logging in"));
        }
        return;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.SecurityAnswer securityAnswer) {

        try {
            Log.d(TAG, "Received SecurityAnswer message");
            if (forcedAccount){
                inProgressAccountInfo.securityAnswer = forcedSecurityAnswer;
            } else {
                inProgressAccountInfo.securityAnswer = securityAnswer.getSecurityAnswer();
            }
            Log.d(TAG,"LoginRequest: Getting sessionid");
            checkSecurityAnswer(inProgressAccountInfo);
            Log.d(TAG,"LoginRequest: got sessionid");
            BrokerWorker.eventBus.post(new Events.LoginRequestResult(1));
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
     * @param getLogin
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetLogin getLogin) {
        try {
            Log.d(TAG, "Received GetLogin message");
            AccountInfo accountInfo = accountInfoStorage.getAccountInfo();
            currentSite.initEnrollServerInfo(accountInfo.enrollServer);
            BrokerWorker.eventBus.post(new Events.GetLoginResult(accountInfo.accountName, accountInfo.password, accountInfo.securityAnswer, accountInfo.rememberMe));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetLogin");
            BrokerWorker.eventBus.post(new Events.Error("Error logging in"));
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.LogoutRequest logoutRequest){
        try {
            Log.d(TAG, "Received LogoutRequest message");
            currentSite.Logout(logoutRequest);
            accountInfoStorage.logout();
            BrokerWorker.eventBus.post(new Events.LoggedOutResult());
        } catch (IOException e) {
            Log.e(TAG, "Exception processing LogoutReqeust", e);
            BrokerWorker.eventBus.post(new Events.Error("Error logging out"));
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetEmployer getEmployer) {
        try {
            Log.d(TAG, "Received GetEmployer");
            checkSessionId();
            BrokerClient brokerClient = employerList.brokerClients.get(getEmployer.getEmployerId());
            String response = currentSite.GetEmployer(getEmployer, brokerClient.employerDetailsUrl, accountInfoStorage.getAccountInfo());
            BrokerWorker.eventBus.post(new Events.BrokerClient (getEmployer.getId(),
                                                                employerList.brokerClients.get(getEmployer.getEmployerId()), parser.parseEmployerDetails(response)));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetEmployer");
            BrokerWorker.eventBus.post(new Events.Error("Error getting employer details"));
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetRoster getRoster) {
        try {
            Log.d(TAG, "Received GetRoster");
            checkSessionId();

            BrokerClient brokerClient = employerList.brokerClients.get(getRoster.getEmployerId());
            String response = currentSite.getRoster(getRoster, brokerClient.employeeRosterUrl, accountInfoStorage.getAccountInfo());
            currentRoster = parser.parseRoster(response);
            BrokerWorker.eventBus.post(new Events.RosterResult (getRoster.getId(), currentRoster));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetEmployer");
            BrokerWorker.eventBus.post(new Events.Error("Error getting employer details"));
        }
    }

    private void checkSessionId() throws Exception {
        AccountInfo accountInfo = accountInfoStorage.getAccountInfo();
        if (accountInfo.sessionId == null || accountInfo.sessionId.length() == 0) {
            getSessionId(accountInfoStorage.getAccountInfo());
        }
    }

    private void getSessionId(AccountInfo accountInfo) throws Exception {
        currentSite.Login(accountInfo);
    }

    private void checkSecurityAnswer(AccountInfo accountInfo) throws Exception {
        currentSite.checkSecurityAnswer(accountInfo);
        accountInfoStorage.storeAccountInfo(accountInfo);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetEmployerList getEmployerList) {
        try {
            Log.d(TAG, "Received GetEmployerList message.");
            checkSessionId();
            String employerResponseString = currentSite.GetEmployerList(getEmployerList, accountInfoStorage.getAccountInfo());
            employerList = parser.parseEmployerList(employerResponseString);
            BrokerWorker.eventBus.post(new Events.EmployerList (getEmployerList.getId(), employerList));
        }
        catch(Throwable e) {
            Log.e(TAG, "Exception processing GetEmployerList");
            BrokerWorker.eventBus.post(new Events.Error("Error getting employer list"));
        }
    }

    @Subscribe(threadMode =  ThreadMode.BACKGROUND)
    public void doThis(Events.GetCarriers getCarriers)
    {
        try {
            Log.d(TAG, "Received GetCarriers message");
            checkSessionId();
            BrokerWorker.eventBus.post(new Events.Carriers(getCarriers.getId(),
                                                           parser.parseCarriers(currentSite.GetCarriers(getCarriers))));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetCarriers");
            BrokerWorker.eventBus.post(new Events.Error("Error getting carriers"));
        }
    }

    @Subscribe(threadMode =  ThreadMode.BACKGROUND)
    public void doThis(Events.GetEmployee getEmployee)
    {
        try {
            Log.d(TAG, "Received GetEmployee message");
            checkSessionId();

            BrokerClient brokerClient = employerList.brokerClients.get(getEmployee.getEmployerId());
            for (Employee employee : currentRoster.roster) {
                if (employee.id == getEmployee.getEmployeeId()){
                    BrokerWorker.eventBus.post(new Events.Employee(getEmployee.getId(), getEmployee.getEmployerId(),
                                                                   employee));
                }
            }
            BrokerWorker.eventBus.post(new Events.Error("Not employee found with that id"));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetEmployee");
            BrokerWorker.eventBus.post(new Events.Error("Error getting employee"));
        }
    }
}


class DateTimeDeserializer implements JsonDeserializer<DateTime> {
    public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        return new DateTime(json.getAsJsonPrimitive().getAsString());
    }
}