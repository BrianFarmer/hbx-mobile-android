package gov.dc.broker;

import com.microsoft.azure.mobile.MobileCenter;
import com.microsoft.azure.mobile.analytics.Analytics;
import com.microsoft.azure.mobile.crashes.Crashes;

import org.joda.time.Duration;

import static gov.dc.broker.ServerConfiguration.HostInfo;

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
        return new DevConfigurationStorageHandler();
    }

    public static ServerConfiguration getServerConfiguration() {
        if (serverConfiguration != null){
            return serverConfiguration;
        }
        serverConfiguration = new ServerConfiguration();
        serverConfiguration.dataInfo = new HostInfo();
        serverConfiguration.dataInfo.host = "raw.githubusercontent.com";
        serverConfiguration.dataInfo.scheme = "https";
        serverConfiguration.dataInfo.port = 443;
        serverConfiguration.employerListPath = "dchealthlink/HBX-mobile-app-APIs/v0.2.1/enroll/broker/employers_list/response/example.json";
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



    public UrlHandler getUrlHandler() {
        return new GitUrlHandler(getServerConfiguration());
    }

    public void logout() {

    }

    public IDataCache getDataCache() {
        return dataCache;
    }

    public CoverageConnection getCoverageConnection() {
        return new GithubCoverageConnection(getUrlHandler(), getConnectionHandler(), getServerConfiguration(), getParser(), getDataCache());
    }

    private JsonParser getParser() {
        return new JsonParser();
    }

    private ConnectionHandler getConnectionHandler() {
        return new ConnectionHandler(serverConfiguration);
    }

    public static void initMobileCenter() {
        MobileCenter.start(BrokerApplication.getBrokerApplication(), "3f262857-3956-470c-acc1-c23fc38d8118",
                Analytics.class, Crashes.class);
    }
}