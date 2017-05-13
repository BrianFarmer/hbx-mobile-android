package org.dchbx.coveragehq;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.HttpUrl;

public interface IConnectionHandler {

    public static class PostResponse {
        public String body;
        public int responseCode;
        public HashMap<String, List<String>> cookies;
        public Map<String, List<String>> headers;
    }

    public static class PutResponse {
        public String body;
        public int responseCode;
        public HashMap<String, List<String>> cookies;
    }

    public static class GetResponse {
        public String body;
        public int responseCode;
        public HashMap<String, List<String>> cookies;
    }

    PutResponse put(UrlHandler.PutParameters putParameters) throws Exception;

    public PostResponse simplePostHttpURLConnection(UrlHandler.PostParameters postParameters, String accountName, String password, Boolean rememberMe) throws Exception;
    PostResponse postHttpURLConnection(UrlHandler.PostParameters postParameters, String accountName, String password, Boolean rememberMe) throws Exception;

    PostResponse post(HttpUrl url, FormBody formBody) throws Exception;

    PostResponse post(HttpUrl url, FormBody formBody, String cookie) throws Exception;

    PostResponse post(UrlHandler.PostParameters postParameters) throws IOException, CoverageException;
    GetResponse get(UrlHandler.GetParameters getParameters) throws IOException, CoverageException;
    GetResponse getHackedSSL(UrlHandler.GetParameters getParameters) throws IOException, CoverageException;
    String get(HttpUrl url, String cookie) throws Exception;
}
