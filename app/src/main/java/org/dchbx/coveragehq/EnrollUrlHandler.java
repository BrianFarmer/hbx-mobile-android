package org.dchbx.coveragehq;

import org.dchbx.coveragehq.models.Security.LoginResponse;
import org.dchbx.coveragehq.models.Security.SecurityAnswerResponse;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.FormBody;
import okhttp3.HttpUrl;

public class EnrollUrlHandler extends UrlHandler {

    private final JsonParser parser;

    public EnrollUrlHandler(ServerConfiguration serverConfiguration, JsonParser parser){
        super(serverConfiguration, parser);
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

    public PostParameters getSecurityAnswerPostParameters(String securityAnswer) {
        PostParameters postParameters = new PostParameters();

        postParameters.url = new HttpUrl.Builder()
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


        postParameters.body = new FormBody.Builder()
                .add("security_answer", securityAnswer)
                .build();


        return postParameters;
    }

    public void processSecurityAnswerResponse(IConnectionHandler.PostResponse putResponse) throws CoverageException {
        if (putResponse.responseCode >= 300
            || putResponse.responseCode < 200){
            throw new CoverageException("Security answer not accepted");
        }
        SecurityAnswerResponse securityAnswerResponse = parser.parseSecurityAnswerResponse(putResponse.body);
        serverConfiguration.dataInfo = parseHostInfo(securityAnswerResponse.enroll_server);
        serverConfiguration.sessionId = securityAnswerResponse.session_id;
        serverConfiguration.employerDetailPath = securityAnswerResponse.employer_details_endpoint;
        serverConfiguration.brokerDetailPath = securityAnswerResponse.broker_endpoint;
        serverConfiguration.employerRosterPathForBroker = securityAnswerResponse.employee_roster_endpoint;
        serverConfiguration.individualPath = securityAnswerResponse.individual_endpoint;
    }

    @Override
    FormBody getSecurityAnswerFormBody(String securityAnswer) {
        return new FormBody.Builder()
                .add("security_answer", serverConfiguration.securityAnswer)
                .build();
    }

    @Override
    public void processSecurityResponse(SecurityAnswerResponse securityAnswerResponse) {
        serverConfiguration.dataInfo = parseHostInfo(securityAnswerResponse.enroll_server);
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
    public PostParameters getLoginPostParameters(String accountName, String password) {
        PostParameters postParameters = new PostParameters();
        postParameters.url = getLoginUrl();
        FormBody formBody = new FormBody.Builder()
                .build();
        postParameters.body = formBody;
        postParameters.formParameters = new HashMap<>();
        postParameters.formParameters.put("userid", accountName);
        postParameters.formParameters.put("pass", password);
        return postParameters;
    }

    @Override
    public CoverageConnection.LoginResult processLoginReponse(String accountName, String password, Boolean rememberMe, IConnectionHandler.PostResponse loginPostResponse, boolean useFingerprintSensor) throws CoverageException {

        if (loginPostResponse.responseCode == 401){
            return CoverageConnection.LoginResult.Failure;
        }

        if (loginPostResponse.responseCode <200
            || loginPostResponse.responseCode >= 300){
            return CoverageConnection.LoginResult.Error;
        }
        serverConfiguration.accountName = accountName;
        serverConfiguration.password = password;
        serverConfiguration.rememberMe = rememberMe;
        serverConfiguration.useFingerprintSensor = useFingerprintSensor;
        LoginResponse loginResponse = parser.parseLogin(loginPostResponse.body);
        serverConfiguration.securityQuestion = loginResponse.security_question;
        serverConfiguration.securityAnswerPath = loginPostResponse.headers.get("location").get(0);
        return CoverageConnection.LoginResult.NeedSecurityQuestion;
    }

    public GetParameters getLoginFormParameters() {
        GetParameters getParameters = new GetParameters();
        getParameters.url = getLoginUrl();
        return getParameters;
    }

    public GetParameters getStayLoggedInParameters() {
        GetParameters getParameters = new GetParameters();
        if (serverConfiguration.sessionId != null) {
            getParameters.cookies = new HashMap<>();
            getParameters.cookies.put("_session_id", serverConfiguration.sessionId.trim());
        }
        getParameters.url = new HttpUrl.Builder()
                .scheme(serverConfiguration.dataInfo.scheme)
                .host(serverConfiguration.dataInfo.host)
                .addPathSegments(serverConfiguration.stayLoggedInPath)
                .port(serverConfiguration.dataInfo.port)
                .build();
        return getParameters;
    }

    public void processStayLoggedInResponse(IConnectionHandler.GetReponse getReponse) throws Exception {
        if (getReponse.responseCode < 200
            || getReponse.responseCode > 299){
            throw new Exception("Unable to reset sesssion time");
        }
    }

    @Override
    public  GetParameters getEmployerDetailsParameters(String employerId) {
        GetParameters getParameters = new GetParameters();

        if (serverConfiguration.sessionId != null) {
            getParameters.cookies = new HashMap<>();
            getParameters.cookies.put("_session_id", serverConfiguration.sessionId);
        }

        if (employerId == null) {
            getParameters.url = HttpUrl.parse(serverConfiguration.employerDetailPath);
        }
        else {
            if (employerId.substring(0, 4).compareToIgnoreCase("http") == 0) {
                getParameters.url = HttpUrl.parse(employerId);
            } else {
                String path;
                if (employerId.substring(0, 1).compareTo("/") == 0) {
                    path = employerId.substring(1, employerId.length());
                } else {
                    path = employerId;
                }
                getParameters.url = new HttpUrl.Builder()
                        .scheme(serverConfiguration.dataInfo.scheme)
                        .host(serverConfiguration.dataInfo.host)
                        .addPathSegments(path)
                        .port(serverConfiguration.dataInfo.port).build();
            }
        }
        return getParameters;
    }

}
