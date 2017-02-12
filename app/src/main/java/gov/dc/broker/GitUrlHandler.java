package gov.dc.broker;

import java.util.ArrayList;
import java.util.HashMap;

import gov.dc.broker.models.Security.SecurityAnswerResponse;
import gov.dc.broker.models.gitaccounts.GitAccounts;
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
    public CoverageConnection.LoginResult processLoginReponse(String accountName, String password, Boolean rememberMe, IConnectionHandler.PostResponse loginPostResponse, boolean useFingerprintSensor){
        serverConfiguration.accountName = accountName;
        serverConfiguration.password = password;
        serverConfiguration.rememberMe = rememberMe;
        serverConfiguration.securityQuestion = "this is a test question.";
        serverConfiguration.securityAnswer = null;
        return CoverageConnection.LoginResult.Success;
    }

    @Override
    public HashMap<String, ArrayList<String>> getNeededLoginCookes() {
        return null;
    }

    @Override
    public PostParameters getLoginPostParameters(String accountName, String password) {
        return null;
    }

    /*@Override
    protected HttpUrl getEmployerDetailsUrl(String employerId) {
        return HttpUrl.parse(employerId);
    }*/

    protected HttpUrl getEmployerRosterUrl(String employerId) {
        return HttpUrl.parse(employerId);
    }

    public GetParameters getGitAccountGetParameters(String urlRoot) {
        GetParameters getParameters = new GetParameters();
        getParameters.url = HttpUrl.parse(urlRoot + "/accounts.json");
        return getParameters;
    }

    public GitAccounts processGetGitAccounts(IConnectionHandler.GetReponse getReponse, String urlRoot) throws Exception {
        if (getReponse.responseCode < 200
            || getReponse.responseCode >= 300){
            throw new Exception("Error getting accounts");
        }

        return parser.parseGitAccounts(getReponse.body);
    }

    @Override
    GetParameters getEmployerDetailsParameters(String employerId) {
        GetParameters getParameters = new GetParameters();

        if (employerId == null) {
            if (serverConfiguration.employerDetailPath.substring(0, 4).compareToIgnoreCase("http") == 0) {
                getParameters.url = HttpUrl.parse(serverConfiguration.employerDetailPath);
            } else {
                getParameters.url = new HttpUrl.Builder()
                        .scheme(serverConfiguration.dataInfo.scheme)
                        .host(serverConfiguration.dataInfo.host)
                        .addPathSegment(serverConfiguration.employerDetailPath)
                        .port(serverConfiguration.dataInfo.port).build();
            }
        }
        else {
            getParameters.url = HttpUrl.parse(employerId);
        }
        return getParameters;
    }

    @Override
    GetParameters getEmployerRosterParameters(String rosterId) {
        GetParameters getParameters = new GetParameters();

            HttpUrl.Builder host = new HttpUrl.Builder()
                    .scheme(serverConfiguration.dataInfo.scheme)
                    .host(serverConfiguration.dataInfo.host);
            host = host.addPathSegments(serverConfiguration.employerRosterPathForBroker);
            if (rosterId != null) {
                host = host.addPathSegments(rosterId);
            }
            getParameters.url = host.port(serverConfiguration.dataInfo.port)
                    .build();
        return getParameters;
    }

    @Override
    GetParameters getEmployerRosterParameters() {
        GetParameters getParameters = new GetParameters();

        if (serverConfiguration.employerRosterPathForBroker.substring(0,4).compareToIgnoreCase("http") == 0){
            getParameters.url = HttpUrl.parse(serverConfiguration.employerRosterPathForBroker);
        } else {
            getParameters.url = new HttpUrl.Builder()
                    .scheme(serverConfiguration.dataInfo.scheme)
                    .host(serverConfiguration.dataInfo.host)
                    .addPathSegments(serverConfiguration.employerRosterPathForBroker)
                    .port(serverConfiguration.dataInfo.port)
                    .build();
        }
        return getParameters;
    }
}
