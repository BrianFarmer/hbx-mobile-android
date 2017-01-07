package gov.dc.broker;

/**
 * Created by plast on 12/23/2016.
 */
public class EnrollCoverageConnection extends CoverageConnection {
    private static String TAG = "EnrollCvrgConnection";
    private final EnrollUrlHandler enrollUrlHandler;

    public EnrollCoverageConnection(EnrollUrlHandler urlHandler, IConnectionHandler connectionHandler,
                                    ServerConfiguration serverConfiguration, JsonParser parser,
                                    IDataCache dataCache, ConfigurationStorageHandler storageHandler) {
        super(urlHandler, connectionHandler, serverConfiguration, parser, dataCache, storageHandler);
        this.enrollUrlHandler = urlHandler;
    }

    @Override
    public void validateUserAndPassword(String accountName, String password, Boolean rememberMe) throws Exception {
        UrlHandler.PostParameters loginPostParameters = enrollUrlHandler.getLoginPostParameters(accountName, password);
        IConnectionHandler.PostResponse postResponse = connectionHandler.post(loginPostParameters);
        enrollUrlHandler.processLoginReponse(accountName, password, rememberMe, postResponse);
    }

    public void checkSecurityAnswer(String securityAnswer) throws Exception {
        UrlHandler.PostParameters securityAnswerPutParameters = enrollUrlHandler.getSecurityAnswerPostParameters(securityAnswer);
        ConnectionHandler.PostResponse postResponse = connectionHandler.post(securityAnswerPutParameters);
        enrollUrlHandler.processSecurityAnswerResponse(postResponse);
        storageHandler.store(serverConfiguration);
    }
}
