package gov.dc.broker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.HttpUrl;

public interface IConnectionHandler {

    public static class PostResponse {
        public String location;
        public String body;
        public int responseCode;
        public HashMap<String, ArrayList<String>> cookies;
    }

    public static class GetReponse {
        String body;
        HashMap<String, List<String>> cookies;
    }

    PostResponse post(HttpUrl url, FormBody formBody) throws Exception;

    PostResponse post(HttpUrl url, FormBody formBody, String cookie) throws Exception;

    PostResponse post(UrlHandler.PostParameters postParameters) throws IOException, CoverageException;
    PostResponse postHttpURLConnection(UrlHandler.PostParameters loginPostParameters, String accountName, String password, Boolean rememberMe, String sessionId, String authenticityToken) throws Exception;

    String get(UrlHandler.GetParameters getParameters, HashMap<String, ArrayList<String>> responseCookies) throws IOException, CoverageException;

    String get(HttpUrl url) throws Exception;

    String get(HttpUrl url, String cookie) throws Exception;

    String get(HttpUrl url, String cookie, HashMap<String, ArrayList<String>> responsseCookies) throws Exception;
}
