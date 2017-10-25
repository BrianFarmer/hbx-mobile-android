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
import org.dchbx.coveragehq.models.ridp.SignUp.SignUpResponse;
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
    private static String SharedPreferenceName = "IvlSharedPreferences";

    private static String ENCRYPTION_ALGORITHM_WITH_PADDING = "AES/ECB/PKCS5Padding";
    private static String ENCRYPTION_ALGORITHM = "AES";
    private static String CHARACTER_SET_UTF8 = "UTF-8";
    private String currentAccount;
    private String currentKey;
    private IEncryption encryption;

    public ConfigurationStorageHandler(){
        encryption = new IdentityEncryption();
    }

    private SharedPreferences getSharedPreferences(String sharedName) {
        return BrokerApplication.getBrokerApplication().getSharedPreferences(sharedName, Context.MODE_PRIVATE);
    }

    @Override
    public void store(ServerConfiguration serverConfiguration) {
        SharedPreferences.Editor editor = getSharedPreferences(getCurrentAccount()).edit();
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
        SharedPreferences sharedPref = getSharedPreferences(getCurrentAccount());
        serverConfiguration.useFingerprintSensor = getSharedPreferences(getCurrentAccount()).getBoolean("useFingerprintSensor", false);
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
        SharedPreferences sharedPref = getSharedPreferences(getCurrentAccount());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
    }

    @Override
    public void store(ServiceManager.AppConfig appConfig) {
        store(appConfig);
    }

    @Override
    public boolean read(ServiceManager.AppConfig appConfig) throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException {
        SharedPreferences sharedPreferences = getSharedPreferences(getCurrentAccount());
        String appConfigJson = sharedPreferences.getString("AppConfigJson", null);
        if (appConfigJson == null){
            return false;
        }
        Gson gson = getGson();
        ServiceManager.AppConfig fromJsonAppConfig = gson.fromJson(encryption.decrypt(appConfigJson), ServiceManager.AppConfig.class);
        appConfig.DataSource = fromJsonAppConfig.DataSource;
        appConfig.EnrollServerUrl = fromJsonAppConfig.EnrollServerUrl;
        appConfig.GithubUrl = fromJsonAppConfig.GithubUrl;
        appConfig.MobileServerUrl = fromJsonAppConfig.MobileServerUrl;
        return true;
    }

    @Override
    public void setAccountName(String accountName){
        currentAccount = accountName;
        currentKey = currentAccount.substring(0,16);
    }

    @Override
    public void storeAccount(Account account) throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeyException, InvalidKeySpecException {
        store(Account.class.getName(), account);
    }

    @Override
    public Account readAccount() throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException {
        Account account = read(Account.class.getName(), Account.class);
        if (account == null) {
            return new Account();
        }
        return account;
    }

    @Override
    public void storeQuestions(Questions questions) throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeyException, InvalidKeySpecException {
        store(Questions.class.getName(), questions);
    }

    @Override
    public Questions readQuestions() throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException {
        return read(Questions.class.getName(), Questions.class);
    }

    public Answers readAnswers() throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException {
        return read(Answers.class.getName(), Answers.class);
    }

    /*
    public <T> void store(T t) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        Gson gson = getGson();
        String jsonString = gson.toJson(t);
        String name = t.getClass().getName();
        editor.putString(name, encrypt(jsonString));
        editor.commit();
    }*/

    protected <T> void store(String name, T t) throws NoSuchPaddingException, InvalidKeySpecException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidParameterSpecException {
        store(name, t, getCurrentAccount(), true);
    }

    protected <T> void store(String name, T t, String sharedName, boolean encrypt) throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeyException, InvalidKeySpecException {
        SharedPreferences.Editor editor = getSharedPreferences(sharedName).edit();
        Gson gson = getGson();
        String jsonString = gson.toJson(t);
        if (encrypt) {
            String encrypted = encryption.encrypt(jsonString);
            editor.putString(name, encrypted);
        } else {
            editor.putString(name, jsonString);
        }
        editor.commit();
    }

    protected <T> T read(String name, Class c) throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException {
        return read(name, getCurrentAccount(), c, true);
    }

    protected <T> T read(String name, String sharedName, Class c, boolean decrypt) throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException {
        SharedPreferences sharedPreferences = getSharedPreferences(sharedName);
        String responseJson = sharedPreferences.getString(c.getName(), null);
        if (responseJson == null
                || responseJson.length() == 0){
            return null;
        }
        Gson gson = getGson();
        if (decrypt) {
            String decrypted = encryption.decrypt(responseJson);
            return (T) gson.fromJson(decrypted, c);
        }
        return (T)gson.fromJson(responseJson, c);
    }


    @Override
    public VerifyIdentityResponse readVerifiyIdentityResponse() throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException {
        return read(VerifyIdentityResponse.class.getName(), VerifyIdentityResponse.class);
    }

    @Override
    public String readStateString() {
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceName);
        String responseJson = sharedPreferences.getString("StateString", null);
        return responseJson;
    }

    @Override
    public Family readUqhpFamily() throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException {
        Family family = read(Family.class.getName(), Family.class);
        if (family == null){
            family = new Family();
            family.Person = new JsonArray();
            family.Relationship = new HashMap<>();
            family.Attestation = new JsonObject();
        }
        return family;
    }

    @Override
    public UqhpDetermination readUqhpDetermination() throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException {
        return read(UqhpDetermination.class.getName(), UqhpDetermination.class);
    }

    @Override
    public void storeEffectiveDate(EffectiveDate effectiveDate) throws NoSuchPaddingException, InvalidKeySpecException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidParameterSpecException {
        store(EffectiveDate.class.getName(), effectiveDate, SharedPreferenceName, false);
    }

    @Override
    public EffectiveDate readEffectiveDate() throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException {
        return read(EffectiveDate.class.getName(), SharedPreferenceName, EffectiveDate.class, false);
    }

    @Override
    public void storeOpenEnrollmentStatus(OpenEnrollmentStatus openEnrollmentStatus) throws NoSuchPaddingException, InvalidKeySpecException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidParameterSpecException {
        store(OpenEnrollmentStatus.class.getName(), openEnrollmentStatus, SharedPreferenceName, false);
    }

    @Override
    public void storeUqhpFamily(Family family) throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeyException, InvalidKeySpecException {
        store(Family.class.getName(), family);
    }

    @Override
    public void storeUqhpDetermination(UqhpDetermination uqhpDetermination) throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeyException, InvalidKeySpecException {
        store(UqhpDetermination.class.getName(), uqhpDetermination);
    }

    @Override
    public void storeSignupResponse(SignUpResponse signUpResponse) throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeyException, InvalidKeySpecException {
        setAccountName(signUpResponse.uuid);
        store(SignUpResponse.class.getName(), signUpResponse);
    }

    @Override
    public void storeAnswers(Answers answers) throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeyException, InvalidKeySpecException {
        store(Answers.class.getName(), answers);
    }

    public String getCurrentAccount() {
        return currentAccount;
    }

    public class IdentityEncryption implements IEncryption {
        @Override
        public String encrypt(String jsonString){
            return jsonString;
        }

        @Override
        public String decrypt(String accountJson) {
            return accountJson;
        }
    }

    interface IEncryption {
        String encrypt(String jsonString) throws NoSuchPaddingException, UnsupportedEncodingException, InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidParameterSpecException;
        public String decrypt(String accountJson) throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException;
    }


    class RealEncryption implements IEncryption {
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
            return new SecretKeySpec(currentKey.getBytes(), ENCRYPTION_ALGORITHM);
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
            try {
                cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e);
            } catch (Throwable t) {
                Log.e(TAG, "throwable: " + t);
                throw t;
            }
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
        public String encrypt(String jsonString) throws NoSuchPaddingException, UnsupportedEncodingException,
                InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException,
                BadPaddingException, InvalidKeyException, InvalidParameterSpecException {
            byte[] encrypted = encryptData(jsonString);
            jsonString = Base64.encodeToString(encrypted, Base64.NO_WRAP);
            return jsonString;
        }

        /**
         * @param accountJson
         * @return
         */
        public String decrypt(String accountJson) throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException {
            accountJson = decryptData(Base64.decode(accountJson, Base64.NO_WRAP));
            return accountJson;
        }
    }
}
