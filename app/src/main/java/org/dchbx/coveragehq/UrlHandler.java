package org.dchbx.coveragehq;

import android.net.Uri;
import android.util.Log;

import org.dchbx.coveragehq.exceptions.BrokerNotFoundException;
import org.dchbx.coveragehq.exceptions.EmployerNotFoundException;
import org.dchbx.coveragehq.exceptions.IndividualNotFoundException;
import org.dchbx.coveragehq.models.Security.Endpoints;
import org.dchbx.coveragehq.models.Security.SecurityAnswerResponse;
import org.dchbx.coveragehq.models.brokeragency.BrokerAgency;
import org.dchbx.coveragehq.models.employer.Employer;
import org.dchbx.coveragehq.models.planshopping.Plan;
import org.dchbx.coveragehq.models.roster.Enrollment;
import org.dchbx.coveragehq.models.roster.Health;
import org.dchbx.coveragehq.models.roster.Roster;
import org.dchbx.coveragehq.models.roster.RosterEntry;
import org.dchbx.coveragehq.models.roster.SummaryOfBenefits;
import org.dchbx.coveragehq.models.services.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.HttpUrl;

/**
 * Created by plast on 12/15/2016.
 */

public abstract class UrlHandler {
    private static String TAG = "UrlHandler";

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


    GetParameters getIndividualParameters(String employerId) {
        GetParameters getParameters = new GetParameters();
        if (serverConfiguration.sessionId != null) {
            getParameters.cookies = new HashMap<>();
            getParameters.cookies.put("_session_id", serverConfiguration.sessionId);
        }
        getParameters.url = HttpUrl.parse(serverConfiguration.individualPath);
        return getParameters;
    }

    public RosterEntry processIndividual(IConnectionHandler.GetResponse response) throws IndividualNotFoundException {

        if (response.responseCode == 401
                || response.responseCode == 404){
            throw new IndividualNotFoundException();
        }
        return parser.parseIndividual(response.body);
    }

