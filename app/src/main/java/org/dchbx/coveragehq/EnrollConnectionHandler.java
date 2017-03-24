package org.dchbx.coveragehq;

import android.content.Context;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by plast on 12/29/2016.
 */

public class EnrollConnectionHandler extends ConnectionHandler {

    private Context context = null;
    SharedPrefsCookiePersistor cookiePersistor = new SharedPrefsCookiePersistor(BrokerApplication.getBrokerApplication());
    ClearableCookieJar cookieJar =
            new PersistentCookieJar(new SetCookieCache(), cookiePersistor);

    static protected OkHttpClient clientHttps = new OkHttpClient()
            .newBuilder()
            //.cookieJar(cookieJar)
            .sslSocketFactory(getSSLContext().getSocketFactory())
            .readTimeout(30000, TimeUnit.MILLISECONDS)
            .followRedirects(true)
            .build();

    protected OkHttpClient clientDontFollowHttps = new OkHttpClient()
            .newBuilder()
            //.cookieJar(cookieJar)
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
            return clientHttps;
        }
    }
}
