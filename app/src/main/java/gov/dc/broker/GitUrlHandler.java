package gov.dc.broker;

import java.util.ArrayList;
import java.util.HashMap;

import gov.dc.broker.models.Security.SecurityAnswerResponse;
import okhttp3.FormBody;
import okhttp3.HttpUrl;

public class GitUrlHandler extends UrlHandler {

    public GitUrlHandler(ServerConfiguration serverConfiguration, JsonParser parser){
        super(serverConfiguration, parser);
    }


    @Override
    public HttpUrl getLoginUrl() {
        return null;
    }

    @Override
    FormBody getSecurityAnswerFormBody(String securityAnswer) {
        return null;
    }

    @Override
    public void processSecurityResponse(SecurityAnswerResponse securityAnswerResponse) {
    }

    @Override
    public String buildSessionCookies() {
        return null;
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
    public void processLoginReponse(String accountName, String password, Boolean rememberMe, IConnectionHandler.PostResponse loginPostResponse){
        serverConfiguration.accountName = accountName;
        serverConfiguration.password = password;
        serverConfiguration.rememberMe = rememberMe;
        serverConfiguration.securityQuestion = "this is a test question.";
        serverConfiguration.securityAnswer = null;
    }

    @Override
    public HashMap<String, ArrayList<String>> getNeededLoginCookes() {
        return null;
    }

    @Override
    public PostParameters getLoginPostParameters(String accountName, String password) {
        return null;
    }

    @Override
    protected HttpUrl getEmployerDetailsUrl(String employerId) {
        return HttpUrl.parse(employerId);
    }

    protected HttpUrl getEmployerRosterUrl(String employerId) {
        return HttpUrl.parse(employerId);
    }
}
