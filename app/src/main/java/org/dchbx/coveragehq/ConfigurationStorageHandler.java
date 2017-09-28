package org.dchbx.coveragehq;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.dchbx.coveragehq.models.account.Account;
import org.dchbx.coveragehq.models.fe.Family;
import org.dchbx.coveragehq.models.fe.UqhpDetermination;
import org.dchbx.coveragehq.models.ridp.Answers;
import org.dchbx.coveragehq.models.ridp.Questions;
import org.dchbx.coveragehq.models.ridp.VerifyIdentityResponse;
import org.dchbx.coveragehq.models.startup.EffectiveDate;
import org.dchbx.coveragehq.models.startup.OpenEnrollmentStatus;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.HashMap;

public class ConfigurationStorageHandler extends IServerConfigurationStorageHandler {
    private static String TAG = "ConfigurationStorage";



    private SharedPreferences getSharedPreferences(){
        return BrokerApplication.getBrokerApplication().getSharedPreferences(BrokerApplication.getBrokerApplication().getString(R.string.sharedpreferencename), Context.MODE_PRIVATE);
    }

    private Gson getGson(){
        final GsonBuilder builder = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
                .registerTypeAdapter(LocalTime.class, new LocalTimeSerializer());
        return builder.create();
    }


    @Override
    public void store(ServerConfiguration serverConfiguration) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean("useFingerprintSensor", serverConfiguration.useFingerprintSensor);
        if (serverConfiguration.useFingerprintSensor) {
            editor.putString(BrokerApplication.getBrokerApplication().getString(R.string.shared_preference_encrypted_info), serverConfiguration.encryptedString);
        } else {
            editor.putString(BrokerApplication.getBrokerApplication().getString(R.string.shared_preference_account_name), serverConfiguration.accountName);
        }
        editor.putBoolean( BrokerApplication.getBrokerApplication().getString(R.string.shared_preference_remember_me), serverConfiguration.rememberMe);
        editor.putBoolean(BrokerApplication.getBrokerApplication().getString(R.string.have_front_insurance_card), serverConfiguration.haveFrontInsuranceCard);
        editor.putBoolean(BrokerApplication.getBrokerApplication().getString(R.string.have_rear_insurance_card), serverConfiguration.haveRearInsuranceCard);
        editor.putFloat("premiumFilter", (float) serverConfiguration.premiumFilter);
        editor.putFloat("deductibleFilter", (float) serverConfiguration.deductibleFilter);
        editor.putString("planShoppingParameters", JsonSerializer.serialize(serverConfiguration.planShoppingParameters));

