package gov.dc.broker;

import gov.dc.broker.models.Security.LoginResponse;
import gov.dc.broker.models.Security.SecurityAnswerResponse;
import okhttp3.FormBody;
import okhttp3.HttpUrl;

/**
 * Created by plast on 12/15/2016.
 */

public abstract class ConnectionHandler {
    abstract HttpUrl getLoginUrl(ServerConfiguration serverConfiguration);
    abstract HttpUrl getSecurityAnswerUrl(ServerConfiguration serverConfiguration);
    abstract HttpUrl getBrokerAgencyUrl(ServerConfiguration serverConfiguration);
    abstract HttpUrl getEmployerDetailsUrl(ServerConfiguration serverConfiguration);
    abstract HttpUrl getEmployerDetailsUrl(String employerId, ServerConfiguration serverConfiguration);
    abstract HttpUrl getEmployerRosterUrl(ServerConfiguration serverConfiguration);
    abstract HttpUrl getEmployerRosterUrl(String employerId, ServerConfiguration serverConfiguration);
    abstract HttpUrl getCarriersUrl(ServerConfiguration serverConfiguration);
    abstract FormBody getSecurityAnswerFormBody(ServerConfiguration serverConfiguration, String securityAnswer);
    abstract void processSecurityResponse(ServerConfiguration serverConfiguration, SecurityAnswerResponse securityAnswerResponse);
    abstract String getSessionCookies(ServerConfiguration serverConfiguration);
    abstract FormBody getLoginBody(String accountName, String password, Boolean rememberMe,
                                          ServerConfiguration serverConfiguration);
    public abstract void processLoginReponse(String accountName, String password, Boolean rememberMe, LoginResponse loginResponse, String location, ServerConfiguration serverConfiguration);
}
