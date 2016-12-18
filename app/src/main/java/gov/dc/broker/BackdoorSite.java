package gov.dc.broker;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by plast on 10/20/2016.
 */

public class BackdoorSite extends HbxSite {
    private final String TAG = "BackdoorSite";

    static String root = "http://ec2-54-234-22-53.compute-1.amazonaws.com:3001";
    static String home = root;
    static String login = "/users/sign_in";
    private CookieManager cookieManager;
    private String sessionId;
    private boolean haveAccountInfo = false;
    private String accountId;

    private String brokerUrl = "api/v1/mobile_api/employers_list";
    private String employerUrl = "api/v1/mobile_api/employer_details";
    private String employerRosterUrl = "api/v1/mobile_api/employer_roster";

    BackdoorSite(ServerSiteConfig serverSiteConfig){
        super(serverSiteConfig, serverSiteConfig);

        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        cookieManager.getCookieStore().removeAll();
    }

    @Override
    public void Login(AccountInfo accountInfo) throws Exception {
        BrokerWorker.BodyAndSession bodyAndSession = getUrl(login, "");
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


    private BrokerWorker.BodyAndSession getUrl(String path, String sessionId) throws Exception {
        return getUrlHttpUrlConnection(path, sessionId);
    }

    private BrokerWorker.BodyAndSession getUrlOkHttp(String urlString, String sessionId) throws Exception {
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

        BrokerWorker.BodyAndSession bodyAndSession = new BrokerWorker.BodyAndSession();
        bodyAndSession.body = response.body().string();
        Headers headers = response.headers();

        Map<String, List<String>> headerFields = headers.toMultimap();
        bodyAndSession.sessionId = getSessionCookie(response.headers().toMultimap());
        return bodyAndSession;
    }

    private BrokerWorker.BodyAndSession getUrlHttpUrlConnection(String path, String sessionId) {
        URL url = null;
        try {
            String str = siteConfig.scheme + "://" + siteConfig.host + ":" + siteConfig.port + path;

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
            BrokerWorker.BodyAndSession bodyAndSession = new BrokerWorker.BodyAndSession();
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
        private final String TAG = "LoggingInterceptor";

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
        //accountInfo.enrollServer = root;
    }
}
