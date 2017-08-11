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

import com.google.gson.annotations.Expose;

import org.dchbx.coveragehq.statemachine.StateManager;
import org.dchbx.coveragehq.uqhp.UQHPService;

import java.io.Serializable;

public class ServiceManager {

    private static ServiceManager staticServiceManager = new ServiceManager();


    // Services
    private SignUpService signUpService;
    private RidpService ridpService;
    private UQHPService uqhpService;
    private BrokerWorker brokerWorker;
    private StateManager stateManager;
    private ConfigurationStorageHandler configurationStorageHandler;
    private AppStatusService appStatusService;
    private DebugStateService debugStateService;

    public AppConfig getAppConfig(){
        AppConfig appConfig = new AppConfig();
        appConfig.DataSource = config.DataSource();
        appConfig.GithubUrl = getServerConfiguration().GitAccountsUrl;
        appConfig.MobileServerUrl = ivlBuildConfig2.getUrl();
        return appConfig;
    }

    public void update(AppConfig updateAppConfig) {
        ConfigurationStorageHandler configurationStorageHandler = getConfigurationStorageHandler();
        configurationStorageHandler.store(updateAppConfig);
        configAppConfig(updateAppConfig);
    }

    public void configAppConfig(AppConfig updateAppConfig) {
        switch (updateAppConfig.DataSource){
            case GitHub:
                config = gitHubBuildConfig2;
                gitHubBuildConfig2.getCoverageConnection();
                ServerConfiguration serverConfiguration = getServerConfiguration();
                serverConfiguration.GitAccountsUrl = updateAppConfig.GithubUrl;
                serverConfiguration.endpointsPath = updateAppConfig.GithubUrl;
                break;
            case MobileServer:
                config = ivlBuildConfig2;
                ivlBuildConfig2.getCoverageConnection();
                ivlBuildConfig2.setUrl(updateAppConfig.MobileServerUrl);
                break;
            case EnrollServer:
                break;
        }
    }

    public StateManager getStateManager() {
        return stateManager;
    }

    public void init() {
        // Initialize all of the services

        signUpService = new SignUpService();
        ridpService = new RidpService(this);
        uqhpService = new UQHPService(this);
        brokerWorker = new BrokerWorker(this);
        stateManager = new StateManager(this);
        configurationStorageHandler = new ConfigurationStorageHandler();
        appStatusService = new AppStatusService(this);
        debugStateService = new DebugStateService();

        debugStateService.init();
        stateManager.init();

        AppConfig appConfig = getAppConfig();
        if (configurationStorageHandler.read(appConfig)) {
            configAppConfig(appConfig);
        }
    }

    public BrokerWorker getBrokerWorker() {
        return brokerWorker;
    }

    public RidpService getRidpService() {
        return ridpService;
    }

    public SignUpService getSignUpService() {
        return signUpService;
    }

    public JsonParser getParser() {
        return new JsonParser();
    }

    public AppStatusService getAppStatusService() {
        return appStatusService;
    }

    public enum DataSource {
        GitHub,
        EnrollServer,
        MobileServer
    }

    public static class AppConfig implements Serializable {
        @Expose
        public DataSource DataSource;

        @Expose
        public String GithubUrl;

        @Expose
        public String EnrollServerUrl;

        @Expose
        public String MobileServerUrl;
    }

    private static IvlBuildConfig2 ivlBuildConfig2 = new IvlBuildConfig2();
    private static GitHubBuildConfig2 gitHubBuildConfig2 = new GitHubBuildConfig2();
    private static EnrollConfigBase config = BuildVariant.initialEnrollConfig();

    public ServerConfiguration getServerConfiguration(){
        return config.getServerConfiguration();
    }

    public EnrollConfigBase enrollConfig(){
        return config;
    }

    public UrlHandler getUrlHandler(){
        return enrollConfig().getUrlHandler();
    }

    public ConnectionHandler getConnectionHandler() {
        return new EnrollConnectionHandler(getServerConfiguration());
    }

    public CoverageConnection getCoverageConnection() {
        return enrollConfig().getCoverageConnection();
    }

    public static ServiceManager getServiceManager(){
        return staticServiceManager;
    }

    public ConfigurationStorageHandler getConfigurationStorageHandler(){
        return configurationStorageHandler;
    }
}
