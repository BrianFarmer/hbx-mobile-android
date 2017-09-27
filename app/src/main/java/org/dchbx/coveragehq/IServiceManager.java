package org.dchbx.coveragehq;

import org.dchbx.coveragehq.financialeligibility.FinancialEligibilityService;
import org.dchbx.coveragehq.ridp.RidpService;
import org.dchbx.coveragehq.statemachine.StateManager;

public interface IServiceManager {

    ServiceManager.AppConfig getAppConfig();

    StateManager getStateManager();

    BrokerWorker getBrokerWorker();

    RidpService getRidpService();

    JsonParser getParser();

    AppStatusService getAppStatusService();

    FinancialEligibilityService getFinancialEligibilityService();

    ServerConfiguration getServerConfiguration();

    UrlHandler getUrlHandler();

    ConnectionHandler getConnectionHandler();

    CoverageConnection getCoverageConnection();

    ConfigurationStorageHandler getConfigurationStorageHandler();

    EnrollConfigBase getEnrollConfig();
}