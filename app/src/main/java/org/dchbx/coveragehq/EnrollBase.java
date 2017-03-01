package org.dchbx.coveragehq;

import com.microsoft.azure.mobile.MobileCenter;
import com.microsoft.azure.mobile.analytics.Analytics;
import com.microsoft.azure.mobile.crashes.Crashes;

import org.joda.time.Duration;

import java.util.ArrayList;

import static org.dchbx.coveragehq.BuildConfig2.getServerConfiguration;
import static org.dchbx.coveragehq.EnrollConfigBase.StorageHandlerType.Clear;

class EnrollConfigBase {
    public enum StorageHandlerType {
        Encrypted,
        Clear
    }

    private static int cacheTimeoutSeconds = 30;
    private static IDataCache dataCache = new DataCache();
    private static StorageHandlerType storageHandlerType = Clear;

    static String getString(){
        return "Hoo Woo!!!";
    }
    public static boolean isBrokerBuild() {
        return true;
    }

    public static int getDataSourceIndex (){
        return 0;
    }

    public static void setServerConfigurationStorageHandlerType(StorageHandlerType newStorageHandlerType) {
        storageHandlerType =newStorageHandlerType;
    }

    public static IServerConfigurationStorageHandler getServerConfigurationStorageHandler() {
        //if (storageHandlerType == Clear) {
            return new ConfigurationStorageHandler();
        //}
        //return new EnrollServerConfigurationStorageHandler();
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

    public static int getLoginLayout() {
        return R.layout.activity_login;
    }

    public static boolean isGit() {
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
        return new EnrollCoverageConnection(getUrlHandler(), getConnectionHandler(), getServerConfiguration(), getParser(), getDataCache(), new ConfigurationStorageHandler());
    }

    private JsonParser getParser() {
        return new JsonParser();
    }

    private ConnectionHandler getConnectionHandler() {
        return new EnrollConnectionHandler(getServerConfiguration());
    }

    public static void initMobileCenter() {
        MobileCenter.start(BrokerApplication.getBrokerApplication(), "3f262857-3956-470c-acc1-c23fc38d8118",
                Analytics.class, Crashes.class);
    }

    public static ArrayList<String> getUrls() { return null;  }

    public static ArrayList<String> getUrlLabels() {
        return null;
    }

}
