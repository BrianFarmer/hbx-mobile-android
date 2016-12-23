package gov.dc.broker;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.HttpUrl;

/**
 * Created by plast on 12/22/2016.
 */

public class DevCoverageConnection extends CoverageConnection {
    private static String TAG = "DevCoverageConnection";

    public DevCoverageConnection(UrlHandler urlHandler, IConnectionHandler connectionHandler, ServerConfiguration serverConfiguration, JsonParser parser, IDataCache dataCache) {
        super(urlHandler, connectionHandler, serverConfiguration, parser, dataCache);
    }

    @Override
    public void validateUserAndPassword(String accountName, String password, Boolean rememberMe) throws Exception {
            HttpUrl loginUrl = urlHandler.getLoginUrl();
            // if getLoginUrl returns null it means there is no security to worry about.
            // main this is here to support data in github.
            if (loginUrl == null){
                urlHandler.processLoginReponse(accountName, password, rememberMe, null);
                return;
            }

            HashMap<String, ArrayList<String>> cookies = new HashMap<>();
            String response = connectionHandler.get(loginUrl, null, cookies);

            String sessionId = cookies.get("_session_id").get(0);
            Pattern pattern = Pattern.compile("<meta name=\\\"csrf-token\\\" content=\\\"([^\"]+)\\\"");
            Matcher matcher = pattern.matcher(response);
            String authenticityToken = null;
            if (matcher.find()){
                authenticityToken = matcher.group(1);
            } else {
                throw new CoverageException("error getting authenticity token");
            }
            Log.d(TAG, "got session id");


            UrlHandler.PostParameters loginPostParameters = urlHandler.getLoginPostParameters(accountName, password, sessionId, authenticityToken);
            IConnectionHandler.PostResponse loginPostResponse = connectionHandler.postHttpURLConnection(loginPostParameters, accountName, password, rememberMe, sessionId, authenticityToken);
            if (loginPostResponse.responseCode >= 400) {
                throw new CoverageException("error validating user and password");
            }
            urlHandler.processLoginReponse(accountName, password, rememberMe, loginPostResponse);
    }
}
