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

import org.dchbx.coveragehq.financialeligibility.FinancialEligibilityService;
import org.dchbx.coveragehq.planshopping.PlanShoppingService;
import org.dchbx.coveragehq.ridp.RidpService;
import org.dchbx.coveragehq.startup.StartUpService;
import org.dchbx.coveragehq.statemachine.StateManager;

import java.io.Serializable;


public class ServiceManager implements IServiceManager {

    private static ServiceManager staticServiceManager = new ServiceManager();


    // Services
    private PlanShoppingService planShoppingService;
    private RidpService ridpService;
    private BrokerWorker brokerWorker;
    private StateManager stateManager;
    private ConfigurationStorageHandler configurationStorageHandler;
    private AppStatusService appStatusService;
    private DebugStateService debugStateService;
    private FinancialEligibilityService financialEligibilityService;
    private StartUpService startUpService;

    @Override
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

    @Override
    public StateManager getStateManager() {
        return stateManager;
    }

    public void init() {
        // Initialize all of the services

        ridpService = new RidpService(this);
        brokerWorker = new BrokerWorker(this);
        stateManager = new StateManager(this);
        configurationStorageHandler = new ConfigurationStorageHandler();
        appStatusService = new AppStatusService(this);
        debugStateService = new DebugStateService();
        financialEligibilityService = new FinancialEligibilityService(this);
        startUpService = new StartUpService(this);
        planShoppingService = new PlanShoppingService(this);

        debugStateService.init();
        stateManager.init();

        AppConfig appConfig = getAppConfig();
        if (configurationStorageHandler.read(appConfig)) {
            configAppConfig(appConfig);
        }
    }

    @Override
    public BrokerWorker getBrokerWorker() {
        return brokerWorker;
    }

    @Override
    public RidpService getRidpService() {
        return ridpService;
    }

    @Override
    public PlanShoppingService getPlanShoppingService() {
        return planShoppingService;
    }

    @Override
    public JsonParser getParser() {
        return new JsonParser();
    }

    @Override
    public AppStatusService getAppStatusService() {
        return appStatusService;
    }

    @Override
    public FinancialEligibilityService getFinancialEligibilityService() {
        return financialEligibilityService;
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

    @Override
    public ServerConfiguration getServerConfiguration(){
        return config.getServerConfiguration();
    }

    public EnrollConfigBase getEnrollConfig(){
        return config;
    }

    @Override
    public UrlHandler getUrlHandler(){
        return getEnrollConfig().getUrlHandler();
    }

    @Override
    public ConnectionHandler getConnectionHandler() {
        return new EnrollConnectionHandler(getServerConfiguration());
    }

    @Override
    public CoverageConnection getCoverageConnection() {
        return getEnrollConfig().getCoverageConnection();
    }

    public static ServiceManager getServiceManager(){
        return staticServiceManager;
    }

    @Override
    public ConfigurationStorageHandler getConfigurationStorageHandler(){
        return configurationStorageHandler;
    }
}
