package gov.dc.broker;

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
import okhttp3.Headers;
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

    private OkHttpClient client = new OkHttpClient()
            .newBuilder()
            .followRedirects(true)
            .build();

    private OkHttpClient clientDontFollow = new OkHttpClient()
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

        Response response = client.newCall(request).execute();

        int code = response.code();
        if (code < 200
                || code > 299) {
            //|| response.header("location", null) == null) {
            throw new Exception("error validing =login");
        }
        PostResponse postResponse = new PostResponse();
        postResponse.body =response.body().string();
        postResponse.location = response.header("location");
        return postResponse;
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

    public PostResponse postHttpURLConnection(UrlHandler.PostParameters postParameters, String accountName, String password, Boolean rememberMe, String sessionId, String authenticityToken) throws Exception {
        URL url = postParameters.url.url();
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        if (postParameters.url.scheme().compareToIgnoreCase("https") == 0){
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection)connection;
            httpsURLConnection.setSSLSocketFactory(getSSLContext().getSocketFactory());
        }
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");

        //connection.setRequestProperty("Host", siteConfig.host + siteConfig.port);
        //connection.setRequestProperty("Connection", "keep-alive");
        //connection.setRequestProperty("Pragma", "no-cache");
        //connection.setRequestProperty("Cache-Control", "no-cache");
        //connection.setRequestProperty("Origin", siteConfig.scheme + "://" + siteConfig.host + siteConfig.port);
        connection.setRequestProperty("Cookie", "_session_id=" + sessionId);
        //connection.setRequestProperty("DNT", "1");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        //connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        //connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        //connection.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
        //connection.setRequestProperty("Referer", siteConfig.scheme + "://" + siteConfig.host + siteConfig.port + "/users/sign_in");
        //connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
        //connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36");

        HashMap<String, String> params = new HashMap<>();
        params.put("utf8", "✓");
        params.put("authenticity_token", authenticityToken);
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
        if (responseCode != 302){
            throw new CoverageException("Bad email or password");
        }

        String newSessionId = getSessionCookie(connection);
        Log.d(TAG, "SessionId for processing: " + newSessionId);
        PostResponse postResponse = new PostResponse();
        postResponse.body = string;
        postResponse.cookies = new HashMap<>();
        ArrayList<String> list = new ArrayList<>();
        list.add(newSessionId);
        postResponse.responseCode = responseCode;
        postResponse.cookies.put("_session_id", list);
        return postResponse;
    }

    public PostResponse post(UrlHandler.PostParameters postParameters) throws IOException, CoverageException {
        Request.Builder builder = new Request.Builder()
                .url(postParameters.url);

        if (postParameters.cookies != null) {
            for (String key : postParameters.cookies.keySet()) {
                builder.header("cookie", key + "=" + postParameters.cookies.get(key));
            }
        }
        if (postParameters.headers != null){
            for (String key : postParameters.headers.keySet()) {
                builder.header(key, postParameters.headers.get(key));
            }

        }
        Request request = builder.post(postParameters.body)
                .build();

        Response response = client.newCall(request).execute();

        PostResponse postResponse = new PostResponse();
        postResponse.body =response.body().string();
        postResponse.location = response.header("location");
        postResponse.responseCode = response.code();
        postResponse.cookies = new HashMap<>();
        getCookies(response.headers(), postResponse.cookies);
        return postResponse;
    }

    @Override
    public String get(UrlHandler.GetParameters getParameters, HashMap<String, ArrayList<String>> responseCookies) throws IOException, CoverageException {
        Request.Builder builder = new Request.Builder()
                .url(getParameters.url);

        if (getParameters.cookies != null) {
            for (Map.Entry<String, String> entry : getParameters.cookies.entrySet()) {
                builder = builder.header("cookie", entry.getKey() + "=" + entry.getValue());
            }
        }

        Request request = builder.get().build();
        Response response = client.newCall(request).execute();

        if (response.code() != 200){
            throw new CoverageException("error getting: " + getParameters.url.toString());
        }

        if (responseCookies != null){
            getCookies(response.headers(), responseCookies);
        }

        return response.body().string();

    }


    public String get(HttpUrl url) throws Exception {
        return get(url, null, null);
    }

    public String get(HttpUrl url, String cookie) throws Exception {
        return get(url, cookie, null);
    }

    public String get(HttpUrl url, String cookie, HashMap<String, ArrayList<String>> responseCookies) throws Exception {
        Request.Builder builder = new Request.Builder()
                .url(url);

        if (cookie != null) {
            builder = builder.header("cookie", cookie);
        }
        Request request = builder.get().build();
        Response response = client.newCall(request).execute();

        if (response.code() != 200){
            throw new Exception("error getting session");
        }

        if (responseCookies != null){
            getCookies(response.headers(), responseCookies);
        }

        return response.body().string();
    }

    private HashMap<String, ArrayList<String>> getCookies(Headers headers, HashMap<String, ArrayList<String>> map){
        for (String name : headers.names()) {
            if (name.compareToIgnoreCase("set-cookie") == 0) {
                String headerValue = headers.get(name);
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
        return map;
    }
}

