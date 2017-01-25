package gov.dc.broker;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.StringJoiner;

import gov.dc.broker.models.gitaccounts.GitAccounts;
import okhttp3.HttpUrl;

/**
 * Created by plast on 12/22/2016.
 */
public class GithubCoverageConnection extends CoverageConnection {
    private static String TAG = "GithubCoverage";
    private GitUrlHandler gitUrlHandler;

    public GithubCoverageConnection(GitUrlHandler urlHandler, ConnectionHandler connectionHandler,
                                    ServerConfiguration serverConfiguration, JsonParser parser,
                                    IDataCache dataCache, IServerConfigurationStorageHandler configurationStorageHandler) {
        super(urlHandler, connectionHandler, serverConfiguration, parser, dataCache, configurationStorageHandler);
        gitUrlHandler = urlHandler;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean validateUserAndPassword(String accountName, String password, Boolean rememberMe) throws Exception {
        HttpUrl parsed = HttpUrl.parse(accountName + "/" + password);
        serverConfiguration.dataInfo.scheme = parsed.scheme();
        serverConfiguration.dataInfo.host = parsed.host();
        if (rememberMe) {
            StringBuilder builder = new StringBuilder();

            boolean first = true;
            for (String s : parsed.pathSegments()) {
                if (!first){
                    builder.append("/");
                } else {
                    first = false;
                }
                builder.append(s);
            }
            String path = builder.toString();

            serverConfiguration.brokerDetailPath = path  + "/broker_details.json";
            serverConfiguration.employerDetailPath = path;
            serverConfiguration.employerRosterPathForBroker = path;
        } else {
            serverConfiguration.employerDetailPath = parsed.encodedPath() + "/" + password + "/employer.json";
            serverConfiguration.employerRosterPathForBroker = parsed.encodedPath() + "/" + password + "/roster.json";
        }
        return true;
    }

    @Override
    protected void checkSessionId() throws Exception, CoverageException {
        Log.d(TAG, "checkSessionId always true for github data");
    }

    @Override
    public GitAccounts getGitAccounts(String urlRoot) throws Exception {
        UrlHandler.GetParameters getParameters = gitUrlHandler.getGitAccountGetParameters(urlRoot);
        IConnectionHandler.GetReponse getReponse = connectionHandler.get(getParameters);
        return gitUrlHandler.processGetGitAccounts(getReponse, urlRoot);
    }
}
