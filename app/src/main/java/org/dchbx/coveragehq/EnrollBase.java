package org.dchbx.coveragehq;

import com.microsoft.azure.mobile.MobileCenter;
import com.microsoft.azure.mobile.analytics.Analytics;
import com.microsoft.azure.mobile.crashes.Crashes;

import org.joda.time.Duration;

import java.util.ArrayList;

import static org.dchbx.coveragehq.EnrollConfigBase.StorageHandlerType.Clear;

abstract class EnrollConfigBase {
    public ServerConfiguration getServerConfiguration() {
        return null;
    }

    public abstract int getTimeoutCountdownSeconds();

    //
    // This is the number of seconds the have to pass before the user gets
    // a dialog telling them that the session is about to timeout.
    //
    public abstract int getSessionTimeoutSeconds();

    public abstract String getVersion();

    public abstract ServiceManager.DataSource DataSource();

    public enum StorageHandlerType {
        Encrypted,
        Clear
    }

    private static int cacheTimeoutSeconds = 30;
    private static IDataCache dataCache = new DataCache();
    private static StorageHandlerType storageHandlerType = Clear;

    static String getString(){
        return "what is this used for";
    }
    public boolean isBrokerBuild() {
        return true;
    }

    public int getDataSourceIndex (){
        return 0;
    }

    public void setServerConfigurationStorageHandlerType(StorageHandlerType newStorageHandlerType) {
        storageHandlerType = newStorageHandlerType;
    }

    public IServerConfigurationStorageHandler getServerConfigurationStorageHandler() {
        //if (storageHandlerType == Clear) {
            return new ConfigurationStorageHandler();
        //}
        //return new EnrollServerConfigurationStorageHandler();
    }

    public boolean checkSession(ServerConfiguration serverConfiguration) {
        return  serverConfiguration.sessionId != null
                && serverConfiguration.sessionId.length() > 0;
    }


    public Duration getCacheTimeout() {
        return Duration.standardSeconds(cacheTimeoutSeconds);
    }

    public int getLoginLayout() {
        return R.layout.activity_login;
    }

    public boolean isGit() {
        return false;
    }

    public EnrollUrlHandler getUrlHandler() {
        return new EnrollUrlHandler(getServerConfiguration(), new JsonParser());
    }

    public void logout() {

    }

    public IDataCache getDataCache() {
        return dataCache;
    }

    public CoverageConnection getCoverageConnection() {
        return new EnrollCoverageConnection(getUrlHandler(), getConnectionHandler(), getServerConfiguration(),
                getParser(), getDataCache(), new ConfigurationStorageHandler(), ServiceManager.getServiceManager());
    }

    private JsonParser getParser() {
        return new JsonParser();
    }

    private ConnectionHandler getConnectionHandler() {
        return new EnrollConnectionHandler(getServerConfiguration());
    }

    public void initMobileCenter() {
        MobileCenter.start(BrokerApplication.getBrokerApplication(), "3f262857-3956-470c-acc1-c23fc38d8118",
                Analytics.class, Crashes.class);
    }

    public ArrayList<String> getUrls() { return null;  }
}
