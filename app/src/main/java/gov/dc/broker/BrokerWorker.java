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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;
import javax.crypto.spec.DESKeySpec;
import javax.net.ssl.HttpsURLConnection;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class BrokerWorker extends IntentService {
    private static String TAG = "BrokerWorker";
    static EventBus eventBus;
    private AccountInfo inProgressAccountInfo; // this member is used to store the account info while the user is trying to login.

    static AccountInfoStorage accountInfoStorage = new SharedPreferencesAccountInfoStorage();

    static HbxSite.ServerSiteConfig enrollFeatureServerSite = new HbxSite.ServerSiteConfig("http", "ec2-54-234-22-53.compute-1.amazonaws.com", 443);

    static GitSite gitSite = new GitSite();
    static BackdoorSite backdoorSite = new BackdoorSite(new HbxSite.ServerSiteConfig("http", "ec2-54-234-22-53.compute-1.amazonaws.com", 3001));
    static BackdoorSite enrollFeatureBackdoorSite = new BackdoorSite(new HbxSite.ServerSiteConfig("https", "enroll-feature.dchbx.org", 443));
    static MobileServerSite enrollFeature = new MobileServerSite(new HbxSite.ServerSiteConfig("http", "hbx-mobile.dchbx.org"));
    static MobileServerSite devTest = new MobileServerSite(new HbxSite.ServerSiteConfig("http", "ec2-54-234-22-53.compute-1.amazonaws.com", 3003));

    private final boolean forcedAccount = false; // This is here to protech mistyping accounts & passwords.
    private final String forcedAccountName = "bill.murray@example.com";
    private final String forcedPassword = "Test123!";
    private final String forcedSecurityAnswer = "Test";
    private EmployerList employerList = null;

    static Site[] sites = {
            gitSite,
            backdoorSite,
            enrollFeatureBackdoorSite,
            enrollFeature,
            devTest
    };

    Site currentSite = sites[4];
    Parser parser = new Parser();




    static private class Parser {
        public gov.dc.broker.EmployerList parseEmployerList(String string){
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Date.class, new DateTimeDeserializer());
            Gson gson = gsonBuilder.create();
            return gson.fromJson(string, EmployerList.class);
        }

        public BrokerClientDetails parseEmployerDetails(String s){
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Date.class, new DateTimeDeserializer());
            Gson gson = gsonBuilder.create();
            return gson.fromJson(s, BrokerClientDetails.class);
        }

        public Carriers parseCarriers(String s) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Date.class, new DateTimeDeserializer());
            Gson gson = gsonBuilder.create();
            return gson.fromJson(s, Carriers.class);
        }
    }

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
            editor.putString(brokerApplication.getString(R.string.shared_preference_password), accountInfo.password);
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
            accountInfo.password = sharedPref.getString(brokerApplication.getString(R.string.shared_preference_password), null);
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

    static private class BackdoorSite extends HbxSite {
        static String root = "http://ec2-54-234-22-53.compute-1.amazonaws.com:3001";
        static String home = root;
        static String login = root + "/users/sign_in";
        private CookieManager cookieManager;
        private String sessionId;
        private boolean haveAccountInfo = false;
        private String accountId;

        BackdoorSite(ServerSiteConfig serverSiteConfig){
            super(serverSiteConfig, serverSiteConfig);

            cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            cookieManager.getCookieStore().removeAll();
        }

        @Override
        public void Login(AccountInfo accountInfo) throws Exception {

            BodyAndSession bodyAndSession = getUrl(login, "");
            if (bodyAndSession == null){
                return;
            }
            String getResponseBody = bodyAndSession.body;
            // Get the login form
            Pattern pattern = Pattern.compile("<meta name=\\\"csrf-token\\\" content=\\\"([^\"]+)\\\"");
            Matcher matcher = pattern.matcher(getResponseBody);
            String authenticityToken = GetCarriers(null);
            if (matcher.find()){
                authenticityToken = matcher.group(1);
            } else {
                Events.LoginRequestResult result = new Events.LoginRequestResult(Events.LoginRequestResult.Error);
                BrokerWorker.eventBus.post(result);
            }
            Log.d(TAG, "got session id");

            // post the login form.

            String sessionId = loginPost(bodyAndSession.sessionId, accountInfo, authenticityToken);
            Log.d(TAG, "new session id:" + sessionId);
            accountInfo.sessionId = sessionId;
            accountInfo.securityQuestion = "this is a test question.";
            accountInfo.securityAnswer = null;
        }


        private BodyAndSession getUrl(String urlString, String sessionId) throws Exception {
            return getUrlHttpUrlConnection(urlString, sessionId);
        }

        private BodyAndSession getUrlOkHttp(String urlString, String sessionId) throws Exception {
            HttpUrl url = new HttpUrl.Builder()
                    .scheme(siteConfig.scheme)
                    .host(siteConfig.host)
                    .port(siteConfig.port)
                    .addPathSegment("users")
                    .addPathSegment("sign_in")
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();


            int responseCode;
            responseCode = response.code();

            BodyAndSession bodyAndSession = new BodyAndSession();
            bodyAndSession.body = response.body().string();
            Headers headers = response.headers();

            Map<String, List<String>> headerFields = headers.toMultimap();
            bodyAndSession.sessionId = getSessionCookie(response.headers().toMultimap());
            return bodyAndSession;
        }

        private BodyAndSession getUrlHttpUrlConnection(String urlString, String sessionId) {
            URL url = null;
            try {
                String str = siteConfig.scheme + "://" + siteConfig.host + ":" + siteConfig.port + "/users/sign_in";

                url = new URL(str);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                if (siteConfig.isSchemeHttps()){
                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) connection;
                    httpsURLConnection.setSSLSocketFactory(getSSLContext().getSocketFactory());
                }
                //dumpHeaders(connection);
                connection.setRequestMethod("GET");
                //connection.setRequestProperty("Cookie", "_session_id=" + sessionId);

                int responseCode;
                try{
                    dumpHeaders(connection);
                    responseCode = connection.getResponseCode();
                } catch (IOException ioException){
                    Log.e(TAG, "trying to connect to backdoor login server", ioException);
                    throw ioException;
                }


                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                BodyAndSession bodyAndSession = new BodyAndSession();
                bodyAndSession.body = response.toString();
                Map<String, List<String>> headerFields = connection.getHeaderFields();
                List<String> strings = headerFields.get("Set-Cookie");
                bodyAndSession.sessionId = getSessionCookie(connection);
                return bodyAndSession;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    String getSessionCookie(HttpURLConnection connection) throws Exception {
        return getSessionCookie(connection.getHeaderFields());
    }

    String getSessionCookie(Map<String, List<String>> headerFields) throws Exception {
            List<String> strings = headerFields.get("Set-Cookie");
            for (String cookie : strings) {
                String[] split = cookie.split(Pattern.quote("="));
                if (split[0].equals("_session_id")){
                    String[] split1 = split[1].split(Pattern.quote(";"));
                    return split1[0];
                }
            }
            throw new Exception("Session cookie not found");
        }

        private void dumpHeaders(HttpURLConnection connection){
            String fullName = connection.getClass().getCanonicalName();
            Log.d(TAG, fullName);
            Map<String, List<String>> headerFields = connection.getHeaderFields();
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
                for (String value : entry.getValue()) {
                    builder.append(entry.getKey());
                    builder.append(": ");
                    builder.append(value);
                    builder.append("\r\n");
                }
            }
            String headers = builder.toString();
            Log.d(TAG, "headers:" );
            Log.d(TAG, headers);
        }


        static class LoggingInterceptor implements Interceptor {
            @Override public Response intercept(Interceptor.Chain chain) throws IOException {
                try {

                    Request request = chain.request();

                    Log.d(TAG, "Sending request: " + request.url());
                    Log.d(TAG, "connection: " + chain.connection());
                    Log.d(TAG, "Headers: " + request.headers());

                    long t1 = System.nanoTime();
                    Response response = chain.proceed(request);

                    long t2 = System.nanoTime();
                    Log.d(TAG, String.format("Received response for %s in %.1fms%n%s",
                            response.request().url(), (t2 - t1) / 1e6d, response.headers()));
                    return response;
                } catch (Throwable e){
                    Log.e(TAG, "logging", e);
                    throw e;
                }
            }
        }

        static private final MediaType URL_ENCODED = MediaType.parse("application/x-www-form-urlencoded");

        private String loginPostOkHttp(String sessionId, AccountInfo accountInfo, String authenticityToken) throws Exception {
            RequestBody requestBody = new FormBody.Builder()
                    .add("utf8", "✓")
                    .add("authenticity_token", authenticityToken)
                    .add("user[login]", accountInfo.accountName)
                    .add("user[password]", accountInfo.password)
                    .add("user[remember_me]", "0")
                    .add("commit", "Sign in")
                    .build();
            HttpUrl url;
            if (siteConfig.scheme == "https")
            {
                url = new HttpUrl.Builder()
                        .scheme(siteConfig.scheme)
                        .host(siteConfig.host)
                        .addPathSegment("users")
                        .addPathSegment("sign_in")
                        .build();
            } else {
                url = new HttpUrl.Builder()
                        .scheme(siteConfig.scheme)
                        .host(siteConfig.host)
                        .port(siteConfig.port)
                        .addPathSegment("users/sign_in")
                        .build();
            }
            Request request = new Request.Builder()
                    .url(url)
                    .header("Host", siteConfig.host + siteConfig.port)
                    .header("Connection", "keep-alive")
                    .header("Pragma", "no-cache")
                    .header("Cache-Control", "no-cache")
                    .header("Origin", siteConfig.scheme + "://" + siteConfig.host + siteConfig.port)
                    .header("Cookie", "_session_id=" + sessionId)
                    .header("DNT", "1")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "en-US,en;q=0.8")
                    .header("Referer", siteConfig.scheme + "://" + siteConfig.host + siteConfig.port + "/users/sign_in")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36")
                    .post(requestBody)
                    .build();
            Log.d(TAG, "account: " + accountInfo.accountName);
            Log.d(TAG, "password: ->" + accountInfo.password + "<-");
            Log.d(TAG, "password length: " + accountInfo.password.length());
            Log.d(TAG, "sending login POST");
            Response response;
            try{
                response = clientDontFollow.newCall(request).execute();
            } catch (Throwable e)
            {
                Log.d(TAG, "doing call", e);
                throw e;
            }
            String string = response.body().string();
            Log.d (TAG, "body: " + string);
            int responseCode = response.code();
            Log.d(TAG, "response code;" + responseCode);
            if (responseCode != 302){
                String body = response.body().string();
                Log.d(TAG, body);
                throw new Exception("Bad email or password");
            }

            String newSessionId = getSessionCookie(response.headers().toMultimap());
            Log.d(TAG, "SessionId for processing: " + newSessionId);
            return newSessionId;
        }

        private String getQuery(HashMap<String, String> params) throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();
            boolean first = true;

            for (Map.Entry<String, String> pair : params.entrySet())
            {
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(pair.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
            }

            return result.toString();
        }

        private String loginPost(String sessionId, AccountInfo accountInfo, String authenticityToken) throws Exception {
            //return loginPostOkHttp(sessionId, accountInfo, authenticityToken);
            return loginPostHttpUrlConnection(sessionId, accountInfo, authenticityToken);
        }

        private String loginPostHttpUrlConnection(String sessionId, AccountInfo accountInfo, String authenticityToken) throws Exception {
            URL url = new URL(siteConfig.scheme, siteConfig.host, siteConfig.port, "users/sign_in");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            if (siteConfig.isSchemeHttps()){
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection)connection;
                httpsURLConnection.setSSLSocketFactory(getSSLContext().getSocketFactory());
            }
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");

            //connection.setRequestProperty("Host", siteConfig.host + siteConfig.port);
            //connection.setRequestProperty("Connection", "keep-alive");
            //connection.setRequestProperty("Pragma", "no-cache");
            //connection.setRequestProperty("Cache-Control", "no-cache");
            //connection.setRequestProperty("Origin", siteConfig.scheme + "://" + siteConfig.host + siteConfig.port);
            connection.setRequestProperty("Cookie", "_session_id=" + sessionId);
            //connection.setRequestProperty("DNT", "1");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            //connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            //connection.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            //connection.setRequestProperty("Referer", siteConfig.scheme + "://" + siteConfig.host + siteConfig.port + "/users/sign_in");
            //connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
            //connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36");

            HashMap<String, String> params = new HashMap<>();
            params.put("utf8", "✓");
            params.put("authenticity_token", authenticityToken);
            params.put("user[login]", accountInfo.accountName);
            params.put("user[password]", accountInfo.password);
            params.put("user[remember_me]", "0");
            params.put("commit", "Sign in");
            String output = getQuery(params);
            //connection.setRequestProperty("Content-Length", Integer.toString(output.length()));

            OutputStream os;
            try {
                os = connection.getOutputStream();
            } catch (Throwable t){
                Log.e(TAG, "getting output stream", t);
                throw t;
            }
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(output);
            writer.flush();
            writer.close();
            os.close();



            Log.d(TAG, "account: " + accountInfo.accountName);
            Log.d(TAG, "password: ->" + accountInfo.password + "<-");
            Log.d(TAG, "password length: " + accountInfo.password.length());
            Log.d(TAG, "sending login POST");

            connection.connect();
            int responseCode = connection.getResponseCode();

            InputStream inputStream = connection.getInputStream();
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(streamReader);
            StringBuilder result = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }

            String string = result.toString();
            Log.d (TAG, "body: " + string);

            Log.d(TAG, "response code;" + responseCode);
            if (responseCode != 302){
                throw new Exception("Bad email or password");
            }

            String newSessionId = getSessionCookie(connection.getHeaderFields());
            Log.d(TAG, "SessionId for processing: " + newSessionId);
            return newSessionId;
        }

        @Override
        public void Logout(Events.LogoutRequest logout) throws IOException {
            haveAccountInfo = false;
            BrokerWorker.eventBus.post(new Events.LoggedOutResult());
        }


        @Override
        public void checkSecurityAnswer(AccountInfo accountInfo) {
            accountInfo.enrollServer = root;

        }
    }

    static private class GitSite extends Site{
        private final String TAG = "GitSite";
        private static EmployerList employerList;
        Parser parser = new Parser();

        String employersList = "https://raw.githubusercontent.com/dchealthlink/HBX-mobile-app-APIs/master/enroll/broker/employers_list/response/example.json";

        public GitSite(){
            Log.d(TAG, "GitSite: In GitSite.GitSite");
        }

        @Override
        public void Login(AccountInfo accountInfo) {
            accountInfo.sessionId = "sessionid";
            accountInfo.securityQuestion = "This is a test question?";
        }

        @Override
        public void Logout(Events.LogoutRequest logout) {
        }

        @Override
        public String GetEmployerList(Events.GetEmployerList getEmployerList, AccountInfo accountInfo) throws IOException{
            return getUrl(employersList);
        }

        @Override
        public String GetEmployer(Events.GetEmployer getEmployer, String url, AccountInfo accountInfo) throws IOException{
            return getUrl(url);
        }

        @Override
        public String GetCarriers(Events.GetCarriers getCarriers) throws IOException{
            return getUrl("https://dchealthlink.com/shared/json/carriers.json");
        }

        @Override
        public void checkSecurityAnswer(AccountInfo accountInfo) {

        }
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
        if (forcedAccount){
            // This is here so accounts and passwords aren't mis-typed when testing against the production
            // which locks up accounts after a couple of bad tries.
            try {
                AccountInfo accountInfo = new AccountInfo();
                accountInfo.accountName = forcedAccountName;
                accountInfo.password = forcedPassword;
                accountInfo.rememberMe = loginRequest.getRememberMe();
                Log.d(TAG,"LoginRequest: Getting sessionid");
                getSessionId(accountInfo);
                inProgressAccountInfo = accountInfo;
                Log.d(TAG,"LoginRequest: got sessionid");
                BrokerWorker.eventBus.post(new Events.GetSecurityAnswer(accountInfo.securityQuestion));
            }
            catch (Exception e) {
                e.printStackTrace();
                BrokerWorker.eventBus.post(new Events.Error("Error logging in"));
            }
            catch (Throwable t){
                t.printStackTrace();
                BrokerWorker.eventBus.post(new Events.Error("Error logging in"));
            }
            return;
        } else {
            try {
                AccountInfo accountInfo = new AccountInfo();
                accountInfo.accountName = loginRequest.getAccountName().toString();
                accountInfo.password = loginRequest.getPassword().toString();
                accountInfo.rememberMe = loginRequest.getRememberMe();
                Log.d(TAG, "LoginRequest: Getting sessionid");
                getSessionId(accountInfo);
                inProgressAccountInfo = accountInfo;
                Log.d(TAG, "LoginRequest: got sessionid");
                BrokerWorker.eventBus.post(new Events.GetSecurityAnswer(accountInfo.securityQuestion));
            } catch (Exception e) {
                e.printStackTrace();
                BrokerWorker.eventBus.post(new Events.Error("Error logging in"));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.SecurityAnswer securityAnswer) {

        try {
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
            e.printStackTrace();
            BrokerWorker.eventBus.post(new Events.Error("Error logging in"));
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetLogin getLogin) {
        try {
            AccountInfo accountInfo = accountInfoStorage.getAccountInfo();
            currentSite.initEnrollServerInfo(accountInfo.enrollServer);
            BrokerWorker.eventBus.post(new Events.GetLoginResult(accountInfo.accountName, accountInfo.password, accountInfo.securityAnswer, accountInfo.rememberMe));
        } catch (Exception e) {
            e.printStackTrace();
            BrokerWorker.eventBus.post(new Events.Error("Error logging in"));
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.LogoutRequest logoutRequest){
        try {
            currentSite.Logout(logoutRequest);
            accountInfoStorage.logout();
            BrokerWorker.eventBus.post(new Events.LoggedOutResult());
        } catch (IOException e) {
            e.printStackTrace();
            BrokerWorker.eventBus.post(new Events.Error("Error logging out"));
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetEmployer getEmployer) {
        try {
            checkSessionId();
            BrokerClient brokerClient = employerList.brokerClients.get(getEmployer.getEmployerId());
            String response = currentSite.GetEmployer(getEmployer, brokerClient.employerDetailsUrl, accountInfoStorage.getAccountInfo());
            BrokerWorker.eventBus.post(new Events.BrokerClient (getEmployer.getId(), employerList.brokerClients.get(getEmployer.getEmployerId()), parser.parseEmployerDetails(response)));
        } catch (Exception e) {
            e.printStackTrace();
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
            checkSessionId();
            String employerResponseString = currentSite.GetEmployerList(getEmployerList, accountInfoStorage.getAccountInfo());
            employerList = parser.parseEmployerList(employerResponseString);
        }
        catch(Exception e) {
            e.printStackTrace();
            BrokerWorker.eventBus.post(new Events.Error("Error getting employer lilst"));
        }
        BrokerWorker.eventBus.post(new Events.EmployerList (getEmployerList.getId(), employerList));
    }

    @Subscribe(threadMode =  ThreadMode.BACKGROUND)
    public void doThis(Events.GetCarriers getCarriers)
    {
        try {
            checkSessionId();
            BrokerWorker.eventBus.post(new Events.Carriers(getCarriers.getId(),
                                                           parser.parseCarriers(currentSite.GetCarriers(getCarriers))));
        } catch (Exception e) {
            e.printStackTrace();
            BrokerWorker.eventBus.post(new Events.Error("Error getting carriers"));
        }
    }
}


class DateTimeDeserializer implements JsonDeserializer<Date> {
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        String dateString = json.getAsJsonPrimitive().getAsString();
        String[] parts = dateString.split("-");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));

        return calendar.getTime();
    }
}