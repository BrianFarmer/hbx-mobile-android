package org.dchbx.coveragehq;

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
        //serverConfiguration.dataInfo.host = "ec2-54-234-22-53.compute-1.amazonaws.com";
        serverConfiguration.dataInfo.host = "mobile.dcmic.org";
        serverConfiguration.dataInfo.scheme = "http";
        serverConfiguration.dataInfo.port = 3000;
        serverConfiguration.brokerDetailPath = "api/v1/mobile_api/employers_list";
        serverConfiguration.employerDetailPath = "api/v1/mobile_api/employer_details";

        serverConfiguration.loginPath = "/users/sign_in";
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

    public static boolean isGit() {
        return false;
    }

    public static int getLoginLayout() {
        return R.layout.activity_login;
    }

    public DevUrlHandler getUrlHandler() {
        return new DevUrlHandler(getServerConfiguration(), new JsonParser());
    }

    public void logout() {

    }

    public IDataCache getDataCache() {
        return dataCache;
    }

    public CoverageConnection getCoverageConnection() {
        return new DevCoverageConnection(getUrlHandler(), getConnectionHandler(), getServerConfiguration(), getParser(), getDataCache(), getServerConfigurationStorageHandler());
    }

    private JsonParser getParser() {
        return new JsonParser();
    }

    private ConnectionHandler getConnectionHandler() {
        return new ConnectionHandler(serverConfiguration);
    }

    public static void initMobileCenter() {

    }

    public static ArrayList<String> getUrls() { return null;  }

    public static int getSessionTimeoutSeconds() {
        return 30;
    }

    public static int getTimeoutCountdownSeconds() {
        return 15;
    }

    public static ArrayList<String> getUrlLabels() {
        return null;
    }
}