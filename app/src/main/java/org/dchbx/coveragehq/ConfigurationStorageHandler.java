package org.dchbx.coveragehq;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
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

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static org.dchbx.coveragehq.Utilities.getGson;

public class ConfigurationStorageHandler extends IServerConfigurationStorageHandler {
    private static String TAG = "ConfigurationStorage";
    private static String UqhpFamily = "UqhpFamily";

    private static String ENCRYPTION_ALGORITHM_WITH_PADDING = "AES/ECB/PKCS5Padding";
    private static String ENCRYPTION_ALGORITHM = "AES";
    private static String CHARACTER_SET_UTF8 = "UTF-8";

    private SharedPreferences getSharedPreferences(){
        return BrokerApplication.getBrokerApplication().getSharedPreferences(BrokerApplication.getBrokerApplication().getString(R.string.sharedpreferencename), Context.MODE_PRIVATE);
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
        SharedPreferences sharedPref = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
    }

    @Override
    public void store(ServiceManager.AppConfig appConfig) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        String jsonString = Utilities.getJson(appConfig);
        editor.putString("AppConfigJson", encrypt(jsonString));
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
        ServiceManager.AppConfig fromJsonAppConfig = gson.fromJson(decrypt(appConfigJson), ServiceManager.AppConfig.class);
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
        editor.putString("AccountJson", encrypt(jsonString));
        editor.commit();
    }

    @Override
    public void clearAccount() {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.remove("AccountJson");
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
        Account account = gson.fromJson(decrypt(accountJson), Account.class);
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
        editor.putString("RidpQuestionsJson", encrypt(jsonString));
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
        Questions questions = gson.fromJson(decrypt(questionsJson), Questions.class);
        return questions;
    }

    @Override
    public void clearAnswers() {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.remove(Answers.class.getName());
        editor.commit();
    }

    public Answers readAnswers() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        String answersJson = sharedPreferences.getString(Answers.class.getName(), null);
        if (answersJson == null
                || answersJson.length() == 0){
            return null;
        }
        Gson gson = getGson();
        Answers answers = gson.fromJson(decrypt(answersJson), Answers.class);
        return answers;

    }

    public <T> void store(T t) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        Gson gson = getGson();
        String jsonString = gson.toJson(t);
        String name = t.getClass().getName();
        editor.putString(name, encrypt(jsonString));
        editor.commit();
    }

    public <T> void store(String name, T t) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        Gson gson = getGson();
        String jsonString = gson.toJson(t);
        editor.putString(name, encrypt(jsonString));
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
        return gson.fromJson(decrypt(responseJson), VerifyIdentityResponse.class);
    }

    @Override
    public String readStateString() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        String responseJson = sharedPreferences.getString("StateString", null);
        return responseJson;
    }

    @Override
    public void clearUqhpFamily(){
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.remove(UqhpFamily);
        editor.commit();

    }

    @Override
    public Family readUqhpFamily() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        String responseJson = sharedPreferences.getString(UqhpFamily, null);
        if (responseJson == null
                || responseJson.length() == 0){
            Family family = new Family();
            family.Person = new JsonArray();
            family.Relationship = new HashMap<>();
            family.Attestation = new JsonObject();
            return family;
        }
        Gson gson = getGson();
        return gson.fromJson(decrypt(responseJson), Family.class);
    }

    @Override
    public UqhpDetermination readUqhpDetermination() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        String json = sharedPreferences.getString("UqhpDetermination", null);
        if (json != null){
            Gson gson = getGson();
            return gson.fromJson(decrypt(json), UqhpDetermination.class);
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
            return gson.fromJson(decrypt(json), EffectiveDate.class);
        }
        return null;
    }

    @Override
    public void storeOpenEnrollmentStatus(OpenEnrollmentStatus openEnrollmentStatus) {
        store("OpenEnrollmentStatus", openEnrollmentStatus);
    }

    @Override
    public void storeUqhpFamily(Family family){
        store(UqhpFamily, family);
    }

    @Override
    public void storeUqhpDetermination(UqhpDetermination uqhpDetermination){
        store("UqhpDetermination", uqhpDetermination);
    }
    public void storeStateString(String stateString) {
        Log.d(TAG, "StateString: ->" + stateString + "<-");
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString("StateString", stateString);
        editor.commit();
    }

    /**
     * Uses a predefined secret key.
     * TODO: Use a specific one defined for this purpose, maybe?
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private SecretKey getSecretKey()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        return new SecretKeySpec(BrokerApplication.getBrokerApplication().getString(R.string.secret_key).getBytes(),
                ENCRYPTION_ALGORITHM);
    }

    /**
     * Returns the encrypted data.
     *
     * @param message
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidParameterSpecException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeySpecException
     */
    private byte[] encryptData(String message)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException,
            UnsupportedEncodingException, InvalidKeySpecException {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM_WITH_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
        return cipher.doFinal(message.getBytes(CHARACTER_SET_UTF8));
    }

    /**
     * Returns the decrypted data.
     *
     * @param cipherText
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidParameterSpecException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeySpecException
     */
    private String decryptData(byte[] cipherText)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeySpecException {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM_WITH_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
        return new String(cipher.doFinal(cipherText), CHARACTER_SET_UTF8);
    }

    /**
     * @param jsonString
     * @return
     */
    private String encrypt(String jsonString) {
        try {
            byte[] encrypted = encryptData(jsonString);
            jsonString = Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        return jsonString;
    }

    /**
     * @param accountJson
     * @return
     */
    private String decrypt(String accountJson) {
        try {
            accountJson = decryptData(Base64.decode(accountJson, Base64.NO_WRAP));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        return accountJson;
    }
}
