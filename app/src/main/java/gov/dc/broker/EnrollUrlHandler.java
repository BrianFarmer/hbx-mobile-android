package gov.dc.broker;

import java.util.ArrayList;
import java.util.HashMap;

import gov.dc.broker.models.Security.SecurityAnswerResponse;
import okhttp3.FormBody;
import okhttp3.HttpUrl;

public class EnrollUrlHandler extends UrlHandler {

    public EnrollUrlHandler(ServerConfiguration serverConfiguration){
        super(serverConfiguration);
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
    HttpUrl getSecurityAnswerUrl() {
        return new HttpUrl.Builder()
                .scheme(serverConfiguration.loginInfo.scheme)
                .host(serverConfiguration.loginInfo.host)
                .addPathSegments(serverConfiguration.securityAnswerPath)
                .port(serverConfiguration.loginInfo.port)
                .build();
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
        return null;
    }

    @Override
    public void processLoginReponse(String accountName, String password, Boolean rememberMe, IConnectionHandler.PostResponse loginPostResponse) {

    }
}
