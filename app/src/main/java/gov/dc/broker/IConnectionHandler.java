package gov.dc.broker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.HttpUrl;

public interface IConnectionHandler {

    public static class PostResponse {
        public String body;
        public int responseCode;
        public HashMap<String, ArrayList<String>> cookies;
        public Map<String, List<String>> headers;
    }

    public static class PutResponse {
        public String body;
        public int responseCode;
        public HashMap<String, ArrayList<String>> cookies;
    }


    public static class GetReponse {
        String body;
        HashMap<String, List<String>> cookies;
    }

    PutResponse put(UrlHandler.PutParameters putParameters) throws Exception;

    PostResponse post(HttpUrl url, FormBody formBody) throws Exception;

    PostResponse post(HttpUrl url, FormBody formBody, String cookie) throws Exception;

    PostResponse post(UrlHandler.PostParameters postParameters) throws IOException, CoverageException;
    PostResponse postHttpURLConnection(UrlHandler.PostParameters loginPostParameters, String accountName, String password, Boolean rememberMe, String sessionId, String authenticityToken) throws Exception;

    String get(UrlHandler.GetParameters getParameters, HashMap<String, ArrayList<String>> responseCookies) throws IOException, CoverageException;

    String get(HttpUrl url) throws Exception;

    String get(HttpUrl url, String cookie) throws Exception;

    String get(HttpUrl url, String cookie, HashMap<String, ArrayList<String>> responsseCookies) throws Exception;
}
