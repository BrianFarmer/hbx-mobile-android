package gov.dc.broker;

import okhttp3.OkHttpClient;

/**
 * Created by plast on 12/29/2016.
 */

public class EnrollConnectionHandler extends ConnectionHandler {

    protected OkHttpClient clientHttps = new OkHttpClient()
            .newBuilder()
            .sslSocketFactory(getSSLContext().getSocketFactory())
            .followRedirects(true)
            .build();

    protected OkHttpClient clientDontFollowHttps = new OkHttpClient()
            .newBuilder()
            .sslSocketFactory(getSSLContext().getSocketFactory())
            .followRedirects(false)
            .followSslRedirects(false)
            .build();

    public EnrollConnectionHandler(ServerConfiguration serverConfiguration) {
        super(serverConfiguration);
    }

    @Override
    protected OkHttpClient getClient(String scheme, boolean follow) {
        if (follow){
            return clientHttps;
        } else {
            return clientDontFollowHttps;
        }
    }
}
