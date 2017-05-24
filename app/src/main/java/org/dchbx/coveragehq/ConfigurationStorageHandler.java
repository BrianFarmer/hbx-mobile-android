package org.dchbx.coveragehq;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigurationStorageHandler extends IServerConfigurationStorageHandler {
    private static String TAG = "ConfigurationStorage";

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
        editor.putBoolean(brokerApplication.getString(R.string.have_front_insurance_card), serverConfiguration.haveFrontInsuranceCard);
        editor.putBoolean(brokerApplication.getString(R.string.have_rear_insurance_card), serverConfiguration.haveRearInsuranceCard);
        editor.putFloat("premiumFilter", (float) serverConfiguration.premiumFilter);
        editor.putFloat("deductibleFilter", (float) serverConfiguration.deductibleFilter);
        editor.putString("planShoppingParameters", JsonSerializer.serialize(serverConfiguration.planShoppingParameters));

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
        serverConfiguration.haveFrontInsuranceCard = sharedPref.getBoolean(brokerApplication.getString(R.string.have_front_insurance_card), false);
        serverConfiguration.haveRearInsuranceCard = sharedPref.getBoolean(brokerApplication.getString(R.string.have_rear_insurance_card), false);
        serverConfiguration.premiumFilter = sharedPref.getFloat("premiumFilter", -1);
        serverConfiguration.deductibleFilter = sharedPref.getFloat("deductibleFilter", -1);

        String planShoppingParametersString = sharedPref.getString("planShoppingParameters", null);
        if (planShoppingParametersString == null){
            serverConfiguration.planShoppingParameters = new PlanShoppingParameters();
        } else {
            serverConfiguration.planShoppingParameters = JsonParser.parsePlanShoppingParameters(planShoppingParametersString);
        }

    }

    @Override
    public void clear() {
        BrokerApplication brokerApplication = BrokerApplication.getBrokerApplication();
        SharedPreferences sharedPref = brokerApplication.getSharedPreferences(brokerApplication.getString(R.string.sharedpreferencename), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
    }
}
