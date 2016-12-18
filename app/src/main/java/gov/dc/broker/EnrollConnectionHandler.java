package gov.dc.broker;

import java.util.UUID;

import gov.dc.broker.models.Security.LoginResponse;
import gov.dc.broker.models.Security.SecurityAnswerResponse;
import okhttp3.FormBody;
import okhttp3.HttpUrl;

public class EnrollConnectionHandler extends ConnectionHandler {
    @Override
    HttpUrl getLoginUrl(ServerConfiguration serverConfiguration) {
        return new HttpUrl.Builder()
                .scheme(serverConfiguration.loginInfo.scheme)
                .host(serverConfiguration.loginInfo.host)
                .addPathSegments(serverConfiguration.loginPath)
                .port(serverConfiguration.loginInfo.port)
                .build();
    }

    @Override
    HttpUrl getSecurityAnswerUrl(ServerConfiguration serverConfiguration) {
        return new HttpUrl.Builder()
                .scheme(serverConfiguration.loginInfo.scheme)
                .host(serverConfiguration.loginInfo.host)
                .addPathSegments(serverConfiguration.securityAnswerPath)
                .port(serverConfiguration.loginInfo.port)
                .build();
    }

    @Override
    HttpUrl getBrokerAgencyUrl(ServerConfiguration serverConfiguration) {
        return new HttpUrl.Builder()
                .scheme(serverConfiguration.dataInfo.scheme)
                .host(serverConfiguration.dataInfo.host)
                .addPathSegments(serverConfiguration.employerListPath)
                .port(serverConfiguration.dataInfo.port)
                .build();
    }

    @Override
    HttpUrl getEmployerDetailsUrl(ServerConfiguration serverConfiguration) {
        return new HttpUrl.Builder()
                .scheme(serverConfiguration.dataInfo.scheme)
                .host(serverConfiguration.dataInfo.host)
                .addPathSegments(serverConfiguration.employerDetailPathForEmployer)
                .port(serverConfiguration.dataInfo.port)
                .build();
    }

    @Override
    HttpUrl getEmployerDetailsUrl(String employerId, ServerConfiguration serverConfiguration) {
        return new HttpUrl.Builder()
                .scheme(serverConfiguration.dataInfo.scheme)
                .host(serverConfiguration.dataInfo.host)
                .addPathSegments(serverConfiguration.employerDetailPathForBroker)
                .port(serverConfiguration.dataInfo.port)
                .build();
    }

    @Override
    HttpUrl getEmployerRosterUrl(ServerConfiguration serverConfiguration) {
        return new HttpUrl.Builder()
                .scheme(serverConfiguration.dataInfo.scheme)
                .host(serverConfiguration.dataInfo.host)
                .addPathSegments(serverConfiguration.employerRosterPathForEmployer)
                .port(serverConfiguration.dataInfo.port)
                .build();
    }

    @Override
    HttpUrl getEmployerRosterUrl(String employerId, ServerConfiguration serverConfiguration) {
        return new HttpUrl.Builder()
                .scheme(serverConfiguration.dataInfo.scheme)
                .host(serverConfiguration.dataInfo.host)
                .addPathSegments(String.format(serverConfiguration.employerRosterPathForBroker, employerId))
                .port(serverConfiguration.loginInfo.port)
                .build();
    }

    @Override
    HttpUrl getCarriersUrl(ServerConfiguration serverConfiguration) {
        return new HttpUrl.Builder()
                .scheme(serverConfiguration.carrierInfo.scheme)
                .host(serverConfiguration.carrierInfo.host)
                .addPathSegments(serverConfiguration.carrierPath)
                .port(serverConfiguration.carrierInfo.port)
                .build();
    }

    @Override
    FormBody getSecurityAnswerFormBody(ServerConfiguration serverConfiguration, String securityAnswer) {
        return new FormBody.Builder()
                .add("security_answer", serverConfiguration.securityAnswer)
                .build();
    }

    @Override
    public void processSecurityResponse(ServerConfiguration serverConfiguration, SecurityAnswerResponse securityAnswerResponse) {
        serverConfiguration.enrollServer = securityAnswerResponse.enroll_server;
        serverConfiguration.sessionId = securityAnswerResponse.session_id;
    }

    @Override
    String getSessionCookies(ServerConfiguration serverConfiguration) {
        return "_session_id=" + serverConfiguration.sessionId;
    }

    @Override
    FormBody getLoginBody(String accountName, String password, Boolean rememberMe, ServerConfiguration serverConfiguration) {
        return new FormBody.Builder()
                .add("userid", accountName)
                .add("pass", password)
                .add("device_id", UUID.randomUUID().toString())
                .build();
    }

    @Override
    public void processLoginReponse(String accountName, String password, Boolean rememberMe, LoginResponse loginResponse, String location, ServerConfiguration serverConfiguration) {
        serverConfiguration.securityQuestion = loginResponse.security_question;
        serverConfiguration.location = location;
        serverConfiguration.accountName = accountName;
        serverConfiguration.password = password;
        serverConfiguration.rememberMe = rememberMe;
    }
}