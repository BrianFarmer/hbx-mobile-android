package gov.dc.broker;

/**
 * Created by plast on 12/22/2016.
 */

public class DevCoverageConnection extends CoverageConnection {
    private static String TAG = "DevCoverageConnection";
    private final DevUrlHandler devUrlHandler;
    private final IServerConfigurationStorageHandler storageHandler;

    public DevCoverageConnection(DevUrlHandler devUrlHandler, IConnectionHandler connectionHandler,
                                 ServerConfiguration serverConfiguration, JsonParser parser, IDataCache dataCache,
                                 IServerConfigurationStorageHandler clearStorageHandler) {
        super(devUrlHandler, connectionHandler, serverConfiguration, parser, dataCache, clearStorageHandler);
        this.devUrlHandler = devUrlHandler;
        this.storageHandler = clearStorageHandler;
    }

    @Override
    public LoginResult validateUserAndPassword(String accountName, String password, Boolean rememberMe, boolean useFingerprintSensor) throws Exception {
        UrlHandler.GetParameters getParameters = devUrlHandler.getLoginUrlParameters();
        IConnectionHandler.GetReponse getReponse = connectionHandler.get(getParameters);
        devUrlHandler.processLoginPageReponse(getReponse);

        UrlHandler.PostParameters loginPostParameters = urlHandler.getLoginPostParameters(accountName.trim(), password);
        IConnectionHandler.PostResponse loginPostResponse = connectionHandler.post(loginPostParameters);
        return urlHandler.processLoginReponse(accountName, password, rememberMe, loginPostResponse, useFingerprintSensor);
    }

    @Override
    public LoginResult revalidateUserAndPassword() throws Exception {
        UrlHandler.GetParameters getParameters = devUrlHandler.getLoginUrlParameters();
        IConnectionHandler.GetReponse getReponse = connectionHandler.get(getParameters);
        devUrlHandler.processLoginPageReponse(getReponse);

        UrlHandler.PostParameters loginPostParameters = urlHandler.getLoginPostParameters(serverConfiguration.accountName.trim(), serverConfiguration.password);
        IConnectionHandler.PostResponse loginPostResponse = connectionHandler.post(loginPostParameters);
        return urlHandler.processLoginReponse(serverConfiguration.accountName, serverConfiguration.password, true, loginPostResponse, true);
    }
}
