package org.dchbx.coveragehq;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigurationStorageHandler extends IServerConfigurationStorageHandler {

    @Override
    public void store(ServerConfiguration serverConfiguration) {
        BrokerApplication brokerApplication = BrokerApplication.getBrokerApplication();
        SharedPreferences sharedPref = brokerApplication.getSharedPreferences(brokerApplication.getString(R.string.sharedpreferencename), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("useFingerprintSensor", serverConfiguration.useFingerprintSensor);
        if (serverConfiguration.useFingerprintSensor) {
            editor.putString(brokerApplication.getString(R.string.shared_preference_encrypted_info), serverConfiguration.encryptedString);
        } else {
            editor.putString(brokerApplication.getString(R.string.shared_preference_account_name), serverConfiguration.accountName);
        }
        editor.putBoolean(brokerApplication.getString(R.string.shared_preference_remember_me), serverConfiguration.rememberMe);
        //editor.putString("UserType", serverConfiguration.userType.name());
        editor.commit();
    }

    @Override
    public void read(ServerConfiguration serverConfiguration) {
        BrokerApplication brokerApplication = BrokerApplication.getBrokerApplication();
        SharedPreferences sharedPref = brokerApplication.getSharedPreferences(brokerApplication.getString(R.string.sharedpreferencename), Context.MODE_PRIVATE);
        serverConfiguration.useFingerprintSensor = sharedPref.getBoolean("useFingerprintSensor", false);
        if (serverConfiguration.useFingerprintSensor){
            serverConfiguration.encryptedString = sharedPref.getString(brokerApplication.getString(R.string.shared_preference_encrypted_info), null);
        } else {
            serverConfiguration.accountName = sharedPref.getString(brokerApplication.getString(R.string.shared_preference_account_name), null);
        }
        serverConfiguration.rememberMe = sharedPref.getBoolean(brokerApplication.getString(R.string.shared_preference_remember_me), true);
        //serverConfiguration.userType = ServerConfiguration.UserType.valueOf(sharedPref.getString("UserType", ServerConfiguration.UserType.Unknown.name()));
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
