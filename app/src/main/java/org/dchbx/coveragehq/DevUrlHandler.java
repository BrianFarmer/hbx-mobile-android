package org.dchbx.coveragehq;


import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dchbx.coveragehq.models.Security.SecurityAnswerResponse;
import okhttp3.FormBody;
import okhttp3.HttpUrl;

public class DevUrlHandler extends UrlHandler {
    private static String TAG = "DevUrlHandler";

    public DevUrlHandler(ServerConfiguration serverConfiguration, JsonParser parser){
        super(serverConfiguration, parser);
    }

    public GetParameters getLoginUrlParameters() {
        GetParameters getParameters = new GetParameters();
        getParameters.url = getLoginUrl();
        //getParameters.cookies = new HashMap<>();
        //getParameters.cookies.put("_session_id", nuserverConfiguration.sessionId);

        return getParameters;
    }

    @Override
    public HttpUrl getLoginUrl() {
        HttpUrl.Builder builder = new HttpUrl.Builder();
        HttpUrl.Builder scheme = builder.scheme(serverConfiguration.dataInfo.scheme);
        HttpUrl.Builder host = scheme.host(serverConfiguration.dataInfo.host);
        HttpUrl.Builder builder1 = host.addPathSegments(serverConfiguration.loginPath);
        HttpUrl.Builder port = builder1.port(serverConfiguration.dataInfo.port);
        return port.build();
    }

    @Override
    public HashMap<String, ArrayList<String>> getNeededLoginCookes() {
        HashMap<String, ArrayList<String>> neededCookies = new HashMap<>();
        neededCookies.put("_session_id", new ArrayList<String>());
        return neededCookies;
    }

    @Override
    public PostParameters getLoginPostParameters(String accountName, String password) {
        PostParameters postParameters = new PostParameters();

        postParameters.url = getLoginUrl();
        postParameters.body = new FormBody.Builder()
                .add("utf8", "✓")
                .add("authenticity_token", serverConfiguration.authenticityToken)
                .add("user[login]", accountName)
                .add("user[password]", password)
                .add("user[remember_me]", "0")
                .add("commit", "Sign in")
                .build();
        postParameters.headers = new HashMap<>();
        postParameters.headers.put("Content-Type", "application/x-www-form-urlencoded");
        postParameters.cookies = new HashMap<>();
        postParameters.cookies.put("_session_id", serverConfiguration.sessionId);
        return postParameters;
    }

    @Override
    FormBody getSecurityAnswerFormBody(String securityAnswer) {
        return new FormBody.Builder()
                .add("security_answer", serverConfiguration.securityAnswer)
                .build();
    }

    @Override
    public void processSecurityResponse(SecurityAnswerResponse securityAnswerResponse) {

    }

    @Override
    public String buildSessionCookies() {
        return buildSessionCookies(serverConfiguration.sessionId);
    }


    @Override
    public String buildSessionCookies(String sessionId) {
        return "_session_id=" + sessionId;
    }

    @Override
    public String getSessionCookie(HashMap<String, ArrayList<String>> cookieMap) {
        return cookieMap.get("_session_id").get(0);
    }

    @Override
    public CoverageConnection.LoginResult processLoginReponse(String accountName, String password, Boolean rememberMe, IConnectionHandler.PostResponse response, boolean useFingerprintSensor) throws CoverageException {
        if (response.responseCode == 401){
            return CoverageConnection.LoginResult.Failure;
        }

        if (response == null
            ||response.responseCode <200
            || response.responseCode >= 300) {
            return CoverageConnection.LoginResult.Error;
        }

        serverConfiguration.accountName = accountName;
        serverConfiguration.password = password;
        serverConfiguration.rememberMe = rememberMe;
        serverConfiguration.securityQuestion = "this is a test question.";
        serverConfiguration.securityAnswer = null;
        if (response.cookies.get("_session_id") != null) {
            //serverConfiguration.sessionId = response.cookies.get("_session_id").get(0);
        }
        return CoverageConnection.LoginResult.Success;
    }

    public void processLoginPageReponse(IConnectionHandler.GetReponse getReponse) throws CoverageException {
        serverConfiguration.sessionId = getReponse.cookies.get("_session_id").get(0);
        Pattern pattern = Pattern.compile("<meta name=\\\"csrf-token\\\" content=\\\"([^\"]+)\\\"");
        Matcher matcher = pattern.matcher(getReponse.body);
        String authenticityToken = null;
        if (matcher.find()){
            serverConfiguration.authenticityToken = matcher.group(1);
        } else {
            throw new CoverageException("error getting authenticity token");
        }
        Log.d(TAG, "got session id");

    }
}
