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
                                 IServerConfigurationStorageHandler storageHandler) {
        super(devUrlHandler, connectionHandler, serverConfiguration, parser, dataCache, storageHandler);
        this.devUrlHandler = devUrlHandler;
        this.storageHandler = storageHandler;
    }

    @Override
    public void validateUserAndPassword(String accountName, String password, Boolean rememberMe) throws Exception {
        UrlHandler.GetParameters getParameters = devUrlHandler.getLoginUrlParameters();
        IConnectionHandler.GetReponse getReponse = connectionHandler.get(getParameters);
        devUrlHandler.processLoginPageReponse(getReponse);


        UrlHandler.PostParameters loginPostParameters = urlHandler.getLoginPostParameters(accountName.trim(), password);
        IConnectionHandler.PostResponse loginPostResponse = connectionHandler.postHttpURLConnection(loginPostParameters, accountName, password, rememberMe);
        urlHandler.processLoginReponse(accountName, password, rememberMe, loginPostResponse);
    }
}