        editor.commit();
    }

    @Override
    public void read(ServerConfiguration serverConfiguration) {
        SharedPreferences sharedPref = getSharedPreferences();
        serverConfiguration.useFingerprintSensor = getSharedPreferences().getBoolean("useFingerprintSensor", false);
        if (serverConfiguration.useFingerprintSensor){
            serverConfiguration.encryptedString = sharedPref.getString(BrokerApplication.getBrokerApplication().getString(R.string.shared_preference_encrypted_info), null);
        } else {
            serverConfiguration.accountName = sharedPref.getString(BrokerApplication.getBrokerApplication().getString(R.string.shared_preference_account_name), null);
        }
        serverConfiguration.rememberMe = sharedPref.getBoolean(BrokerApplication.getBrokerApplication().getString(R.string.shared_preference_remember_me), true);
        serverConfiguration.haveFrontInsuranceCard = sharedPref.getBoolean(BrokerApplication.getBrokerApplication().getString(R.string.have_front_insurance_card), false);
        serverConfiguration.haveRearInsuranceCard = sharedPref.getBoolean(BrokerApplication.getBrokerApplication().getString(R.string.have_rear_insurance_card), false);
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

    @Override
    public void store(ServiceManager.AppConfig appConfig) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        Gson gson = getGson();
        String jsonString = gson.toJson(appConfig);
        editor.putString("AppConfigJson", jsonString);
        editor.commit();
    }

    @Override
    public boolean read(ServiceManager.AppConfig appConfig) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        String appConfigJson = sharedPreferences.getString("AppConfigJson", null);
        if (appConfigJson == null){
            return false;
        }
        Gson gson = getGson();
        ServiceManager.AppConfig fromJsonAppConfig = gson.fromJson(appConfigJson, ServiceManager.AppConfig.class);
        appConfig.DataSource = fromJsonAppConfig.DataSource;
        appConfig.EnrollServerUrl = fromJsonAppConfig.EnrollServerUrl;
        appConfig.GithubUrl = fromJsonAppConfig.GithubUrl;
        appConfig.MobileServerUrl = fromJsonAppConfig.MobileServerUrl;
        return true;
    }

    @Override
    public void store(Account account) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        Gson gson = getGson();
        String jsonString = gson.toJson(account);
        editor.putString("AccountJson", jsonString);
        editor.commit();
    }

    @Override
    public Account readAccount() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        String accountJson = sharedPreferences.getString("AccountJson", null);
        if (accountJson == null
            || accountJson.length() == 0){
            return new Account();
        }
        Gson gson = getGson();
        Account account = gson.fromJson(accountJson, Account.class);
        if (account == null){
            return new Account();
        }
        return account;
    }

    @Override
    public void store(Questions questions) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        Gson gson = getGson();
        String jsonString = gson.toJson(questions);
        editor.putString("RidpQuestionsJson", jsonString);
        editor.remove("RidpAnswersJson");
        editor.commit();
    }

    @Override
    public Questions readQuestions() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        String questionsJson = sharedPreferences.getString("RidpQuestionsJson", null);
        if (questionsJson == null
                || questionsJson.length() == 0){
            return null;
        }
        Gson gson = getGson();
        Questions questions = gson.fromJson(questionsJson, Questions.class);
        return questions;
    }

    public Answers readAnswers() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        String answersJson = sharedPreferences.getString(Answers.class.getName(), null);
        if (answersJson == null
                || answersJson.length() == 0){
            return null;
        }
        Gson gson = getGson();
        Answers answers = gson.fromJson(answersJson, Answers.class);
        return answers;

    }

    public <T> void store(T t) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        Gson gson = getGson();
        String jsonString = gson.toJson(t);
        String name = t.getClass().getName();
        editor.putString(name, jsonString);
        editor.commit();
    }

    public <T> void store(String name, T t) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        Gson gson = getGson();
        String jsonString = gson.toJson(t);
        editor.putString(name, jsonString);
        editor.commit();
    }

    @Override
    public VerifyIdentityResponse readVerifiyIdentityResponse() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        String responseJson = sharedPreferences.getString(VerifyIdentityResponse.class.getName(), null);
        if (responseJson == null
                || responseJson.length() == 0){
            return null;
        }
        Gson gson = getGson();
        return gson.fromJson(responseJson, VerifyIdentityResponse.class);
    }

    @Override
    public String readStateString() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        String responseJson = sharedPreferences.getString("StateString", null);
        return responseJson;
    }

    @Override
    public Family readUqhpFamily() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        String responseJson = sharedPreferences.getString("UqhpFamily", null);
        if (responseJson == null
                || responseJson.length() == 0){
            Family family = new Family();
            family.Person = new JsonArray();
            family.Relationship = new HashMap<>();
            family.Attestation = new JsonObject();
            return family;
        }
        Gson gson = getGson();
        return gson.fromJson(responseJson, Family.class);
    }

    @Override
    public UqhpDetermination readUqhpDetermination() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        String json = sharedPreferences.getString("UqhpDetermination", null);
        if (json != null){
            Gson gson = getGson();
            return gson.fromJson(json, UqhpDetermination.class);
        }
        return null;
    }

    @Override
    public void storeEffectiveDate(EffectiveDate effectiveDate) {
        store("EffectiveDate", effectiveDate);
    }

    @Override
    public EffectiveDate readEffectiveDate(){
        SharedPreferences sharedPreferences = getSharedPreferences();
        String json = sharedPreferences.getString("EffectiveDate", null);
        if (json != null){
            Gson gson = getGson();
            return gson.fromJson(json, EffectiveDate.class);
        }
        return null;
    }

    @Override
    public void storeOpenEnrollmentStatus(OpenEnrollmentStatus openEnrollmentStatus) {
        store("OpenEnrollmentStatus", openEnrollmentStatus);
    }

    @Override
    public void storeUqhpFamily(Family family){
        store("UqhpFamily", family);
    }

    public void storeStateString(String stateString) {
        Log.d(TAG, "StateString: ->" + stateString + "<-");
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString("StateString", stateString);
        editor.commit();
    }

}
