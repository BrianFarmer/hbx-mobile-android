package org.dchbx.coveragehq;
/*
    This file is part of DC Health Link.

    DC Health Link SmallBiz is free software:you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation,either version 3of the License,or
    (at your option)any later version.

    DC Health Link SmallBiz is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY;without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DC Health Link SmallBiz.If not,see<http://www.gnu.org/licenses/>.
    This statement should go near the beginning of every source file,close to the copyright notices.When using the Lesser GPL,insert the word “Lesser” before “General” in all three places.When using the GNU AGPL,insert the word “Affero” before “General” in all three places.
*/

public class BrokerWorkerConfig {

    private static BrokerWorkerConfig staticBrokerWorkerConfig = new BrokerWorkerConfig();

    public void update(AppConfig updateAppConfig) {
        switch (updateAppConfig.DataSource){
            case GitHub:
                config = gitHubBuildConfig2;
                gitHubBuildConfig2.getCoverageConnection();
                break;
            case MobileServer:
                break;
            case EnrollServer:
                break;
        }
    }

    public enum DataSource {
        GitHub,
        EnrollServer,
        MobileServer
    }

    public static class AppConfig{
        public DataSource DataSource;
        public String GithubUrl;
        public String EnrollServerUrl;
        public String MobileServerUrl;
    }

    private static IvlBuildConfig2 ivlBuildConfig2 = new IvlBuildConfig2();
    private static GitHubBuildConfig2 gitHubBuildConfig2 = new GitHubBuildConfig2();
    private static EnrollConfigBase config = BuildVariant.initialEnrollConfig();

    public AppConfig getAppConfig(){
        AppConfig appConfig = new AppConfig();
        appConfig.DataSource = config.DataSource();
        appConfig.GithubUrl = gitHubBuildConfig2.getUrl();
        appConfig.MobileServerUrl = ivlBuildConfig2.getUrl();
        return appConfig;
    }

    public ServerConfiguration getServerConfiguration(){
        return config.getServerConfiguration();
    }

    public EnrollConfigBase enrollConfig(){
        return config;
    }
    public CoverageConnection getCoverageConnection() {
        return enrollConfig().getCoverageConnection();
    }

    public static BrokerWorkerConfig config(){
        return staticBrokerWorkerConfig;
    }
}
