package gov.dc.broker;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.spec.DESKeySpec;

import gov.dc.broker.models.Security.LoginResponse;
import gov.dc.broker.models.Security.SecurityAnswerResponse;
import gov.dc.broker.models.brokeragency.BrokerAgency;
import gov.dc.broker.models.brokeragency.BrokerClient;
import gov.dc.broker.models.employer.Employer;
import gov.dc.broker.models.roster.Roster;
import gov.dc.broker.models.roster.RosterEntry;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class BrokerWorker extends IntentService {
    private static String TAG = "BrokerWorker";

    static EventBus eventBus;
    private AccountInfo inProgressAccountInfo; // this member is used to store the account info while the user is trying to login.

    static AccountInfoStorage accountInfoStorage = new SharedPreferencesAccountInfoStorage();

    //static HbxSite.ServerSiteConfig enrollFeatureServerSite = new HbxSite.ServerSiteConfig("http", "ec2-54-234-22-53.compute-1.amazonaws.com", 443);
    //static HbxSite.ServerSiteConfig enrollFeatureServerSite = new HbxSite.ServerSiteConfig("http", "54.224.226.203", 443);


    private ConnectionHandler connectionHandler = BuildConfig2.getConnectionHandler();

    private OkHttpClient client = new OkHttpClient()
            .newBuilder()
            .build();
    private OkHttpClient clientDontFollow = new OkHttpClient()
            .newBuilder()
            .followRedirects(false)
            .followSslRedirects(false)
            .build();



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

te
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
            editor.putString("securityanswer", accountInfo.securityAnswer);
            editor.putBoolean(brokerApplication.getString(R.string.shared_preference_remember_me), accountInfo.rememberMe);
            editor.putInt(brokerApplication.getString(R.string.shared_preference_user_type), accountInfo.userType);
            editor.commit();
        }

        @Override
        public AccountInfo getAccountInfo() {
            AccountInfo accountInfo = new AccountInfo();

            BrokerApplication brokerApplication = BrokerApplication.getBrokerApplication();
            SharedPreferences sharedPref = brokerApplication.getSharedPreferences(brokerApplication.getString(R.string.sharedpreferencename), Context.MODE_PRIVATE);
            accountInfo.accountName = sharedPref.getString(brokerApplication.getString(R.string.shared_preference_account_name), null);
            //accountInfo.password = sharedPref.getString(brokerApplication.getString(R.string.shared_preference_password), null);
            accountInfo.securityAnswer = sharedPref.getString("securityanswer", null);
            accountInfo.rememberMe = sharedPref.getBoolean(brokerApplication.getString(R.string.shared_preference_remember_me), true);
            accountInfo.userType = sharedPref.getInt(brokerApplication.getString(R.string.shared_preference_user_type), 0);

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


    // This causes the background functionality to be initialized.

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
            ServerConfiguration serverConfiguration = BuildConfig2.getServerConfiguration();
            Log.d(TAG, "Received LoginRequest message");
            String accountName = loginRequest.getAccountName().toString();
            String password = loginRequest.getPassword().toString();
            Boolean rememberMe = loginRequest.getRememberMe();
            Log.d(TAG,"LoginRequest: Getting sessionid");
            validateUserAndPassword(accountName, password, rememberMe, connectionHandler, serverConfiguration);
            Log.d(TAG,"LoginRequest: got sessionid");
            BrokerWorker.eventBus.post(new Events.GetSecurityAnswer(serverConfiguration.securityQuestion));
        }
        catch (Exception e) {
            Log.e(TAG, "Exception processing LoginReqeust", e);
            e.printStackTrace();
            BrokerWorker.eventBus.post(new Events.Error("Error logging in"));
        }
        return;
    }

    private void validateUserAndPassword(String accountName, String password, Boolean rememberMe,
                                        ConnectionHandler connectionHandler, gov.dc.broker.ServerConfiguration serverConfiguration) throws Exception {
        try {
            HttpUrl loginUrl = connectionHandler.getLoginUrl(serverConfiguration);
            // if getLoginUrl returns null it means there is no security to worry about.
            // main this is here to support data in github.
            if (loginUrl == null){
                connectionHandler.processLoginReponse(accountName, password, rememberMe, null, null, serverConfiguration);
                return;
            }
            FormBody formBody = connectionHandler.getLoginBody(accountName, password, rememberMe, serverConfiguration);

            Request request = new Request.Builder()
                    .url(loginUrl)
                    .post(formBody)
                    .build();

            Response response = client.newCall(request)
                    .execute();

            int code = response.code();
            if (code < 200
                    || code > 299){
                //|| response.header("location", null) == null) {
                throw new Exception("error validing =login");
            }
            String responseBody = response.body().string();
            Log.d(TAG, "login repsonse: " + responseBody);

            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            LoginResponse loginResponse = gson.fromJson(responseBody, LoginResponse.class);

            connectionHandler.processLoginReponse(accountName, password, rememberMe, loginResponse, response.header("location"), serverConfiguration);
        } catch (Throwable t){
            Log.e(TAG, "throwable", t);
            throw t;
        }

    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.SecurityAnswer securityAnswer) {

        try {
            ServerConfiguration serverConfiguration = BuildConfig2.getServerConfiguration();
            Log.d(TAG, "Received SecurityAnswer message");
            String securityAnswerString = securityAnswer.getSecurityAnswer();
            Log.d(TAG,"LoginRequest: Getting sessionid");
            checkSecurityAnswer(serverConfiguration, securityAnswerString);
            Log.d(TAG,"LoginRequest: got sessionid");

            determineUserType(connectionHandler, serverConfiguration);
            BrokerWorker.eventBus.post(new Events.LoginRequestResult(Events.LoginRequestResult.Success));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing SecurityAnswer");
            e.printStackTrace();
            BrokerWorker.eventBus.post(new Events.Error("Error logging in"));
        }
    }

    private Events.GetLoginResult.UserType userTypeFromAccountInfo(gov.dc.broker.ServerConfiguration serverConfiguration){
        if (serverConfiguration.userType == null){
            return Events.GetLoginResult.UserType.Unknown;
        }
        switch (serverConfiguration.userType){
            case Broker:
                return Events.GetLoginResult.UserType.Broker;
            case Employer:
                return Events.GetLoginResult.UserType.Employer;
            case Employee:
                return Events.GetLoginResult.UserType.Employee;
        }
        return Events.GetLoginResult.UserType.Unknown;
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

            ServerConfiguration serverConfiguration = BuildConfig2.getServerConfiguration();
            BrokerWorker.eventBus.post(new Events.GetLoginResult(serverConfiguration.accountName, serverConfiguration.password,
                                                                 serverConfiguration.securityAnswer, serverConfiguration.rememberMe,
                                                                 userTypeFromAccountInfo(serverConfiguration)));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetLogin");
            BrokerWorker.eventBus.post(new Events.Error("Error logging in"));
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.LogoutRequest logoutRequest){
        Log.d(TAG, "Received LogoutRequest message");
        BuildConfig2.logout();
        BrokerWorker.eventBus.post(new Events.LoggedOutResult());
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetEmployer getEmployer) {
        try {
            Log.d(TAG, "Received GetEmployer");
            DateTime now = DateTime.now();
            ServerConfiguration serverConfiguration = BuildConfig2.getServerConfiguration();
            checkSessionId(serverConfiguration);
            IDataCache dataCache = BuildConfig2.getDataCache();

            dataCache.getEmployer(getEmployer.getEmployerId(), now);
            BrokerAgency brokerAgency = dataCache.getBrokerAgency(now);
            String id = getEmployer.getEmployerId();
            BrokerClient brokerClient = BrokerUtilities.getBrokerClient(brokerAgency, id);
            Employer employer = getEmployer(brokerClient.employerDetailsUrl, connectionHandler, serverConfiguration);
            dataCache.store(getEmployer.getEmployerId(), employer, now);
            BrokerWorker.eventBus.post(new Events.BrokerClient(getEmployer.getId(), brokerClient, employer));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetEmployer");
            BrokerWorker.eventBus.post(new Events.Error("Error getting employer details"));
        }
    }

    private Employer getEmployer(String employerRelativeUrl, ConnectionHandler connectionHandler, ServerConfiguration serverConfiguration) throws Exception {
        HttpUrl employerDetailsUrl1 = connectionHandler.getEmployerDetailsUrl(employerRelativeUrl, serverConfiguration);
        String response = getUrl(employerDetailsUrl1, connectionHandler, serverConfiguration);
        return parser.parseEmployerDetails(response);
    }

    private Employer getEmployer(ConnectionHandler connectionHandler, ServerConfiguration serverConfiguration) throws Exception {
        HttpUrl employerDetailsUrl1 = connectionHandler.getEmployerDetailsUrl(serverConfiguration);
        String response = getUrl(employerDetailsUrl1, connectionHandler, serverConfiguration);
        return parser.parseEmployerDetails(response);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetRoster getRoster) {
        try {
            Log.d(TAG, "Received GetRoster");
            ServerConfiguration serverConfiguration = BuildConfig2.getServerConfiguration();
            checkSessionId(serverConfiguration);


            BrokerAgency brokerAgency = BuildConfig2.getDataCache().getBrokerAgency();
            BrokerClient brokerClient = BrokerUtilities.getBrokerClient(brokerAgency, getRoster.getEmployerId());

            Roster roster = getRoster(brokerClient.employeeRosterUrl, BuildConfig2.getConnectionHandler(), serverConfiguration);
            BrokerWorker.eventBus.post(new Events.RosterResult (getRoster.getId(), roster));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetEmployer");
            BrokerWorker.eventBus.post(new Events.Error("Error getting employer details"));
        }
    }

    private Roster getRoster(String employeeRosterUrl, ConnectionHandler connectionHandler, ServerConfiguration serverConfiguration) throws Exception {
        HttpUrl employerRosterUrl = connectionHandler.getEmployerRosterUrl(employeeRosterUrl, serverConfiguration);
        String response = getUrl(employerRosterUrl, connectionHandler, serverConfiguration);
        return parser.parseRoster(response);
    }

    private void checkSessionId(ServerConfiguration serverConfiguration) throws Exception {
        if (BuildConfig2.checkSession(serverConfiguration)){
            return;
        }
        if (serverConfiguration.isPasswordEmpty()){
            sendError();
        }
        try {
            validateUserAndPassword(serverConfiguration.accountName, serverConfiguration.password, serverConfiguration.rememberMe, connectionHandler, serverConfiguration);
        } catch (Exception e){
            serverConfiguration.password = null;
            sendError();
        }
    }

    private void sendLogin(String reason){
        BrokerWorker.eventBus.post(new Events.Finish(reason));
    }

    private void sendError() {
        BrokerWorker.eventBus.post(new Events.Error("Error getting employee"));

    }

    private void checkSecurityAnswer(gov.dc.broker.ServerConfiguration serverConfiguration, String securityAnswer) throws Exception {

        HttpUrl securityAnswerUrl = connectionHandler.getSecurityAnswerUrl(serverConfiguration);
        // returning null means security question answer can be ignored.
        if (securityAnswerUrl == null){
            return;
        }

        FormBody formBody = connectionHandler.getSecurityAnswerFormBody(serverConfiguration, securityAnswer);

        Request request = new Request.Builder()
                .url(securityAnswerUrl)
                .post(formBody)
                .build();

        Response response = clientDontFollow.newCall(request)
                .execute();

        if (response.code() != 200){
            throw new Exception("error getting session");
        }

        String body = response.body().string();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        SecurityAnswerResponse securityAnswerResponse = gson.fromJson(body, SecurityAnswerResponse.class);

        connectionHandler.processSecurityResponse(serverConfiguration, securityAnswerResponse);
        storeAccountInfo(serverConfiguration);
    }

    private void storeAccountInfo(gov.dc.broker.ServerConfiguration serverConfiguration) {
        BuildConfig2.getServerConfigurationStorageHandler().store(serverConfiguration);
    }

    private void determineUserType(ConnectionHandler connectionHandler, gov.dc.broker.ServerConfiguration serverConfiguration) throws Exception {
        try{
            BrokerAgency brokerAgency = getBrokerAgency(connectionHandler, serverConfiguration);
            serverConfiguration.userType = gov.dc.broker.ServerConfiguration.UserType.Broker;
            BuildConfig2.getDataCache().store(brokerAgency, DateTime.now());
            return;
        } catch (Exception e){
            // Eatinng exceptions here is intentional. Failure to get broker object
            // will cause an exception and we then need to try to get an employer.
            Log.d(TAG, "eating exception caused by failture getting broker agency");
        }

        Employer employer = getEmployer(connectionHandler, serverConfiguration);
        BuildConfig2.getDataCache().store(employer, DateTime.now());
        serverConfiguration.userType = gov.dc.broker.ServerConfiguration.UserType.Employer;
        storeAccountInfo(serverConfiguration);
    }

    protected String getUrl(HttpUrl url, ConnectionHandler connectionHandler, gov.dc.broker.ServerConfiguration serverConfiguration) throws Exception {
        Request.Builder builder = new Request.Builder()
                .url(url);

        String cookie = connectionHandler.getSessionCookies(serverConfiguration);
        if (cookie != null) {
            builder = builder.header("cookie", cookie);
        }
        Request request = builder.get().build();
        Response response = clientDontFollow.newCall(request).execute();

        if (response.code() != 200){
            throw new Exception("error getting session");
        }

        return response.body().string();
    }

    private BrokerAgency getBrokerAgency(ConnectionHandler connectionHandler, gov.dc.broker.ServerConfiguration serverConfiguration) throws Exception {
        HttpUrl brokerAgencyUrl = connectionHandler.getBrokerAgencyUrl(serverConfiguration);
        String response = getUrl(brokerAgencyUrl, connectionHandler, serverConfiguration);
        return parser.parseEmployerList(response);
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetEmployerList getEmployerList) {
        try {
            Log.d(TAG, "Received GetBrokerAgency message.");
            ServerConfiguration serverConfiguration = BuildConfig2.getServerConfiguration();
            checkSessionId(serverConfiguration);
            IDataCache dataCache = BuildConfig2.getDataCache();
            BrokerAgency brokerAgency = dataCache.getBrokerAgency(DateTime.now());
            if (brokerAgency == null){
                brokerAgency = getBrokerAgency(BuildConfig2.getConnectionHandler(), serverConfiguration);
                dataCache.store(brokerAgency, DateTime.now());
            }
            BrokerWorker.eventBus.post(new Events.EmployerList (getEmployerList.getId(), brokerAgency));
        }
        catch(Throwable e) {
            Log.e(TAG, "Exception processing GetBrokerAgency");
            BrokerWorker.eventBus.post(new Events.Error("Error getting employer list"));
        }
    }

    @Subscribe(threadMode =  ThreadMode.BACKGROUND)
    public void doThis(Events.GetCarriers getCarriers)
    {
        try {
            Log.d(TAG, "Received GetCarriers message");
            ServerConfiguration serverConfiguration = BuildConfig2.getServerConfiguration();
            checkSessionId(serverConfiguration);
            Carriers carriers = getCarriers(connectionHandler, serverConfiguration);
            BrokerWorker.eventBus.post(new Events.Carriers(getCarriers.getId(), carriers));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetCarriers");
            BrokerWorker.eventBus.post(new Events.Error("Error getting carriers"));
        }
    }

    private Carriers getCarriers(ConnectionHandler connectionHandler, ServerConfiguration serverConfiguration) throws Exception {
        HttpUrl carriersUrl = connectionHandler.getCarriersUrl(serverConfiguration);
        String response = getUrl(carriersUrl, connectionHandler, serverConfiguration);
        return parser.parseCarriers(response);
    }

    @Subscribe(threadMode =  ThreadMode.BACKGROUND)
    public void doThis(Events.GetEmployee getEmployee)
    {
        try {
            Log.d(TAG, "Received GetEmployee message");
            ServerConfiguration serverConfiguration = BuildConfig2.getServerConfiguration();
            checkSessionId(serverConfiguration);
            BrokerAgency brokerAgency = BuildConfig2.getDataCache().getBrokerAgency();
            BrokerClient brokerClient = BrokerUtilities.getBrokerClient(brokerAgency, getEmployee.getEmployerId());
            Roster roster = getRoster(brokerClient.employeeRosterUrl, BuildConfig2.getConnectionHandler(), serverConfiguration);

            DateTime now = DateTime.now();
            for (RosterEntry rosterEntry : roster.roster) {
                if (rosterEntry.id.compareToIgnoreCase(getEmployee.getEmployeeId()) == 0){
                    BrokerWorker.eventBus.post(new Events.Employee(getEmployee.getId(), getEmployee.getEmployeeId(), getEmployee.getEmployerId(),
                                                                   rosterEntry));
                    return;
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