package org.dchbx.coveragehq;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import org.dchbx.coveragehq.exceptions.BrokerNotFoundException;
import org.dchbx.coveragehq.exceptions.EmployerNotFoundException;
import org.dchbx.coveragehq.exceptions.IndividualNotFoundException;
import org.dchbx.coveragehq.models.Security.Endpoints;
import org.dchbx.coveragehq.models.Security.SecurityAnswerResponse;
import org.dchbx.coveragehq.models.brokeragency.BrokerAgency;
import org.dchbx.coveragehq.models.employer.Employer;
import org.dchbx.coveragehq.models.fe.UqhpApplication;
import org.dchbx.coveragehq.models.planshopping.Plan;
import org.dchbx.coveragehq.models.ridp.Answers;
import org.dchbx.coveragehq.models.ridp.Questions;
import org.dchbx.coveragehq.models.ridp.SignUp.Links;
import org.dchbx.coveragehq.models.ridp.SignUp.SignUp;
import org.dchbx.coveragehq.models.ridp.VerifyIdentity;
import org.dchbx.coveragehq.models.roster.Enrollment;
import org.dchbx.coveragehq.models.roster.Health;
import org.dchbx.coveragehq.models.roster.Roster;
import org.dchbx.coveragehq.models.roster.RosterEntry;
import org.dchbx.coveragehq.models.roster.SummaryOfBenefits;
import org.dchbx.coveragehq.models.services.Service;
import org.dchbx.coveragehq.models.startup.Login;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.RequestBody;

