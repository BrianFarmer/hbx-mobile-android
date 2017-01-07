package gov.dc.broker;

import android.util.Log;

/**
 * Created by plast on 12/22/2016.
 */
public class GithubCoverageConnection extends CoverageConnection {
    private static String TAG = "GithubCoverage";

    public GithubCoverageConnection(UrlHandler urlHandler, ConnectionHandler connectionHandler,
                                    ServerConfiguration serverConfiguration, JsonParser parser,
                                    IDataCache dataCache, IServerConfigurationStorageHandler configurationStorageHandler) {
        super(urlHandler, connectionHandler, serverConfiguration, parser, dataCache, configurationStorageHandler);
    }

    @Override
    public void validateUserAndPassword(String accountName, String password, Boolean rememberMe) throws Exception {
        serverConfiguration.accountName = accountName;
        serverConfiguration.password = password;
        serverConfiguration.rememberMe = rememberMe;
    }

    @Override
    protected void checkSessionId() throws Exception, CoverageException {
        Log.d(TAG, "checkSessionId always true for github data");
    }
}
