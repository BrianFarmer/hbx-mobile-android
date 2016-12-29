package gov.dc.broker;

import java.util.ArrayList;
import java.util.HashMap;

import gov.dc.broker.models.Security.SecurityAnswerResponse;
import gov.dc.broker.models.brokeragency.BrokerAgency;
import okhttp3.FormBody;
import okhttp3.HttpUrl;

/**
 * Created by plast on 12/15/2016.
 */

public abstract class UrlHandler {

    public class PutParameters {
        FormBody body;
        HttpUrl url;
        public HashMap<String, String> cookies;
        public HashMap<String, String> headers;
    }

    public class PostParameters {
        FormBody body;
        HttpUrl url;
        public HashMap<String, String> cookies;
        public HashMap<String, String> headers;
    }

    public class GetParameters {
        HttpUrl url;
        public HashMap<String, String> cookies;
        public HashMap<String, String> headers;
    }

    protected final ServerConfiguration serverConfiguration;
    private final JsonParser parser;

    public UrlHandler(ServerConfiguration serverConfiguration, JsonParser parser){
        this.serverConfiguration = serverConfiguration;
        this.parser = parser;
    }

    public abstract HttpUrl getLoginUrl();

    public abstract PutParameters getSecurityAnswerPutParameters(String securityAnswer);
    public abstract void processSecurityAnswerResponse(IConnectionHandler.PutResponse putResponse) throws CoverageException;

    public GetParameters getBrokerAgencyParameters() {
        GetParameters getParameters = new GetParameters();
        if (serverConfiguration.sessionId != null) {
            getParameters.cookies = new HashMap<>();
            getParameters.cookies.put("_session_id", serverConfiguration.sessionId);
        }
        getParameters.url = new HttpUrl.Builder()
                .scheme(serverConfiguration.dataInfo.scheme)
                .host(serverConfiguration.dataInfo.host)
                .addPathSegments(serverConfiguration.employerListPath)
                .port(serverConfiguration.dataInfo.port)
                .build();

        return getParameters;
    }

    HttpUrl getEmployerDetailsUrl() {
        return new HttpUrl.Builder()
                .scheme(serverConfiguration.dataInfo.scheme)
                .host(serverConfiguration.dataInfo.host)
                .addPathSegments(serverConfiguration.employerDetailPathForEmployer)
                .port(serverConfiguration.dataInfo.port)
                .build();
    }

    protected HttpUrl getEmployerDetailsUrl(String employerId) {
        return new HttpUrl.Builder()
                .scheme(serverConfiguration.dataInfo.scheme)
                .host(serverConfiguration.dataInfo.host)
                .addPathSegments(String.format(serverConfiguration.employerDetailPathForBroker, employerId))
                .port(serverConfiguration.dataInfo.port)
                .build();
    }

    protected HttpUrl getEmployerRosterUrl() {
        return new HttpUrl.Builder()
                .scheme(serverConfiguration.dataInfo.scheme)
                .host(serverConfiguration.dataInfo.host)
                .addPathSegments(serverConfiguration.employerDetailPathForEmployer)
                .port(serverConfiguration.dataInfo.port)
                .build();
    }

    protected HttpUrl getEmployerRosterUrl(String employerId) {
        return new HttpUrl.Builder()
                .scheme(serverConfiguration.dataInfo.scheme)
                .host(serverConfiguration.dataInfo.host)
                .addPathSegments(String.format(serverConfiguration.employerRosterPathForBroker, employerId))
                .port(serverConfiguration.dataInfo.port)
                .build();
    }

    HttpUrl getCarriersUrl() {
        return new HttpUrl.Builder()
                .scheme(serverConfiguration.carrierInfo.scheme)
                .host(serverConfiguration.carrierInfo.host)
                .addPathSegments(serverConfiguration.carrierPath)
                .port(serverConfiguration.carrierInfo.port)
                .build();
    }


    public BrokerAgency processBrokerAgency(IConnectionHandler.GetReponse getReponse){
        return parser.parseEmployerList(getReponse.body);
    }


    abstract FormBody getSecurityAnswerFormBody(String securityAnswer);
    abstract void processSecurityResponse(SecurityAnswerResponse securityAnswerResponse);
    public abstract String buildSessionCookies();
    public abstract String buildSessionCookies(String sessionId);
    public abstract String getSessionCookie(HashMap<String, ArrayList<String>> cookieMap);
    public abstract HashMap<String,ArrayList<String>> getNeededLoginCookes();
    public abstract PostParameters getLoginPostParameters(String accountName, String password, String sessionId, String authenticityToken);
    public abstract void processLoginReponse(String accountName, String password, Boolean rememberMe, IConnectionHandler.PostResponse loginPostResponse) throws CoverageException;
}
