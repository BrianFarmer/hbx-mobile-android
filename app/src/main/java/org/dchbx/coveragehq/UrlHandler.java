package org.dchbx.coveragehq;

import org.dchbx.coveragehq.models.Security.SecurityAnswerResponse;
import org.dchbx.coveragehq.models.brokeragency.BrokerAgency;
import org.dchbx.coveragehq.models.employer.Employer;
import org.dchbx.coveragehq.models.roster.Roster;
import org.dchbx.coveragehq.models.roster.RosterEntry;

import java.util.ArrayList;
import java.util.HashMap;

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
        public HashMap<String, String> formParameters;
    }

    public class GetParameters {
        HttpUrl url;
        public HashMap<String, String> cookies;
        public HashMap<String, String> headers;
    }

    protected final ServerConfiguration serverConfiguration;
    protected final JsonParser parser;

    public UrlHandler(ServerConfiguration serverConfiguration, JsonParser parser){
        this.serverConfiguration = serverConfiguration;
        this.parser = parser;
    }

    public abstract HttpUrl getLoginUrl();


    public GetParameters getBrokerAgencyParameters() {
        GetParameters getParameters = new GetParameters();
        if (serverConfiguration.sessionId != null) {
            getParameters.cookies = new HashMap<>();
            getParameters.cookies.put("_session_id", serverConfiguration.sessionId.trim());
        }
        String subString = serverConfiguration.brokerDetailPath.substring(0,4);
        String subString2 = serverConfiguration.brokerDetailPath.substring(0,3);
        if (serverConfiguration.brokerDetailPath.substring(0,4).compareToIgnoreCase("http") == 0) {
            getParameters.url = HttpUrl.parse(serverConfiguration.brokerDetailPath);
        } else {
            getParameters.url = new HttpUrl.Builder()
                    .scheme(serverConfiguration.dataInfo.scheme)
                    .host(serverConfiguration.dataInfo.host)
                    .addPathSegments(serverConfiguration.brokerDetailPath)
                    .port(serverConfiguration.dataInfo.port)
                    .build();
        }
        return getParameters;
    }

    public Employer processEmployerDetails(IConnectionHandler.GetReponse response) {
        return parser.parseEmployerDetails(response.body);
    }

    GetParameters getEmployerDetailsParameters(String employerId) {
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
                HttpUrl.Builder host = new HttpUrl.Builder()
                        .scheme(serverConfiguration.dataInfo.scheme)
                        .host(serverConfiguration.dataInfo.host);
                if (path == null) {
                    host = host.addPathSegments(serverConfiguration.employerDetailPath);
                } else {
                    host = host.addPathSegments(serverConfiguration.employerDetailPath);
                    host = host.addPathSegments(path);
                }
                getParameters.url = host.port(serverConfiguration.dataInfo.port).build();
            }
        }
        return getParameters;
    }
    /*
    protected HttpUrl getEmployerDetailsUrl(String employerId) {
        if (employerId.substring(0,4).compareToIgnoreCase("http") == 0){
            return HttpUrl.parse(employerId);
        }

        return HttpUrl.parse(serverConfiguration.enrollServer + employerId);
        return new HttpUrl.Builder()
                .scheme(serverConfiguration.dataInfo.scheme)
                .host(serverConfiguration.dataInfo.host)
                .addPathSegments(String.format(serverConfiguration.employerDetailPath, employerId))
                .port(serverConfiguration.dataInfo.port)
                .build();
    }*/

    GetParameters getEmployerRosterParameters(String rosterId) {
        GetParameters getParameters = new GetParameters();

        if (serverConfiguration.sessionId != null) {
            getParameters.cookies = new HashMap<>();
            getParameters.cookies.put("_session_id", serverConfiguration.sessionId);
        }


        if (rosterId.substring(0,4).compareToIgnoreCase("http") == 0){
            getParameters.url = HttpUrl.parse(rosterId);
        } else {
            HttpUrl.Builder host = new HttpUrl.Builder()
                    .scheme(serverConfiguration.dataInfo.scheme)
                    .host(serverConfiguration.dataInfo.host);
            if (rosterId == null) {
                host = host.addPathSegments(serverConfiguration.employerRosterPathForBroker);
            } else {
                String segments;
                if (rosterId.substring(0,1).compareTo("/") == 0){
                    segments = rosterId.substring(1);
                } else {
                    segments = rosterId;
                }
                host = host.addPathSegments(segments);
            }
            getParameters.url = host.port(serverConfiguration.dataInfo.port)
                    .build();
        }
        return getParameters;
    }

    GetParameters getEmployerRosterParameters() {
        GetParameters getParameters = new GetParameters();

        if (serverConfiguration.sessionId != null) {
            getParameters.cookies = new HashMap<>();
            getParameters.cookies.put("_session_id", serverConfiguration.sessionId);
        }


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


    public Roster processRoster(IConnectionHandler.GetReponse response) {
        return parser.parseRoster(response.body);
    }

    GetParameters getCarriersUrl() {
        GetParameters getParameters = new GetParameters();
        getParameters.url = HttpUrl.parse("https://dchealthlink.com/shared/json/carriers.json");
        return getParameters;
    }

    public Carriers processCarrier(IConnectionHandler.GetReponse response){
        return parser.parseCarriers(response.body);
    }

    public BrokerAgency processBrokerAgency(IConnectionHandler.GetReponse getReponse) throws Exception {
        if (getReponse.responseCode == 401
            || getReponse.responseCode == 404){
            throw new BrokerNotFoundException();
        }
        return parser.parseEmployerList(getReponse.body);
    }
    protected ServerConfiguration.HostInfo parseHostInfo(String enroll_server) {
        ServerConfiguration.HostInfo hostInfo = new ServerConfiguration.HostInfo();
        HttpUrl parse = HttpUrl.parse(enroll_server);
        hostInfo.scheme = parse.scheme();
        hostInfo.host = parse.host();
        hostInfo.port = parse.port();
        return hostInfo;
    }

    public GetParameters getGlossaryParameters() {
        GetParameters getParameters = new GetParameters();
        getParameters.url = HttpUrl.parse("https://dchealthlink.com/glossary/json");
        return getParameters;
    }

    public GetParameters getEmployeeDetailsParameters() {
        return null;
    }

    public RosterEntry processEmployeeDetails(IConnectionHandler.GetReponse getReponse) {
        return null;
    }

    abstract FormBody getSecurityAnswerFormBody(String securityAnswer);
    abstract void processSecurityResponse(SecurityAnswerResponse securityAnswerResponse);
    public abstract String buildSessionCookies();
    public abstract String buildSessionCookies(String sessionId);
    public abstract String getSessionCookie(HashMap<String, ArrayList<String>> cookieMap);
    public abstract HashMap<String,ArrayList<String>> getNeededLoginCookes();
    public abstract PostParameters getLoginPostParameters(String accountName, String password);
    public abstract CoverageConnection.LoginResult processLoginReponse(String accountName, String password, Boolean rememberMe, IConnectionHandler.PostResponse loginPostResponse, boolean useFingerprintSensor) throws CoverageException;
}
