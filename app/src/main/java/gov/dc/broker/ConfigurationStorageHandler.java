package gov.dc.broker;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigurationStorageHandler extends IServerConfigurationStorageHandler {

    private final ISettingEncryption settingEncryption;

    public ConfigurationStorageHandler(ISettingEncryption settingEncryption){
        this.settingEncryption = settingEncryption;
    }

    @Override
    public void store(ServerConfiguration serverConfiguration) {
        BrokerApplication brokerApplication = BrokerApplication.getBrokerApplication();
        SharedPreferences sharedPref = brokerApplication.getSharedPreferences(brokerApplication.getString(R.string.sharedpreferencename), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("useFingerprintSensor", serverConfiguration.useFingerprintSensor);
        editor.putString(brokerApplication.getString(R.string.shared_preference_account_name), serverConfiguration.accountName);
        editor.putBoolean(brokerApplication.getString(R.string.shared_preference_remember_me), serverConfiguration.rememberMe);
        //editor.putString("UserType", serverConfiguration.userType.name());
        if (serverConfiguration.useFingerprintSensor){
            editor.putString("password", serverConfiguration.password);
        } else {
            editor.putString("password", null);
        }
        editor.commit();
    }

    @Override
    public void read(ServerConfiguration serverConfiguration) {
        BrokerApplication brokerApplication = BrokerApplication.getBrokerApplication();
        SharedPreferences sharedPref = brokerApplication.getSharedPreferences(brokerApplication.getString(R.string.sharedpreferencename), Context.MODE_PRIVATE);
        serverConfiguration.useFingerprintSensor = sharedPref.getBoolean("useFingerprintSensor", false);
        serverConfiguration.accountName = sharedPref.getString(brokerApplication.getString(R.string.shared_preference_account_name), null);
        serverConfiguration.rememberMe = sharedPref.getBoolean(brokerApplication.getString(R.string.shared_preference_remember_me), true);
        //serverConfiguration.userType = ServerConfiguration.UserType.valueOf(sharedPref.getString("UserType", ServerConfiguration.UserType.Unknown.name()));
        if (serverConfiguration.useFingerprintSensor) {
            serverConfiguration.password = sharedPref.getString("password", null);
        }
    }

    @Override
    public void clear() {
        BrokerApplication brokerApplication = BrokerApplication.getBrokerApplication();
        SharedPreferences sharedPref = brokerApplication.getSharedPreferences(brokerApplication.getString(R.string.sharedpreferencename), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
    }
}
