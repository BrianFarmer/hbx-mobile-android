package gov.dc.broker;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigurationStorageHandler extends IServerConfigurationStorageHandler {

    @Override
    public void store(ServerConfiguration serverConfiguration) {
        BrokerApplication brokerApplication = BrokerApplication.getBrokerApplication();
        SharedPreferences sharedPref = brokerApplication.getSharedPreferences(brokerApplication.getString(R.string.sharedpreferencename), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(brokerApplication.getString(R.string.shared_preference_account_name), serverConfiguration.accountName);
        editor.putBoolean(brokerApplication.getString(R.string.shared_preference_remember_me), serverConfiguration.rememberMe);
        editor.commit();
    }

    @Override
    public void read(ServerConfiguration serverConfiguration) {
        BrokerApplication brokerApplication = BrokerApplication.getBrokerApplication();
        SharedPreferences sharedPref = brokerApplication.getSharedPreferences(brokerApplication.getString(R.string.sharedpreferencename), Context.MODE_PRIVATE);
        serverConfiguration.accountName = sharedPref.getString(brokerApplication.getString(R.string.shared_preference_account_name), null);
        serverConfiguration.rememberMe = sharedPref.getBoolean(brokerApplication.getString(R.string.shared_preference_remember_me), true);
    }
}
