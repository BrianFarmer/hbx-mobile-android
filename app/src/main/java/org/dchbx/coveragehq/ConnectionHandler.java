package org.dchbx.coveragehq;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by plast on 12/20/2016.
 */

public class ConnectionHandler implements IConnectionHandler{

    private static String TAG = "ConnectionHandler";
    private final ServerConfiguration serverConfiguration;

    public ConnectionHandler(ServerConfiguration serverConfiguration){
        this.serverConfiguration = serverConfiguration;
    }


    private OkHttpClient clientHttp = new OkHttpClient()
            .newBuilder()
            .followRedirects(true)
            .build();

    private OkHttpClient clientDontFollowHttp = new OkHttpClient()
            .newBuilder()
            .followRedirects(false)
            .followSslRedirects(false)
            .build();

    public PostResponse post(HttpUrl url, FormBody formBody) throws Exception {
        return post(url, formBody, null);
    }

    public PostResponse post(HttpUrl url, FormBody formBody, String cookie) throws Exception {
        Request.Builder builder = new Request.Builder()
                .url(url);
        if (cookie != null) {
            builder = builder.header("cookie", cookie);
        }
        Request request = builder.post(formBody)
                .build();

        Response response = getClient(url.scheme(), true).newCall(request).execute();

        int code = response.code();
        if (code < 200
                || code > 299) {
            //|| response.header("location", null) == null) {
            throw new Exception("error validing =login");
        }
        PostResponse postResponse = new PostResponse();
        postResponse.body =response.body().string();
        postResponse.headers = response.headers().toMultimap();
        return postResponse;
    }

    protected OkHttpClient getClient(String scheme, boolean follow) {
        if (follow){
            return  new OkHttpClient()
                    .newBuilder()
                    .followRedirects(true)
                    .build();
            //return clientHttp;
        } else {
            return clientDontFollowHttp;
        }
    }

    public static SSLContext getSSLContext() {
        try {
            String certString = BrokerApplication.getBrokerApplication().getResources().getString(R.string.hbxroot);
            ByteArrayInputStream is = new ByteArrayInputStream(certString.getBytes());


// Load CAs from an InputStream
// (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = null;
            cf = CertificateFactory.getInstance("X.509");
// From https://www.washington.edu/itconnect/security/ca/load-der.crt
            Certificate ca = null;
            ca = cf.generateCertificate(is);
            Log.d(TAG, "ca=" + ((X509Certificate) ca).getSubjectDN());
            is.close();

// Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

// Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

// Create an SSLContext that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            return sslContext;
        } catch (Exception e){
            Log.d(TAG, "Eating exception!!!", e);
        }
        return null;
    }

    private String getQuery(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> pair : params.entrySet())
        {
            if (first)
                first = false;
            else
                result.append("&");

            try {

                result.append(URLEncoder.encode(pair.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
            } catch (Exception e){
                Log.e(TAG, "getquery", e);
                throw e;
            }
        }

        return result.toString();
    }

    String getSessionCookie(HttpURLConnection connection) throws Exception {
        return getSessionCookie(connection.getHeaderFields());
    }

    String getSessionCookie(Map<String, List<String>> headerFields) throws Exception {
        List<String> strings = headerFields.get("Set-Cookie");
        for (String cookie : strings) {
            String[] split = cookie.split(Pattern.quote("="));
            if (split[0].equals("_session_id")){
                String[] split1 = split[1].split(Pattern.quote(";"));
                return split1[0];
            }
        }
        throw new Exception("Session cookie not found");
    }

    public PostResponse simplePostHttpURLConnection(UrlHandler.PostParameters postParameters, String accountName, String password, Boolean rememberMe) throws Exception {
        URL url = new URL(postParameters.url.toString());//postParameters.url.url();
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        if (postParameters.url.scheme().compareToIgnoreCase("https") == 0){
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection)connection;
            httpsURLConnection.setSSLSocketFactory(getSSLContext().getSocketFactory());
        }
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod("POST");

        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");


        String output = getQuery(postParameters.formParameters);
        //connection.setRequestProperty("Content-Length", Integer.toString(output.length()));

        OutputStream os;
        try {
            os = connection.getOutputStream();
        } catch (Throwable t){
            Log.e(TAG, "getting output stream", t);
            throw t;
        }
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(output);
        writer.flush();
        writer.close();
        os.close();

        connection.connect();
        int responseCode = connection.getResponseCode();

        PostResponse postResponse = new PostResponse();

        if (responseCode < 400) {

            while (responseCode == 302) {
                HashMap<String, List<String>> cookies = getCookies(connection.getHeaderFields());
                serverConfiguration.sessionId = cookies.get("_session_id").get(0);
                String newSessionId = "_session_id=" + cookies.get("_session_id").get(0);
                String redirectUrlString = connection.getHeaderField("location");
                URL redirectUrl = new URL(redirectUrlString);
                connection = (HttpURLConnection) redirectUrl.openConnection();
                if (redirectUrl.getProtocol().compareToIgnoreCase("https") == 0) {
                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) connection;
                    httpsURLConnection.setSSLSocketFactory(getSSLContext().getSocketFactory());
                }
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod("GET");

                connection.setRequestProperty("Cookie", serverConfiguration.sessionId);
                connection.connect();
                responseCode = connection.getResponseCode();
            }


            InputStream inputStream = connection.getInputStream();
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(streamReader);
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }

