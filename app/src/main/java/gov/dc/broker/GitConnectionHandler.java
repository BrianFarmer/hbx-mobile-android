package gov.dc.broker;

import gov.dc.broker.models.Security.LoginResponse;
import gov.dc.broker.models.Security.SecurityAnswerResponse;
import okhttp3.FormBody;
import okhttp3.HttpUrl;

public class GitConnectionHandler extends ConnectionHandler {
    @Override
    HttpUrl getLoginUrl(ServerConfiguration serverConfiguration) {
        return null;
    }

    @Override
    HttpUrl getSecurityAnswerUrl(ServerConfiguration serverConfiguration) {
        return null;
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
                .addPathSegments(serverConfiguration.employerListPath)
                .port(serverConfiguration.dataInfo.port)
                .build();
    }

    @Override
    HttpUrl getEmployerDetailsUrl(String employerId, ServerConfiguration serverConfiguration) {
        return HttpUrl.parse(employerId);
    }

    @Override
    HttpUrl getEmployerRosterUrl(ServerConfiguration serverConfiguration) {
        return null;
    }

    @Override
    HttpUrl getEmployerRosterUrl(String employerId, ServerConfiguration serverConfiguration) {
        return HttpUrl.parse(employerId);
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
        return null;
    }

    @Override
    public void processSecurityResponse(ServerConfiguration serverConfiguration, SecurityAnswerResponse securityAnswerResponse) {
    }

    @Override
    String getSessionCookies(ServerConfiguration serverConfiguration) {
        return null;
    }

    @Override
    FormBody getLoginBody(String accountName, String password, Boolean rememberMe, ServerConfiguration serverConfiguration) {
        return null;
    }

    @Override
    public void processLoginReponse(String accountName, String password, Boolean rememberMe, LoginResponse loginResponse, String location, ServerConfiguration serverConfiguration) {
        serverConfiguration.accountName = accountName;
        serverConfiguration.password = password;
        serverConfiguration.rememberMe = rememberMe;
    }
}
