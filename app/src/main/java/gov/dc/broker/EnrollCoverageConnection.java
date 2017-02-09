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
    public LoginResult validateUserAndPassword(String accountName, String password, Boolean rememberMe, boolean useFingerprintSensor) throws Exception {
        UrlHandler.PostParameters loginPostParameters = enrollUrlHandler.getLoginPostParameters(accountName, password);
        IConnectionHandler.PostResponse postResponse = connectionHandler.simplePostHttpURLConnection(loginPostParameters, accountName, password, rememberMe);
        //IConnectionHandler.PostResponse postResponse = connectionHandler.post(loginPostParameters);
        return enrollUrlHandler.processLoginReponse(accountName, password, rememberMe, postResponse, useFingerprintSensor);
    }


    @Override
    public LoginResult revalidateUserAndPassword() throws Exception {
        UrlHandler.PostParameters loginPostParameters = enrollUrlHandler.getLoginPostParameters(serverConfiguration.accountName, serverConfiguration.password);
        IConnectionHandler.PostResponse postResponse = connectionHandler.simplePostHttpURLConnection(loginPostParameters, serverConfiguration.accountName, serverConfiguration.password, serverConfiguration.rememberMe);
        return enrollUrlHandler.processLoginReponse(serverConfiguration.accountName, serverConfiguration.password, true, postResponse, true);
    }

    public void checkSecurityAnswer(String securityAnswer) throws Exception {
        UrlHandler.PostParameters securityAnswerPutParameters = enrollUrlHandler.getSecurityAnswerPostParameters(securityAnswer);
        ConnectionHandler.PostResponse postResponse = connectionHandler.post(securityAnswerPutParameters);
        enrollUrlHandler.processSecurityAnswerResponse(postResponse);
        storageHandler.store(serverConfiguration);
    }

    public void stayLoggedIn() throws Exception {
        UrlHandler.GetParameters stayLoggedInParameters = enrollUrlHandler.getStayLoggedInParameters();
        enrollUrlHandler.processStayLoggedInResponse(connectionHandler.get(stayLoggedInParameters));
    }
}
