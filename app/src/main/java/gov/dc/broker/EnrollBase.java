package gov.dc.broker;

import com.microsoft.azure.mobile.MobileCenter;
import com.microsoft.azure.mobile.analytics.Analytics;
import com.microsoft.azure.mobile.crashes.Crashes;

import org.joda.time.Duration;

import java.util.ArrayList;

import static gov.dc.broker.BuildConfig2.getServerConfiguration;

class EnrollConfigBase {
    private static int cacheTimeoutSeconds = 30;
    private static IDataCache dataCache = new DataCache();

    static String getString(){
        return "Hoo Woo!!!";
    }
    public static boolean isBrokerBuild() {
        return true;
    }

    public static int getDataSourceIndex (){
        return 0;
    }

    public static IServerConfigurationStorageHandler getServerConfigurationStorageHandler() {
        return new ConfigurationStorageHandler(new IdentityEncryptionImpl());
    }

    public static boolean checkSession(ServerConfiguration serverConfiguration) {
        return  serverConfiguration.sessionId != null
                && serverConfiguration.sessionId.length() > 0;
    }


    public static Duration getCacheTimeout() {
        return Duration.standardSeconds(cacheTimeoutSeconds);
    }

    public static BuildConfig2 getConfig() {
        return new BuildConfig2();
    }

    public static int getLoginLayout() {
        return R.layout.activity_login;
    }

    public static boolean isGit() {
        return false;
    }

    public EnrollUrlHandler getUrlHandler() {
        return new EnrollUrlHandler(getServerConfiguration(), new JsonParser());
    }

    public void logout() {

    }

    public IDataCache getDataCache() {
        return dataCache;
    }

    public CoverageConnection getCoverageConnection() {
        return new EnrollCoverageConnection(getUrlHandler(), getConnectionHandler(), getServerConfiguration(), getParser(), getDataCache(), new ConfigurationStorageHandler(new IdentityEncryptionImpl()));
    }

    private JsonParser getParser() {
        return new JsonParser();
    }

    private ConnectionHandler getConnectionHandler() {
        return new EnrollConnectionHandler(getServerConfiguration());
    }

    public static void initMobileCenter() {
        MobileCenter.start(BrokerApplication.getBrokerApplication(), "3f262857-3956-470c-acc1-c23fc38d8118",
                Analytics.class, Crashes.class);
    }

    public static ArrayList<String> getUrls() { return null;  }

    public static ArrayList<String> getUrlLabels() {
        return null;
    }

    public static int getTimeoutCountdownSeconds() {
        return 30;
    }

    //
    // This is the number of seconds the have to pass before the user gets
    // a dialog telling them that the session is about to timeout.
    //
    public static int getSessionTimeoutSeconds() {
        return 120;
    }
}
