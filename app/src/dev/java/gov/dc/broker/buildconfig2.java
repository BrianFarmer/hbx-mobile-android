package gov.dc.broker;

import org.joda.time.Duration;

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
        serverConfiguration.dataInfo = new ServerConfiguration.HostInfo();
        //serverConfiguration.dataInfo.host = "ec2-54-234-22-53.compute-1.amazonaws.com";
        serverConfiguration.dataInfo.host = "mobile.dcmic.org";
        serverConfiguration.dataInfo.scheme = "http";
        serverConfiguration.dataInfo.port = 3000;
        serverConfiguration.employerListPath = "api/v1/mobile_api/employers_list";
        serverConfiguration.loginPath = ServerConfiguration.defaultDevLogin;
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
        return new DevUrlHandler(getServerConfiguration());
    }

    public void logout() {

    }

    public IDataCache getDataCache() {
        return dataCache;
    }

    public CoverageConnection getCoverageConnection() {
        return new DevCoverageConnection(getUrlHandler(), getConnectionHandler(), getServerConfiguration(), getParser(), getDataCache());
    }

    private JsonParser getParser() {
        return new JsonParser();
    }

    private ConnectionHandler getConnectionHandler() {
        return new ConnectionHandler(serverConfiguration);
    }
}