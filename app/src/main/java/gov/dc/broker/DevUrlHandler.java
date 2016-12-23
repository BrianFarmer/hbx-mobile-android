package gov.dc.broker;


import java.util.ArrayList;
import java.util.HashMap;

import gov.dc.broker.models.Security.SecurityAnswerResponse;
import okhttp3.FormBody;
import okhttp3.HttpUrl;

public class DevUrlHandler extends UrlHandler {
    private static String TAG = "DevUrlHandler";

    public DevUrlHandler(ServerConfiguration serverConfiguration){
        super(serverConfiguration);
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
    public PostParameters getLoginPostParameters(String accountName, String password, String sessionId, String authenticityToken) {
        PostParameters postParameters = new PostParameters();

        postParameters.url = getLoginUrl();
        postParameters.body = new FormBody.Builder()
                .add("utf8", "✓")
                .add("authenticity_token", authenticityToken)
                .add("user[login]", accountName)
                .add("user[password]", password)
                .add("user[remember_me]", "0")
                .add("commit", "Sign in")
                .build();
        postParameters.headers = new HashMap<>();
        postParameters.headers.put("Content-Type", "application/x-www-form-urlencoded");
        postParameters.cookies = new HashMap<>();
        postParameters.cookies.put("_session_id", sessionId);
        return postParameters;
    }

    @Override
    HttpUrl getSecurityAnswerUrl() {
        return null;
        /*return new HttpUrl.Builder()
                .scheme(serverConfiguration.dataInfo.scheme)
                .host(serverConfiguration.dataInfo.host)
                .addPathSegments(serverConfiguration.securityAnswerPath)
                .port(serverConfiguration.dataInfo.port)
                .build();*/
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
    public void processLoginReponse(String accountName, String password, Boolean rememberMe, IConnectionHandler.PostResponse loginPostResponse){
        serverConfiguration.accountName = accountName;
        serverConfiguration.password = password;
        serverConfiguration.rememberMe = rememberMe;
        serverConfiguration.securityQuestion = "this is a test question.";
        serverConfiguration.securityAnswer = null;
        if (loginPostResponse != null){
            serverConfiguration.sessionId = loginPostResponse.cookies.get("_session_id").get(0);
        }
    }
}
