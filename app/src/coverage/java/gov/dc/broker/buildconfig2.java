package gov.dc.broker;

import com.microsoft.azure.mobile.MobileCenter;
import com.microsoft.azure.mobile.analytics.Analytics;
import com.microsoft.azure.mobile.crashes.Crashes;

import org.joda.time.Duration;

import java.util.ArrayList;

class BuildConfig2 {
    private static int cacheTimeoutSeconds = 120;
    private static IDataCache dataCache = new DataCache();

    static String getString(){
        return "Hoo Woo!!!";
    }
    private static ServerConfiguration serverConfiguration = null;
    public static boolean isBrokerBuild() {
        return true;
    }

    public static int getDataSourceIndex (){
        return 0;
    }

    public static IServerConfigurationStorageHandler getServerConfigurationStorageHandler() {
        return new ConfigurationStorageHandler(new IdentityEncryptionImpl());
    }

    public static ServerConfiguration getServerConfiguration() {
        if (serverConfiguration != null){
            return serverConfiguration;
        }
        serverConfiguration = new ServerConfiguration();

        serverConfiguration.dataInfo = new ServerConfiguration.HostInfo();
        serverConfiguration.dataInfo.host = "enroll-mobile.dchbx.org";
        serverConfiguration.dataInfo.scheme = "https";
        serverConfiguration.dataInfo.port = 443;

        serverConfiguration.loginInfo = new ServerConfiguration.HostInfo();
        serverConfiguration.loginInfo.host = "hbx-mobile-preprod.dchbx.org";
        serverConfiguration.loginInfo.scheme = "http";
        serverConfiguration.loginInfo.port = 80;


        //serverConfiguration.employerListPath = "api/v1/mobile_api/employers_list";
        serverConfiguration.loginPath = "login";
        return serverConfiguration;
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
        return new EnrollConnectionHandler(serverConfiguration);
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
        return (14*60)+30;
    }
}