            String string = result.toString();
            Log.d(TAG, "body: " + string);
            postResponse.body = string;
        }

        Log.d(TAG, "response code;" + responseCode);
        postResponse.headers = connection.getHeaderFields();
        ArrayList<String> list = new ArrayList<>();
        Map<String, List<String>> headerFields = connection.getHeaderFields();
        postResponse.cookies = getCookies(headerFields);
        postResponse.responseCode = responseCode;
        return postResponse;
    }

    public PostResponse postHttpURLConnection(UrlHandler.PostParameters postParameters, String accountName, String password, Boolean rememberMe) throws Exception {
        URL url = new URL(postParameters.url.toString());//postParameters.url.url();
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        if (postParameters.url.scheme().compareToIgnoreCase("https") == 0){
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection)connection;
            httpsURLConnection.setSSLSocketFactory(getSSLContext().getSocketFactory());
        }
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");

        connection.setRequestProperty("Cookie", "_session_id=" + serverConfiguration.sessionId);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        HashMap<String, String> params = new HashMap<>();
        params.put("utf8", "✓");
        params.put("authenticity_token", serverConfiguration.authenticityToken);
        params.put("user[login]", accountName);
        params.put("user[password]", password);
        params.put("user[remember_me]", rememberMe?"1":"0");
        params.put("commit", "Sign in");
        String output = getQuery(params);
        //connection.setRequestProperty("Content-Length", Integer.toString(output.length()));

        OutputStream os;
        try {
            os = connection.getOutputStream();
        } catch (Throwable t){
            Log.e(TAG, "getting output stream", t);
            throw t;
        }
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(output);
        writer.flush();
        writer.close();
        os.close();

        connection.connect();
        int responseCode = connection.getResponseCode();

        if (responseCode != 302){
            return null;
        }

        while (responseCode == 302){
            HashMap<String, List<String>> cookies = getCookies(connection.getHeaderFields());
            serverConfiguration.sessionId = cookies.get("_session_id").get(0);
            String newSessionId = "_session_id=" + cookies.get("_session_id").get(0);
            String redirectUrlString = connection.getHeaderField("location");
            URL redirectUrl = new URL(redirectUrlString);
            connection = (HttpURLConnection)redirectUrl.openConnection();
            if (redirectUrl.getProtocol().compareToIgnoreCase("https") == 0){
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection)connection;
                httpsURLConnection.setSSLSocketFactory(getSSLContext().getSocketFactory());
            }
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");

            connection.setRequestProperty("Cookie", serverConfiguration.sessionId);
            connection.connect();
            responseCode = connection.getResponseCode();
        }


        InputStream inputStream = connection.getInputStream();
        InputStreamReader streamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(streamReader);
        StringBuilder result = new StringBuilder();
        String line;
        while((line = bufferedReader.readLine()) != null) {
            result.append(line);
        }

        String string = result.toString();
        Log.d (TAG, "body: " + string);

        Log.d(TAG, "response code;" + responseCode);

        PostResponse postResponse = new PostResponse();
        postResponse.headers = connection.getHeaderFields();
        postResponse.body = string;
        ArrayList<String> list = new ArrayList<>();
        Map<String, List<String>> headerFields = connection.getHeaderFields();
        postResponse.cookies = getCookies(headerFields);
        postResponse.responseCode = responseCode;
        return postResponse;
    }

    public PostResponse postLogin(UrlHandler.PostParameters postParameters) throws IOException, CoverageException {

        Request.Builder builder = new Request.Builder()
                .url(postParameters.url);

        /*
        if (postParameters.cookies != null) {
            for (String key : postParameters.cookies.keySet()) {
                builder.header("cookie", key + "=" + postParameters.cookies.get(key));
            }
        }*/

        if (postParameters.headers != null){
            for (String key : postParameters.headers.keySet()) {
                builder.header(key, postParameters.headers.get(key));
            }

        }
        Request request = builder.post(postParameters.body)
                .build();

        Response response;
        try {
            response = getClient(postParameters.url.scheme(), true).newCall(request).execute();
        } catch (Throwable t){
            Log.e(TAG, "exception during post", t);
            throw t;
        }

        PostResponse postResponse = new PostResponse();
        postResponse.body = response.body().string();
        postResponse.headers = response.headers().toMultimap();
        postResponse.responseCode = response.code();
        postResponse.cookies = getCookies(response.headers().toMultimap());
        return postResponse;
    }

    public PostResponse post(UrlHandler.PostParameters postParameters) throws IOException, CoverageException {
        Request.Builder builder = new Request.Builder()
                .url(postParameters.url);

        /*
        if (postParameters.cookies != null) {
            for (String key : postParameters.cookies.keySet()) {
                builder.header("cookie", key + "=" + postParameters.cookies.get(key));
            }
        }*/

        if (postParameters.headers != null){
            for (String key : postParameters.headers.keySet()) {
                builder.header(key, postParameters.headers.get(key));
            }

        }
        Request request = builder.post(postParameters.body)
                .build();

        Response response;
        try {
            response = getClient(postParameters.url.scheme(), true).newCall(request).execute();
        } catch (Throwable t){
            Log.e(TAG, "exception during post", t);
            throw t;
        }

        PostResponse postResponse = new PostResponse();
        postResponse.body = response.body().string();
        postResponse.headers = response.headers().toMultimap();
        postResponse.responseCode = response.code();
        postResponse.cookies = getCookies(response.headers().toMultimap());
        return postResponse;
    }

    public PutResponse put(UrlHandler.PutParameters putParameters) throws IOException, CoverageException {
        Request.Builder builder = new Request.Builder()
                .url(putParameters.url);

        if (putParameters.cookies != null) {
            for (String key : putParameters.cookies.keySet()) {
                builder.header("cookie", key + "=" + putParameters.cookies.get(key));
            }
        }
        if (putParameters.headers != null){
            for (String key : putParameters.headers.keySet()) {
                builder.header(key, putParameters.headers.get(key));
            }
        }
        Request request = builder.put(putParameters.body).build();

        Response response = getClient(putParameters.url.scheme(), true).newCall(request).execute();

        PutResponse putResponse = new PutResponse();
        putResponse.body =response.body().string();
        putResponse.responseCode = response.code();
        putResponse.cookies = getCookies(response.headers().toMultimap());
        return putResponse;
    }

    @Override
    public GetReponse get(UrlHandler.GetParameters getParameters) throws IOException, CoverageException {
        if (getParameters == null){
            throw new IllegalArgumentException  ("getParameters is null");
        }
        if (getParameters.url == null
            || getParameters.url.toString().length() == 0){
            throw new IllegalArgumentException  ("url is empty or null");
        }

        Request.Builder builder = new Request.Builder()
                .url(getParameters.url);

        if (getParameters.cookies != null) {
            for (Map.Entry<String, String> entry : getParameters.cookies.entrySet()) {
                builder = builder.header("Cookie", entry.getKey() + "=" + entry.getValue());
                Log.d(TAG, "cookie value:->" + entry.getValue() + "<-");
            }
        }

        Request request = builder.get().build();
        Response response = getClient(getParameters.url.scheme(), false).newCall(request).execute();


        /*OkHttpClient client = new OkHttpClient()
                .newBuilder()
                //.followRedirects(true)
                .build();

        client.newCall(request).execute();*/

        if (response.code() != 200){
            throw new CoverageException("error getting: " + getParameters.url.toString());
        }

        GetReponse getReponse = new GetReponse();
        getReponse.responseCode = response.code();
        getReponse.body = response.body().string();
        getReponse.cookies = getCookies(response.headers().toMultimap());

        return getReponse;
    }


    public String get(HttpUrl url, String cookie) throws Exception {
        Request.Builder builder = new Request.Builder()
                .url(url);

        if (cookie != null) {
            builder = builder.header("cookie", cookie);
        }
        Request request = builder.get().build();
        Response response = getClient(url.scheme(), true).newCall(request).execute();

        if (response.code() != 200){
            throw new Exception("error getting session");
        }

        return response.body().string();
    }

    private HashMap<String, List<String>> getCookies(Map<String, List<String>> headers){
        HashMap<String, List<String>> map = new HashMap<>();
        for (Map.Entry<String, List<String>> entry: headers.entrySet()) {
            if (entry.getKey() != null
                && entry.getKey().compareToIgnoreCase("set-cookie") == 0) {
                for (String headerValue : entry.getValue()) {
                    String[] cookieParts = headerValue.split(Pattern.quote(";"));
                    if (cookieParts.length >= 1){
                        String[] cookieNameAndValue = cookieParts[0].split(Pattern.quote("="));
                        if (cookieNameAndValue.length == 2){
                            if (map.containsKey(cookieNameAndValue[0])) {
                                map.get(cookieNameAndValue[0]).add(cookieNameAndValue[1]);
                            } else {
                                ArrayList<String> values = new ArrayList<>();
                                values.add(cookieNameAndValue[1]);
                                map.put(cookieNameAndValue[0], values);
                            }
                        }
                    }
                }
            }
        }
        return map;
    }
}

