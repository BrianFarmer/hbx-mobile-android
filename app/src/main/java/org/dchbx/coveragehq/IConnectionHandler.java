package org.dchbx.coveragehq;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import okhttp3.FormBody;
import okhttp3.HttpUrl;

public interface IConnectionHandler {


    public static abstract class HttpResponse{
        public abstract int getResponseCode();
        public abstract String getBody();
    }

    public static class PostResponse extends HttpResponse{
        public String body;
        public int responseCode;
        public HashMap<String, List<String>> cookies;
        public Map<String, List<String>> headers;

        @Override
        public int getResponseCode() {
            return responseCode;
        }

        @Override
        public String getBody() {
            return body;
        }
    }

    public static class PutResponse  extends HttpResponse{
        public String body;
        public int responseCode;
        public HashMap<String, List<String>> cookies;

        @Override
        public int getResponseCode() {
            return responseCode;
        }

        @Override
        public String getBody() {
            return body;
        }
    }

    public static class GetResponse  extends HttpResponse{
        public String body;
        public int responseCode;
        public HashMap<String, List<String>> cookies;

        @Override
        public int getResponseCode() {
            return responseCode;
        }

        @Override
        public String getBody() {
            return body;
        }
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
    GetResponse simpleGet(UrlHandler.GetParameters getParameters) throws IOException;
    HttpResponse process(UrlHandler.HttpRequest request) throws Exception;
    void process(UrlHandler.HttpRequest request, ConnectionHandler.OnCompletion onCompletion) throws Exception;

    public static interface OnCompletion {
        void onCompletion(IConnectionHandler.HttpResponse response) throws NoSuchPaddingException, InvalidKeySpecException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidParameterSpecException, InvalidAlgorithmParameterException;
    }
}
