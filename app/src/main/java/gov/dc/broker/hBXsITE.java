package gov.dc.broker;

import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public abstract class HbxSite extends Site {

    public ServerSiteConfig getEnrollServerSiteConfig() {
        return enrollServerSiteConfig;
    }

    public void setEnrollServerSiteConfig(ServerSiteConfig enrollServerSiteConfig) {
        this.enrollServerSiteConfig = enrollServerSiteConfig;
    }

    static public class ServerSiteConfig {
        public final String scheme;
        public final String host;
        public final int port;
        public final String root;

        public ServerSiteConfig(String scheme, String host){
            this.root = null;
            this.scheme = scheme;
            this.host = host;
            if (scheme.compareToIgnoreCase("http") == 0) {
                this.port = 80;
            } else {
                if (scheme.compareToIgnoreCase("https") == 0) {
                    this.port = 443;
                }
                else {
                    this.port = -1;
                }
            }
        }

        public ServerSiteConfig(String scheme, String host, int port){

            this.scheme = scheme;
            this.host = host;
            this.port = port;
            this.root = null;
        }

        public ServerSiteConfig(String scheme, String host, int port, String root){
            this.scheme = scheme;
            this.host = host;
            this.port = port;
            this.root = root;
        }


        public boolean isSchemeHttps(){
            return scheme.compareToIgnoreCase("https") == 0;
        }

        public String builtUrlRoot(){
            String url = scheme + "://" + host + ":" + Integer.toString(port) + "/";
            if (root == null){
                return url;
            }
            return url + root + "/";
        }
    }

    private static String TAG = "HbxSite";
    private static String employersList = "/api/v1/mobile_api/employers_list";
    private static String employer = "/api/v1/mobile_api/employer_details";
    protected final ServerSiteConfig siteConfig;
    private ServerSiteConfig enrollServerSiteConfig;
    static protected OkHttpClient client = null;
    static OkHttpClient clientDontFollow = null;

    public static void initClients(){
        try
        {
            client = new OkHttpClient()
                    .newBuilder()
                    .sslSocketFactory(getSSLContext().getSocketFactory())
                    .build();
            clientDontFollow = new OkHttpClient()
                .newBuilder()
                .sslSocketFactory(getSSLContext().getSocketFactory())
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
        } catch (Throwable t){
            Log.e(TAG, "Exception constructing OkHttpClients", t);
        }
    }

    HbxSite(ServerSiteConfig siteConfig){
        this.siteConfig = siteConfig;
        enrollServerSiteConfig = null;
    }

    HbxSite(ServerSiteConfig siteConfig, ServerSiteConfig enrollServerSiteConfig){
        this.siteConfig = siteConfig;
        this.enrollServerSiteConfig = enrollServerSiteConfig;
    }

    @Override
    public void initEnrollServerInfo(String enrollServerBaseUrl) throws URISyntaxException {
        if (enrollServerBaseUrl == null){
            enrollServerSiteConfig = null;
            return;
        }
        URI uri = new URI(enrollServerBaseUrl);
        int port = uri.getPort();
        if (port == -1){
            if (uri.getScheme().compareToIgnoreCase("http") == 0){
                port = 80;
            } else {
                port = 443;
            }
        }
        enrollServerSiteConfig = BuildConfig2.getServerSiteConfig();
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

    protected boolean isUrlHttps(String url){
        if (url.length() <= 5){
            return false;
        }
        return url.substring(0, 5).compareToIgnoreCase("https") == 0;
    }

    protected String getFullyQualifiedUrl(String urlString, AccountInfo accountInfo) throws Exception {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (isUrlHttps(urlString)){
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) connection;
                httpsURLConnection.setSSLSocketFactory(getSSLContext().getSocketFactory());
            }
            connection.setRequestProperty("Cookie", "_session_id=" + accountInfo.sessionId);
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder result = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                result.append(line);
            }
            String responseBody = result.toString();
            Log.d(TAG, "login repsonse: " + responseBody);
            return responseBody;
        } catch (Throwable t){
            Log.e(TAG, "throwable", t);
            throw t;
        }
    }

    protected String getUrlOkHttpClient(String urlString, AccountInfo accountInfo) throws Exception {
        try {
            HttpUrl url = new HttpUrl.Builder()
                    .scheme(siteConfig.scheme)
                    .host(siteConfig.host)
                    .addPathSegments(urlString)
                    .port(siteConfig.port)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .header("Cookie", "_session_id=" + accountInfo.sessionId)
                    .get()
                    .build();

            Response response = client.newCall(request)
                    .execute();

            int code = response.code();
            if (code < 200
                    || code > 299) {
                throw new Exception("error getting session");
            }
            String responseBody = response.body().string();
            Log.d(TAG, "login repsonse: " + responseBody);
            return responseBody;
        } catch (Throwable t){
            Log.e(TAG, "throwable", t);
            throw t;
        }
    }

    protected String getFullyQualifiedUrl2(String urlString, AccountInfo accountInfo) throws Exception {
        Log.d(TAG, "Getting url: " + urlString);
        Log.d(TAG, "Session id: " + accountInfo.sessionId);
        try {
            Request request = new Request.Builder()
                    .url(urlString)
                    .header("Cookie", "_session_id=" + accountInfo.sessionId)
                    .get()
                    .build();

            Response response = client.newCall(request)
                    .execute();

            int code = response.code();
            if (code < 200
                    || code > 299) {
                throw new Exception("error getting session");
            }
            String responseBody = response.body().string();
            Log.d(TAG, "login repsonse: " + responseBody);
            return responseBody;
        } catch (Throwable t){
            Log.e(TAG, "throwable", t);
            throw t;
        }
    }

    protected String getRelativeUrl(String relativeUrl, AccountInfo accountInfo) throws Exception {
        return getRelativeUrl(siteConfig, relativeUrl, accountInfo);
    }

    protected String getRelativeUrl(ServerSiteConfig serverSiteConfig, String relativeUrl, AccountInfo accountInfo) throws Exception {
        String fullyQualifiedUrl;
        if (relativeUrl.charAt(0) == '/') {
            fullyQualifiedUrl = String.format(BrokerApplication.getBrokerApplication().getString(R.string.format_url_path_with_leading_slash), serverSiteConfig.scheme, serverSiteConfig.host, serverSiteConfig.port, relativeUrl);
        } else {
            fullyQualifiedUrl = String.format(BrokerApplication.getBrokerApplication().getString(R.string.format_url_path_without_leading_slash), serverSiteConfig.scheme, serverSiteConfig.host, serverSiteConfig.port, relativeUrl);
        }
        return getFullyQualifiedUrl(fullyQualifiedUrl, accountInfo);
    }

    @Override
    public String GetBrokerAgency(AccountInfo accountInfo) throws Exception {
        String result = getRelativeUrl(enrollServerSiteConfig, employersList, accountInfo);
        return result;
    }

    @Override
    public String GetEmployer(AccountInfo accountInfo) throws Exception {
        return GetEmployer(employer, accountInfo);
    }

    @Override
    public String GetEmployer(String url, AccountInfo accountInfo) throws Exception {
        return getRelativeUrl(enrollServerSiteConfig, url, accountInfo);
    }

    @Override
    public String getRoster(Events.GetRoster getRoster, String url, AccountInfo accountInfo) throws Exception {
        return getRelativeUrl(enrollServerSiteConfig, url, accountInfo);
    }

    @Override
    public String GetCarriers(Events.GetCarriers getCarriers) throws IOException{
        return getUrl("https://dchealthlink.com/shared/json/carriers.json");
    }
}
