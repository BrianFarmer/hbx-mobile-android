package gov.dc.broker;

/**
 * Created by plast on 12/23/2016.
 */
public class EnrollCoverageConnection extends CoverageConnection {
    private static String TAG = "EnrollCvrgConnection";
    private final EnrollUrlHandler enrollUrlHandler;

    public EnrollCoverageConnection(EnrollUrlHandler urlHandler, IConnectionHandler connectionHandler, ServerConfiguration serverConfiguration, JsonParser parser, IDataCache dataCache) {
        super(urlHandler, connectionHandler, serverConfiguration, parser, dataCache);
        this.enrollUrlHandler = urlHandler;
    }

    @Override
    public void validateUserAndPassword(String accountName, String password, Boolean rememberMe) throws Exception {
        UrlHandler.PostParameters loginPostParameters = enrollUrlHandler.getLoginPostParameters(accountName, password, null, null);
        IConnectionHandler.PostResponse postResponse = connectionHandler.post(loginPostParameters);
        enrollUrlHandler.processLoginReponse(accountName, password, rememberMe, postResponse);
    }

    public void checkSecurityAnswer(String securityAnswer) throws Exception {
        UrlHandler.PutParameters securityAnswerPutParameters = urlHandler.getSecurityAnswerPutParameters(securityAnswer);
        ConnectionHandler.PutResponse putResponse = connectionHandler.put(securityAnswerPutParameters);
        urlHandler.processSecurityAnswerResponse(putResponse);
    }
}
