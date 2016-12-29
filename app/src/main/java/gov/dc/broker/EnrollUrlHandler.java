package gov.dc.broker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import gov.dc.broker.models.Security.LoginResponse;
import gov.dc.broker.models.Security.SecurityAnswerResponse;
import okhttp3.FormBody;
import okhttp3.HttpUrl;

public class EnrollUrlHandler extends UrlHandler {

    private final JsonParser parser;

    public EnrollUrlHandler(ServerConfiguration serverConfiguration, JsonParser parser){
        super(serverConfiguration);
        this.parser = parser;
    }
    @Override
    public HttpUrl getLoginUrl() {
        return new HttpUrl.Builder()
                .scheme(serverConfiguration.loginInfo.scheme)
                .host(serverConfiguration.loginInfo.host)
                .addPathSegments(serverConfiguration.loginPath)
                .port(serverConfiguration.loginInfo.port)
                .build();
    }

    @Override
    public PutParameters getSecurityAnswerPutParameters(String securityAnswer) {
        PutParameters putParameters = new PutParameters();

        putParameters.url = new HttpUrl.Builder()
                .scheme(serverConfiguration.loginInfo.scheme)
                .host(serverConfiguration.loginInfo.host)
                .addPathSegments(serverConfiguration.securityAnswerPath)
                .port(serverConfiguration.loginInfo.port)
                .build();


        new HttpUrl.Builder()
                .scheme(serverConfiguration.loginInfo.scheme)
                .host(serverConfiguration.loginInfo.host)
                .addPathSegments(serverConfiguration.securityAnswerPath)
                .port(serverConfiguration.loginInfo.port)
                .build();


        putParameters.body = new FormBody.Builder()
                .add("security_answer", securityAnswer)
                .build();


        return putParameters;
    }

    @Override
    public void processSecurityAnswerResponse(IConnectionHandler.PutResponse putResponse) throws CoverageException {
        if (putResponse.responseCode >= 300
            || putResponse.responseCode < 200){
            throw new CoverageException("Security answer not accepted");
        }
        SecurityAnswerResponse securityAnswerResponse = parser.parseSecurityAnswerResponse(putResponse.body);
        serverConfiguration.enrollServer = securityAnswerResponse.enroll_server;
        serverConfiguration.sessionId = securityAnswerResponse.session_id;
    }

    @Override
    FormBody getSecurityAnswerFormBody(String securityAnswer) {
        return new FormBody.Builder()
                .add("security_answer", serverConfiguration.securityAnswer)
                .build();
    }

    @Override
    public void processSecurityResponse(SecurityAnswerResponse securityAnswerResponse) {
        serverConfiguration.enrollServer = securityAnswerResponse.enroll_server;
        serverConfiguration.sessionId = securityAnswerResponse.session_id;
    }

    @Override
    public String buildSessionCookies() {
        return "_session_id=" + serverConfiguration.sessionId;
    }

    @Override
    public String buildSessionCookies(String sessionId) {
        return null;
    }

    @Override
    public String getSessionCookie(HashMap<String, ArrayList<String>> cookieMap) {
        return null;
    }


    @Override
    public HashMap<String, ArrayList<String>> getNeededLoginCookes() {
        return null;
    }

    @Override
    public PostParameters getLoginPostParameters(String accountName, String password, String sessionId, String authenticityToken) {
        PostParameters postParameters = new PostParameters();
        postParameters.url = getLoginUrl();
        FormBody formBody = new FormBody.Builder()
                .add("userid", accountName)
                .add("pass", password)
                .add("device_id", UUID.randomUUID().toString())
                .build();
        postParameters.body = formBody;
        return postParameters;
    }

    @Override
    public void processLoginReponse(String accountName, String password, Boolean rememberMe, IConnectionHandler.PostResponse loginPostResponse) throws CoverageException {

        if (loginPostResponse.responseCode <200
            || loginPostResponse.responseCode >= 300){
            throw new CoverageException("Bad response code probably bad password");
        }
        serverConfiguration.accountName = accountName;
        serverConfiguration.password = password;
        serverConfiguration.rememberMe = rememberMe;
        LoginResponse loginResponse = parser.parseLogin(loginPostResponse.body);
        serverConfiguration.securityQuestion = loginResponse.security_question;
        serverConfiguration.securityAnswerPath = loginPostResponse.headers.get("location").get(0);
    }
}
