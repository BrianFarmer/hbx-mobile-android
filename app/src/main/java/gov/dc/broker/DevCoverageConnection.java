package gov.dc.broker;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by plast on 12/22/2016.
 */

public class DevCoverageConnection extends CoverageConnection {
    private static String TAG = "DevCoverageConnection";
    private final DevUrlHandler devUrlHandler;

    public DevCoverageConnection(DevUrlHandler devUrlHandler, IConnectionHandler connectionHandler, ServerConfiguration serverConfiguration, JsonParser parser, IDataCache dataCache) {
        super(devUrlHandler, connectionHandler, serverConfiguration, parser, dataCache);
        this.devUrlHandler = devUrlHandler;
    }

    @Override
    public void validateUserAndPassword(String accountName, String password, Boolean rememberMe) throws Exception {
            UrlHandler.GetParameters getParameters = devUrlHandler.getLoginUrlParameters();

            IConnectionHandler.GetReponse getReponse = connectionHandler.get(getParameters);

            String sessionId = getReponse.cookies.get("_session_id").get(0);
            Pattern pattern = Pattern.compile("<meta name=\\\"csrf-token\\\" content=\\\"([^\"]+)\\\"");
            Matcher matcher = pattern.matcher(getReponse.body);
            String authenticityToken = null;
            if (matcher.find()){
                authenticityToken = matcher.group(1);
            } else {
                throw new CoverageException("error getting authenticity token");
            }
            Log.d(TAG, "got session id");


            UrlHandler.PostParameters loginPostParameters = urlHandler.getLoginPostParameters(accountName, password, sessionId, authenticityToken);
            IConnectionHandler.PostResponse loginPostResponse = connectionHandler.postHttpURLConnection(loginPostParameters, accountName, password, rememberMe, sessionId, authenticityToken);
            urlHandler.processLoginReponse(accountName, password, rememberMe, loginPostResponse);
    }
}
