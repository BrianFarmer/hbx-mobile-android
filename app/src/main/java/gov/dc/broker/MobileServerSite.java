package gov.dc.broker;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.UUID;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MobileServerSite extends HbxSite {
    private static String TAG = "MobilServerSite";

    private final String HBX_MOBILE_SERVER_URL = "http://hbx-mobile.dchbx.org";
    private final String HBX_LOGIN = HBX_MOBILE_SERVER_URL + "/visits";

    private final MediaType TEXT = MediaType.parse("text");
    static private final MediaType URL_ENCODED = MediaType.parse("application/x-www-form-urlencoded");



    public MobileServerSite(ServerSiteConfig siteConfig){
        super(siteConfig);
    }

    private OkHttpClient client = new OkHttpClient()
        .newBuilder()
        .build();
    OkHttpClient clientDontFollow = new OkHttpClient()
            .newBuilder()
            .followRedirects(false)
            .followSslRedirects(false)
            .build();

    static private class SecurityAnswerResponse {
        public String session_id;
        public String enroll_server;
    }

    static private class LoginResponse {
        public String security_question;
    }



    @Override
    public void Login(AccountInfo accountInfo) throws IOException, Exception {
        try {
            FormBody formBody = new FormBody.Builder()
                    .add("userid", accountInfo.accountName)
                    .add("pass", accountInfo.password)
                    .add("device_id", UUID.randomUUID().toString())
                    .build();

            HttpUrl url = new HttpUrl.Builder()
                    .scheme(siteConfig.scheme)
                    .host(siteConfig.host)
                    .addPathSegments("login")
                    .port(siteConfig.port)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();

            Response response = client.newCall(request)
                    .execute();

            int code = response.code();
            if (code < 200
                    || code > 299
                    || response.header("location", null) == null) {
                throw new Exception("error getting session");
            }
            String responseBody = response.body().string();
            Log.d(TAG, "login repsonse: " + responseBody);

            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            LoginResponse visits = gson.fromJson(responseBody, LoginResponse.class);

            accountInfo.securityQuestion = visits.security_question;
            accountInfo.location = response.header("Location");
        } catch (Throwable t){
            Log.e(TAG, "throwable", t);
            throw t;
        }
    }

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    @Override
    public void checkSecurityAnswer(AccountInfo accountInfo) throws Exception {
        FormBody formBody = new FormBody.Builder()
                .add("security_answer", accountInfo.securityAnswer)
                .build();

        String url;
        if (accountInfo.location.substring(0,4).compareToIgnoreCase("http") == 0){
            url = accountInfo.location;
        } else {
            url = siteConfig.scheme + "://" + siteConfig.host + accountInfo.location;
        }

        Request request = new Request.Builder()
                .url(url)
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

        accountInfo.enrollServer = securityAnswerResponse.enroll_server;
        accountInfo.sessionId = securityAnswerResponse.session_id;

        initEnrollServerInfo(securityAnswerResponse.enroll_server);
    }

    @Override
    public void Logout(Events.LogoutRequest logout) throws IOException {

    }
}