    public Employer processEmployerDetails(IConnectionHandler.GetResponse response) throws EmployerNotFoundException {
        if (response.responseCode == 401
                || response.responseCode == 404){
            throw new EmployerNotFoundException();
        }

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


    public Roster processRoster(IConnectionHandler.GetResponse response) {
        return parser.parseRoster(response.body);
    }

    GetParameters getCarriersUrl() {
        GetParameters getParameters = new GetParameters();
        getParameters.url = HttpUrl.parse("https://dchealthlink.com/shared/json/carriers.json");
        return getParameters;
    }

    public Carriers processCarrier(IConnectionHandler.GetResponse response){
        return parser.parseCarriers(response.body);
    }

    public BrokerAgency processBrokerAgency(IConnectionHandler.GetResponse getReponse) throws Exception {
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

    public RosterEntry processEmployeeDetails(IConnectionHandler.GetResponse getResponse) throws IndividualNotFoundException, CoverageException {
        if (getResponse.responseCode == 401
                || getResponse.responseCode == 404){
            throw new IndividualNotFoundException();
        }
        if (getResponse.responseCode > 299
            || getResponse.responseCode < 200){
            throw new CoverageException("Error get individual");
        }
        RosterEntry rosterEntry = parser.parseEmployeeDetails(getResponse.body);
        if (rosterEntry.enrollments != null){
            for (Enrollment enrollment : rosterEntry.enrollments) {
                if (enrollment.health != null){
                    checkPlanUrls(enrollment.health);
                }
                if (enrollment.dental != null){
                    checkPlanUrls(enrollment.dental);
                }
            }

        }
        return rosterEntry;
    }

    private void checkPlanUrls(Health plan) {
        if (plan.servicesRatesUrl != null){
            plan.servicesRatesUrl = checkUrl(plan.servicesRatesUrl);
        }
        if (plan.provider_directory_url != null){
            plan.provider_directory_url = checkUrl(plan.provider_directory_url);
        }
        if (plan.RxFormularyUrl != null){
            plan.RxFormularyUrl = checkUrl(plan.RxFormularyUrl);
        }
    }

    protected String checkUrl(String url){
        Log.d(TAG, "checking url; " + url);
        if (url.toLowerCase().startsWith("http")){
            return url;
        }

        HttpUrl rootUrl = HttpUrl.parse(serverConfiguration.individualPath);
        Uri.Builder builder = new Uri.Builder();
        Uri qualifiedUrl = builder.scheme(rootUrl.scheme())
                .authority(rootUrl.host())
                .build();
        return qualifiedUrl.toString() + url;
    }

    public GetParameters getEmployeeDetailsParameters() {
        GetParameters getParameters = new GetParameters();
        if (serverConfiguration.sessionId != null) {
            getParameters.cookies = new HashMap<>();
            getParameters.cookies.put("_session_id", serverConfiguration.sessionId);
        }
        getParameters.url = HttpUrl.parse(serverConfiguration.individualPath);
        return getParameters;
    }

    abstract FormBody getSecurityAnswerFormBody(String securityAnswer);
    abstract void processSecurityResponse(SecurityAnswerResponse securityAnswerResponse);
    public abstract String buildSessionCookies();
    public abstract String buildSessionCookies(String sessionId);
    public abstract String getSessionCookie(HashMap<String, ArrayList<String>> cookieMap);
    public abstract HashMap<String,ArrayList<String>> getNeededLoginCookes();
    public abstract PostParameters getLoginPostParameters(String accountName, String password);
    public abstract CoverageConnection.LoginResult processLoginReponse(String accountName, String password, Boolean rememberMe, IConnectionHandler.PostResponse loginPostResponse, boolean useFingerprintSensor) throws CoverageException;

    public GetParameters getSummaryOfBenefitsParameters(String summaryOfBenefitsUrl){
        GetParameters getParameters = new GetParameters();
        if (serverConfiguration.sessionId != null) {
            getParameters.cookies = new HashMap<>();
            getParameters.cookies.put("_session_id", serverConfiguration.sessionId);
        }
        getParameters.url = HttpUrl.parse(summaryOfBenefitsUrl);
        return getParameters;
    }

    public GetParameters getServicesParameters(String servicesUrl){
        GetParameters getParameters = new GetParameters();
        if (serverConfiguration.sessionId != null) {
            getParameters.cookies = new HashMap<>();
            getParameters.cookies.put("_session_id", serverConfiguration.sessionId);
        }
        getParameters.url = HttpUrl.parse(servicesUrl);
        return getParameters;
    }

    public List<SummaryOfBenefits> processSummaryOfBenefits(IConnectionHandler.GetResponse response){
        return parser.parseSummaryOfBenefits(response.body);
    }

    public List<Service> processServices(IConnectionHandler.GetResponse response){
        return parser.parseServices(response.body);
    }

    public GetParameters getPlansParameters() {
        GetParameters parameters = new GetParameters();

        String queryParameters = "?coverage_kind=health&active_year=2017";
        for (Integer age : serverConfiguration.planShoppingParameters.ages) {
            queryParameters = queryParameters + "&ages=" + age.toString();
        }
        parameters.url = HttpUrl.parse(serverConfiguration.planEndpoint + queryParameters);
        return parameters;
    }

    public GetParameters getEndpointsParameters(){
        GetParameters getParameters = new GetParameters();
        getParameters.url = new HttpUrl.Builder()
                .scheme(serverConfiguration.loginInfo.scheme)
                .host(serverConfiguration.loginInfo.host)
                .addPathSegments(serverConfiguration.endpointsPath)
                .port(serverConfiguration.loginInfo.port)
                .build();
        return getParameters;
    }

    public void processEndpoints(IConnectionHandler.GetResponse getResponse) {
        Endpoints endpoints = parser.parseEndpoionts(getResponse.body);
        serverConfiguration.planEndpoint = endpoints.plan_endpoint;
        serverConfiguration.verifyIdentityEndpoint = endpoints.verify_identity_endpoint;
    }

    public List<Plan> processPlans(IConnectionHandler.GetResponse getResponse) {
        return parser.parsePlans(getResponse.body);
    }
}