import static org.dchbx.coveragehq.ConnectionHandler.JSON;

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

    public class HttpParameters {

    }

    public class PostParameters extends HttpParameters{
        FormBody body;
        HttpUrl url;
        public HashMap<String, String> cookies;
        public HashMap<String, String> headers;
        public HashMap<String, String> formParameters;
        public RequestBody requestBody;
        public String requestString;
    }

    public static class HttpRequest {
        public GetParameters getParameters;
        public PostParameters postParameters;

        enum RequestType {
            Get,
            Post,
            Put,
            Delete
        }

        public RequestType requestType;
        public HttpParameters httpParameters;
    }

    public class GetParameters extends HttpParameters {
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


    public HttpRequest getCreateAccount(SignUp signUp) {
        PostParameters postParameters = new PostParameters();
        postParameters.url = HttpUrl.parse(serverConfiguration.localSignUpEndpoint);
        String json = (new Gson()).toJson(signUp);
        postParameters.requestString = json;
        postParameters.requestBody = RequestBody.create(JSON, json );
        postParameters.headers = new HashMap<>();
        postParameters.headers.put("Content-Type", "application/json");

        HttpRequest request = new HttpRequest();
        request.postParameters = postParameters;
        request.requestType = HttpRequest.RequestType.Post;
        return request;

    }



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
            getParameters.cookies.put(serverConfiguration.sessionKey, serverConfiguration.sessionId);
        }
        getParameters.url = HttpUrl.parse(serverConfiguration.individualEndpoint);
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

    public HttpRequest getGlossaryParameters() {
        GetParameters getParameters = new GetParameters();
        getParameters.url = HttpUrl.parse("https://dchealthlink.com/glossary/json");
        HttpRequest httpRequest = new HttpRequest();
        httpRequest.getParameters = getParameters;
        httpRequest.requestType = HttpRequest.RequestType.Get;
        return httpRequest;
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

        HttpUrl rootUrl = HttpUrl.parse(serverConfiguration.individualEndpoint);
        Uri.Builder builder = new Uri.Builder();
        Uri qualifiedUrl = builder.scheme(rootUrl.scheme())
                .authority(rootUrl.host())
                .build();
        return qualifiedUrl.toString() + url;
    }

    public GetParameters getEmployeeDetailsParameters() {
        GetParameters getParameters = new GetParameters();
        if (serverConfiguration.sessionKey != null) {
            getParameters.cookies = new HashMap<>();
            getParameters.cookies.put(serverConfiguration.sessionKey, serverConfiguration.sessionValue);
        }
        getParameters.url = HttpUrl.parse(serverConfiguration.individualEndpoint);
        return getParameters;
    }

    abstract FormBody getSecurityAnswerFormBody(String securityAnswer);
    abstract void processSecurityResponse(SecurityAnswerResponse securityAnswerResponse);
    public abstract String buildSessionCookies();
    public abstract String buildSessionCookies(String sessionId);
    public abstract String getSessionCookie(HashMap<String, ArrayList<String>> cookieMap);
    public abstract HashMap<String,ArrayList<String>> getNeededLoginCookes();
    public abstract PostParameters getLoginPostParameters(String accountName, String password);
    public abstract CoverageConnection.LoginResult processLoginReponse(String accountName, String password, Boolean rememberMe, IConnectionHandler.PostResponse loginPostResponse, boolean useFingerprintSensor) throws Exception;

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

    public HttpRequest getPlansParameters(LocalDate year, ArrayList<Integer> ages) {
        GetParameters parameters = new GetParameters();
        String queryParameters = "?coverage_kind=health&active_year=2017"; // + year.getYear();
        for (Integer age : ages) {
            queryParameters = queryParameters + "&ages=" + age.toString();
        }
        parameters.url = HttpUrl.parse(serverConfiguration.planEndpoint + queryParameters);

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.requestType = HttpRequest.RequestType.Get;
        httpRequest.getParameters = parameters;
        return httpRequest;
    }

    public GetParameters getEndpointsParameters(){
        if (serverConfiguration.endpointsPath == null){
            return null;
        }
        if (serverConfiguration.endpointsPath.substring(0, 4).toLowerCase().compareTo("http") == 0){
            GetParameters getParameters = new GetParameters();
            getParameters.url = HttpUrl.parse(serverConfiguration.endpointsPath);
            return getParameters;
        }
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
        if (endpoints.enroll_server != null) {
            HttpUrl parsedUrl = HttpUrl.parse(endpoints.enroll_server);
            serverConfiguration.dataInfo.host = parsedUrl.host();
            serverConfiguration.dataInfo.port = parsedUrl.port();
            serverConfiguration.dataInfo.scheme = parsedUrl.scheme();
        }
        serverConfiguration.planEndpoint = endpoints.plan_endpoint;
        serverConfiguration.verifyIdentityEndpoint = endpoints.verify_identity_endpoint;
        serverConfiguration.verifyIdentityAnswersEndpoint = endpoints.verify_identity_answers_endpoint ;
        serverConfiguration.localSignUpEndpoint = endpoints.local_sign_up_endpoint;
        serverConfiguration.localLoginEndpoint = endpoints.local_login_endpoint;
        serverConfiguration.localLogoutEndpoint = endpoints.local_logout_endpoint;
        serverConfiguration.uqhpApplicationSchemaEndpoint = endpoints.uqhp_application_schema_endpoint;
        serverConfiguration.faaApplicationSchemaEndpoint = endpoints.faa_application_schema_endpoint;
        serverConfiguration.effectiveDateEndpoint = endpoints.effective_date_endpoint;
        serverConfiguration.glossaryEndpoint = endpoints.glossary_endpoint;
        serverConfiguration.openEnrollmentStatusEndpoint = endpoints.open_enrollment_status_endpoint;
    }

    public List<Plan> processPlans(IConnectionHandler.GetResponse getResponse) {
        List<Plan> plans = parser.parsePlans(getResponse.body);
        for (Plan plan : plans) {
            if (plan.links != null){
                if (plan.links.carrierLogo != null
                && plan.links.carrierLogo.substring(0,1).compareTo("/") == 0) {
                    plan.links.carrierLogo = new HttpUrl.Builder()
                            .scheme(serverConfiguration.dataInfo.scheme)
                            .host(serverConfiguration.dataInfo.host)
                            .addPathSegments(plan.links.carrierLogo.substring(1))
                            .port(serverConfiguration.dataInfo.port)
                            .build().toString();
                }
                if (plan.links.summaryOfBenefits != null
                    && plan.links.summaryOfBenefits.substring(0,1).compareTo("/") == 0){
                    plan.links.summaryOfBenefits = new HttpUrl.Builder()
                            .scheme(serverConfiguration.dataInfo.scheme)
                            .host(serverConfiguration.dataInfo.host)
                            .addPathSegments(plan.links.summaryOfBenefits.substring(1))
                            .port(serverConfiguration.dataInfo.port)
                            .build().toString();
                }
            }
        }

        return plans;
    }

    public HttpRequest getSummaryParameters(String summaryOfBenefits) {

        String[] split = summaryOfBenefits.split("\\?");
        GetParameters getParameters = new GetParameters();
        getParameters.url = new HttpUrl.Builder()
                .scheme(serverConfiguration.dataInfo.scheme)
                //.host(serverConfiguration.dataInfo.host)
                .host("enroll-mobile2.dchbx.org")
                .addEncodedPathSegments(split[0])
                .query(split[1])
                .port(serverConfiguration.dataInfo.port)
                .build();

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.requestType = HttpRequest.RequestType.Get;
        httpRequest.getParameters = getParameters;
        return httpRequest;
    }

    public HttpRequest getRidpVerificationParameters(VerifyIdentity verifyIdentity) {
        PostParameters postParameters = new PostParameters();
        if (serverConfiguration.verifyIdentityEndpoint.substring(0, 4).toLowerCase().compareTo("http") == 0){
            postParameters.url = HttpUrl.parse(serverConfiguration.verifyIdentityEndpoint);
        } else {
            postParameters.url = new HttpUrl.Builder()
                    .scheme(serverConfiguration.loginInfo.scheme)
                    .host(serverConfiguration.loginInfo.host)
                    .addPathSegments(serverConfiguration.verifyIdentityEndpoint)
                    .port(serverConfiguration.loginInfo.port)
                    .build();
        }
        String json = (new Gson()).toJson(verifyIdentity);
        postParameters.requestBody = RequestBody.create(JSON, json );
        postParameters.requestString = json;
        postParameters.headers = new HashMap<>();
        postParameters.headers.put("Content-Type", "application/json");

        HttpRequest request = new HttpRequest();
        request.postParameters = postParameters;
        request.requestType = HttpRequest.RequestType.Post;
        return request;
    }

    public Questions processRidpQuestions(IConnectionHandler.GetResponse response) {
        return parser.parseRidpQuestions(response.body);
    }

    public HttpRequest getAnswersRequest(Answers answers) {
        PostParameters postParameters = new PostParameters();
        if (serverConfiguration.verifyIdentityAnswersEndpoint.substring(0, 4).toLowerCase().compareTo("http") == 0){
            postParameters.url = HttpUrl.parse(serverConfiguration.verifyIdentityAnswersEndpoint);
        } else {
            postParameters.url = new HttpUrl.Builder()
                    .scheme(serverConfiguration.loginInfo.scheme)
                    .host(serverConfiguration.loginInfo.host)
                    .addPathSegments(serverConfiguration.verifyIdentityAnswersEndpoint)
                    .port(serverConfiguration.loginInfo.port)
                    .build();
        }
        String jsonString = (new Gson()).toJson(answers);
        postParameters.requestBody = RequestBody.create(JSON, jsonString);
        postParameters.requestString = jsonString;

        HttpRequest request = new HttpRequest();
        request.postParameters = postParameters;
        request.requestType = HttpRequest.RequestType.Post;
        return request;
    }

    public HttpRequest getFinancialEligibilityJson(){
        GetParameters getParameters = new GetParameters();
        if (serverConfiguration.faaApplicationSchemaEndpoint.substring(0, 4).toLowerCase().compareTo("http") == 0){
            getParameters.url = HttpUrl.parse(serverConfiguration.faaApplicationSchemaEndpoint);
        }

        HttpRequest request = new HttpRequest();
        request.getParameters = getParameters;
        request.requestType = HttpRequest.RequestType.Get;
        return request;
    }

    public HttpRequest getUqhpSchema(){
        GetParameters getParameters = new GetParameters();
        if (serverConfiguration.uqhpApplicationSchemaEndpoint.substring(0, 4).toLowerCase().compareTo("http") == 0){
            getParameters.url = HttpUrl.parse(serverConfiguration.uqhpApplicationSchemaEndpoint);
        }

        HttpRequest request = new HttpRequest();
        request.getParameters = getParameters;
        request.requestType = HttpRequest.RequestType.Get;
        return request;
    }

    public HttpRequest getHavenApplication(UqhpApplication family){
        PostParameters postParameters = new PostParameters();
        postParameters.url = HttpUrl.parse(serverConfiguration.uqhpDeterminationUrl);

        HttpRequest request = new HttpRequest();
        request.postParameters = postParameters;
        request.requestType = HttpRequest.RequestType.Post;

        String json = new Gson().toJson(family);
        request.postParameters.requestString = json;
        postParameters.requestBody = RequestBody.create(JSON, json);

        return request;
    }

    public HttpRequest getLoginRequest(Login login) {
        PostParameters postParameters = new PostParameters();
        postParameters.url = HttpUrl.parse(serverConfiguration.localLoginEndpoint);
        FormBody formBody = new FormBody.Builder()
                .build();
        String jsonString = (new Gson()).toJson(login);
        postParameters.requestBody = RequestBody.create(JSON, jsonString);
        postParameters.requestString = jsonString;

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.requestType = HttpRequest.RequestType.Post;
        httpRequest.postParameters = postParameters;
        return httpRequest;
    }


    public HttpRequest getResumeRequest(LocalDate effectiveDate) {
        GetParameters getParameters = new GetParameters();
        getParameters.url = HttpUrl.parse(serverConfiguration.statusUrl+"?year="+effectiveDate.getYear());
        HttpRequest request = new HttpRequest();
        request.requestType = HttpRequest.RequestType.Get;
        request.getParameters = getParameters;
        return request;
    }

    public HttpRequest getOpenEnrollmentRequest() {
        GetParameters getParameters = new GetParameters();
        getParameters.url = HttpUrl.parse(serverConfiguration.openEnrollmentStatusEndpoint);
        HttpRequest request = new HttpRequest();
        request.requestType = HttpRequest.RequestType.Get;
        request.getParameters = getParameters;
        return request;
    }

    public void populateLinks(Links links) {
        serverConfiguration.logoutUrl = links.get.logoutUrl;
        serverConfiguration.statusUrl = links.get.statusUrl;
        serverConfiguration.userCoverageUrl = links.get.userCoverageUrl;
        serverConfiguration.isDeployedUrl = links.get.isDeployedUrl;
        serverConfiguration.havenDeterminationUrl = links.get.havenDeterminationUrl;
        serverConfiguration.uqhpDeterminationUrl = links.get.uqhpDeterminationUrl;
        serverConfiguration.localLoginEndpoint = links.post.loginUrl;
        serverConfiguration.planChoiceUrl = links.post.planChoiceUrl;
    }

    public HttpRequest getEffectiveDateRequest() {
        return buildGet(new UrlSource() {
            @Override
            public String source() {
                return serverConfiguration.effectiveDateEndpoint;
            }
        });
    }

    private interface UrlSource {
        String source();
    }
    private HttpRequest buildGet(UrlSource urlSource){

        GetParameters getParameters = new GetParameters();
        getParameters.url = HttpUrl.parse(urlSource.source());
        HttpRequest request = new HttpRequest();
        request.requestType = HttpRequest.RequestType.Get;
        request.getParameters = getParameters;
        return request;
    }

    public HttpRequest getUqhpDetermination(final String eaid) {
        return buildGet(new UrlSource() {
            @Override
            public String source() {
                return serverConfiguration.uqhpDeterminationUrl + "?eaid=" + eaid;
            }
        });
        }
}
