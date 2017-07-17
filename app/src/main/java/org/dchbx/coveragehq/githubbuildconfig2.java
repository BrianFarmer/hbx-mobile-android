package org.dchbx.coveragehq;

import com.microsoft.azure.mobile.MobileCenter;
import com.microsoft.azure.mobile.analytics.Analytics;
import com.microsoft.azure.mobile.crashes.Crashes;

import org.joda.time.Duration;

import static org.dchbx.coveragehq.ServerConfiguration.HostInfo;

/*
    This file is part of DC.

    DC Health Link SmallBiz is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DC Health Link SmallBiz is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DC Health Link SmallBiz.  If not, see <http://www.gnu.org/licenses/>.
    This statement should go near the beginning of every source file, close to the copyright notices. When using the Lesser GPL, insert the word “Lesser” before “General” in all three places. When using the GNU AGPL, insert the word “Affero” before “General” in all three places.
*/

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
        return "https://raw.githubusercontent.com/dchealthlink/HBX-mobile-app-APIs/master/static/accounts.json";
        //return "https://raw.githubusercontent.com/dchealthlink/HBX-mobile-app-APIs/master/generated/accounts.json";
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
    public ServiceManager.DataSource DataSource() {
        return ServiceManager.DataSource.GitHub;
    }
}