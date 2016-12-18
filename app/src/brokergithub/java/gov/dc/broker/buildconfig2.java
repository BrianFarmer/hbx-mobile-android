package gov.dc.broker;

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
    public static HbxSite.ServerSiteConfig getServerSiteConfig() {
        return new HbxSite.ServerSiteConfig(null, null);
    }

    public static int getDataSourceIndex (){
        return 0;
    }

    public static ServerConfigurationStorageHandler getServerConfigurationStorageHandler() {
        return new GitServerConfigurationStorageHandler();
    }

    public static ConnectionHandler getConnectionHandler(){
        return new GitConnectionHandler();
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

    public static IDataCache getDataCache() {
        return dataCache;
    }

    public static void logout() {
        serverConfiguration = null;
    }
}