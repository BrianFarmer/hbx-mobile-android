package gov.dc.broker;

import com.microsoft.azure.mobile.MobileCenter;
import com.microsoft.azure.mobile.analytics.Analytics;
import com.microsoft.azure.mobile.crashes.Crashes;

import org.joda.time.Duration;

import java.util.ArrayList;

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
        return new ConfigurationStorageHandler();
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
        //serverConfiguration.employerListPath = "dchealthlink/HBX-mobile-app-APIs/v0.2.1/enroll/broker/employers_list/response/example.json";
        serverConfiguration.brokerDetailPath = "dchealthlink/HBX-mobile-app-APIs/master/enroll/broker/employers_list/response/example.json";
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
        return true;
    }

    public static int getLoginLayout() {
        return R.layout.activity_login_git;
    }


    public GitUrlHandler getUrlHandler() {
        return new GitUrlHandler(getServerConfiguration(), new JsonParser());
    }

    public void logout() {

    }

    public IDataCache getDataCache() {
        return dataCache;
    }

    public CoverageConnection getCoverageConnection() {
        return new GithubCoverageConnection(getUrlHandler(), getConnectionHandler(), getServerConfiguration(), getParser(), getDataCache(), getServerConfigurationStorageHandler(), new EnrollServerConfigurationStorageHandler ());
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

    public static ArrayList<String> getUrls() {
        ArrayList urls = new ArrayList();

        urls.add("Choose a repository");
        urls.add("https://raw.githubusercontent.com/dchealthlink/HBX-mobile-app-APIs/master/generated");
        urls.add("https://raw.githubusercontent.com/BrianFarmer/HBX-mobile-app-APIs/master/from_templates/generated");
        urls.add("https://raw.githubusercontent.com/BrianFarmer/HBX-mobile-app-APIs/TemplatedTests/accounts");
        urls.add("https://raw.githubusercontent.com/dchealthlink/HBX-mobile-app-APIs/TemplatedTests/accounts");
        return urls;
    }

    public static ArrayList<String> getUrlLabels() {
        ArrayList urls = new ArrayList();
        urls.add("Choose a repository");
        urls.add("HBX Mobile app api");
        urls.add("Brian's Master Fork");
        urls.add("Brian's Fork");
        urls.add("HBX Mobile app api test data repository");
        return urls;
    }

    public static int getTimeoutCountdownSeconds() {
        return 30;
    }

    public static int getSessionTimeoutSeconds() {
        return 14*60;
    }
}