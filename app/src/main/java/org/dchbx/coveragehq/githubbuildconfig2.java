package org.dchbx.coveragehq;

import com.microsoft.azure.mobile.MobileCenter;
import com.microsoft.azure.mobile.analytics.Analytics;
import com.microsoft.azure.mobile.crashes.Crashes;

import org.joda.time.Duration;

import java.util.ArrayList;

import static org.dchbx.coveragehq.ServerConfiguration.HostInfo;

class GitHubBuildConfig2 extends EnrollConfigBase{

    private static int cacheTimeoutSeconds = 120;
    private static IDataCache dataCache = new DataCache();

    private static ServerConfiguration serverConfiguration = null;

    @Override
    public boolean isBrokerBuild() {
        return true;
    }

    @Override
    public int getDataSourceIndex (){
        return 0;
    }

    @Override
    public IServerConfigurationStorageHandler getServerConfigurationStorageHandler() {
        return new ConfigurationStorageHandler();
    }

    @Override
    public ServerConfiguration getServerConfiguration() {
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

    @Override
    public boolean checkSession(ServerConfiguration serverConfiguration) {
        return  serverConfiguration.sessionId != null
                && serverConfiguration.sessionId.length() > 0;
    }


    @Override
    public Duration getCacheTimeout() {
        return Duration.standardSeconds(cacheTimeoutSeconds);
    }

    @Override
    public boolean isGit() {
        return true;
    }

    @Override
    public int getLoginLayout() {
        return R.layout.activity_login_git;
    }


    public GitUrlHandler getGitUrlHandler() {
        return new GitUrlHandler(getServerConfiguration(), new JsonParser());
    }

    public void logout() {

    }

    public IDataCache getDataCache() {
        return dataCache;
    }

    public CoverageConnection getCoverageConnection() {
        return new GithubCoverageConnection(getGitUrlHandler(), getConnectionHandler(), getServerConfiguration(), getParser(), getDataCache(), getServerConfigurationStorageHandler());
    }

    private JsonParser getParser() {
        return new JsonParser();
    }

    private ConnectionHandler getConnectionHandler() {
        return new ConnectionHandler(serverConfiguration);
    }

    @Override
    public void initMobileCenter() {
        MobileCenter.start(BrokerApplication.getBrokerApplication(), "3f262857-3956-470c-acc1-c23fc38d8118",
                Analytics.class, Crashes.class);
    }

    public String getUrl() {
        return "https://raw.githubusercontent.com/dchealthlink/HBX-mobile-app-APIs/master/generated";
    }

    @Override
    public ArrayList<String> getUrlLabels() {
        ArrayList urls = new ArrayList();
        urls.add("Choose a repository");
        urls.add("HBX Mobile app api Master");
        urls.add("Brian's Master Fork");
        urls.add("Brian's Fork");
        urls.add("HBX Mobile app api test data repository");
        return urls;
    }

    @Override
    public int getTimeoutCountdownSeconds() {
        return 30;
    }

    @Override
    public int getSessionTimeoutSeconds() {
        return 14*60;
    }

    @Override
    public String getVersion() {
        return "github";
    }

    @Override
    public BrokerWorkerConfig.DataSource DataSource() {
        return BrokerWorkerConfig.DataSource.GitHub;
    }
}