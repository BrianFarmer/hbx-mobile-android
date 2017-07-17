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
    static SharedPrefsCookiePersistor cookiePersistor3 = new SharedPrefsCookiePersistor(BrokerApplication.getBrokerApplication());
    static SharedPrefsCookiePersistor cookiePersistor4 = new SharedPrefsCookiePersistor(BrokerApplication.getBrokerApplication());
    static ClearableCookieJar cookieJar3 = new PersistentCookieJar(new SetCookieCache(), cookiePersistor3);
    static ClearableCookieJar cookieJar4 = new PersistentCookieJar(new SetCookieCache(), cookiePersistor3);

    static protected OkHttpClient clientHttps = new OkHttpClient()
            .newBuilder()
            .cookieJar(cookieJar3)
            //.sslSocketFactory(getSSLContext().getSocketFactory())
            .readTimeout(30000, TimeUnit.MILLISECONDS)
            .followRedirects(true)
            .build();

    static protected OkHttpClient clientDontFollowHttps = new OkHttpClient()
            .newBuilder()
            .cookieJar(cookieJar4)
            //.sslSocketFactory(getSSLContext().getSocketFactory())
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
