package org.dchbx.coveragehq;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Base64;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import static android.content.Context.KEYGUARD_SERVICE;
import static android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import static android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import static android.hardware.fingerprint.FingerprintManager.CryptoObject;

/**
 * Created by plast on 2/1/2017.
 */

@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintManager {
    private static final String TAG = "FingerprintManager";

    private static final String KEY_NAME = "androidHive";

    private static FingerprintManager fingerprintManagerSingleton = null;
    private static final String TRANSFORMATION = KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7;

    private final EventBus eventBus;
    private android.hardware.fingerprint.FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private Cipher cipher;
    private KeyStore keyStore;
    private boolean enrolledFingerprints;
    private boolean keyguardSecure;
    private boolean hardwareSupported = false;

    private int watchedCount = 0;
    private boolean badState;
    private CryptoObject cryptoObject;
    private byte[] iv;
    private CancellationSignal cancellationSignal;

    private String accountName;
    private String password;
    private IAuthenticationEncryptResult authenticationEncryptResult;
    private IAuthenticationDecryptResult authenticationDecryptResult;
    private String encryptedMessages;
    private String encryptedText;

    public static FingerprintManager getInstance() {
        return fingerprintManagerSingleton;
    }

    public enum RequestStatus {
        NotInitialized,
        Success,
        Error
    }

    public class InitializationResult {
        public RequestStatus requestStatus;
        public boolean enrolledFingerprints;
        public boolean keyguardSecure;
    }

    public class DetectFingerprintResult {
        public boolean osSupportsFingerprint = false;
        public boolean hardwarePresent = false;
        public boolean fingerprintRegistered = false;
    }

    public FingerprintManager(EventBus eventBus){
        this.eventBus = eventBus;
        watchedCount = 0;
    }

    public static FingerprintManager build(EventBus eventBus){
        if (fingerprintManagerSingleton != null) {
            return fingerprintManagerSingleton;
        }

        fingerprintManagerSingleton = new FingerprintManager(eventBus);
        return fingerprintManagerSingleton;
    }


    public DetectFingerprintResult detect(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Fingerprint API only available on from Android 6.0 (M)
            BrokerApplication brokerApplication = BrokerApplication.getBrokerApplication();
            android.hardware.fingerprint.FingerprintManager fingerprintManager = (android.hardware.fingerprint.FingerprintManager) brokerApplication.getSystemService(Context.FINGERPRINT_SERVICE);

            if (!fingerprintManager.isHardwareDetected()) {
                DetectFingerprintResult detectFingerprintResult = new DetectFingerprintResult();
                detectFingerprintResult.osSupportsFingerprint = true;
                return detectFingerprintResult;
            } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                DetectFingerprintResult detectFingerprintResult = new DetectFingerprintResult();
                detectFingerprintResult.hardwarePresent = true;
                detectFingerprintResult.osSupportsFingerprint = true;
                detectFingerprintResult.fingerprintRegistered = false;
                return detectFingerprintResult;
            } else {
                DetectFingerprintResult detectFingerprintResult = new DetectFingerprintResult();
                detectFingerprintResult.osSupportsFingerprint = true;
                detectFingerprintResult.hardwarePresent = true;
                detectFingerprintResult.fingerprintRegistered = true;
                return detectFingerprintResult;
            }
        }
        return new DetectFingerprintResult();
    }


    public boolean init(boolean encrypt) throws InvalidParameterSpecException, InvalidAlgorithmParameterException {

        fingerprintManager = (android.hardware.fingerprint.FingerprintManager)BrokerApplication.getBrokerApplication().getSystemService(Context.FINGERPRINT_SERVICE);
        if (!fingerprintManager.isHardwareDetected()) {
            return false;
        }

        keyguardManager = (KeyguardManager) BrokerApplication.getBrokerApplication().getSystemService(KEYGUARD_SERVICE);
        boolean enrolledFingerprints = fingerprintManager.hasEnrolledFingerprints();
        boolean keyguardSecure = keyguardManager.isKeyguardSecure();
        if (enrolledFingerprints
                && keyguardSecure) {
            try {
                if (encrypt){
                    generateKey();
                    cipherInitForEncrypt();
                } else {
                    SecretKey secretKey = loadKey();
                    cipherInitForDecrypt(secretKey);
                }
                cryptoObject = new CryptoObject(cipher);
                cancellationSignal = new CancellationSignal();
            }
            catch (NoSuchAlgorithmException | NoSuchPaddingException | KeyStoreException
                   | CertificateException | UnrecoverableKeyException | IOException
                   | InvalidKeyException | NoSuchProviderException e) {
                badState = true;

                return false;
            }
        }
        return true;
    }

    public boolean isHardwareSupported() {
        return hardwareSupported;
    }

    public void release(){
        if (watchedCount > 0) {
            watchedCount--;
        } else {
            if (watchedCount <= 0) {

                keyguardManager = null;
                fingerprintManager = null;
            }
        }
    }

    public interface IAuthenticationDecryptResult {
        void error(CharSequence errString);
        void help(CharSequence helpString);
        void success(String accountName,  String password);
        void failed();
    }

    public interface IAuthenticationEncryptResult {
        void error(CharSequence errString);
        void help(CharSequence helpString);
        void success(String encryptedText);
        void failed();
    }

    public void authenticate(final String encryptedText, final IAuthenticationDecryptResult authenticationResult ) throws InvalidParameterSpecException, InvalidAlgorithmParameterException {

        Gson gson = new Gson();
        EncryptedString encryptedString = gson.fromJson(encryptedText, EncryptedString.class);
        this.encryptedText = encryptedString.string;
        this.iv = Base64.decode(encryptedString.iv, Base64.DEFAULT);
        this.authenticationDecryptResult = authenticationResult;
        init(false);

        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, new AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                super.onAuthenticationError(errMsgId, errString);
                authenticationResult.error(errString);
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                super.onAuthenticationHelp(helpMsgId, helpString);
                authenticationResult.help(helpString);
            }

            @Override
            public void onAuthenticationSucceeded(AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                try {
                    String decryptedString = decryptString(encryptedText);
                    Gson gson = new Gson();
                    StoredInfo storedInfo = gson.fromJson(decryptedString, StoredInfo.class);
                    authenticationResult.success(storedInfo.accountName, storedInfo.password);
                } catch (Throwable e) {
                    e.printStackTrace();
                    authenticationResult.error("Error decrypting");
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                authenticationResult.failed();
            }
        }, null);
    }

    public void authenticate(final String accountName, final String password, final IAuthenticationEncryptResult authenticationResult ) throws InvalidParameterSpecException, InvalidAlgorithmParameterException {
        this.accountName = accountName;
        this.password = password;
        this.authenticationEncryptResult = authenticationResult;
        init(true);

        /*
        if (watchedCount == 0){
            eventBus.post(new Events.FingerprintStatus("Fingerprint manager isn't initialized."));
            return;
        }*/

        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, new AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                super.onAuthenticationError(errMsgId, errString);
                authenticationResult.error(errString);
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                super.onAuthenticationHelp(helpMsgId, helpString);
                authenticationResult.help(helpString);
            }

            @Override
            public void onAuthenticationSucceeded(AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                try {
                    String encryptedText = encryptAccountInfo(accountName, password);
                    authenticationResult.success(encryptedText);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                authenticationResult.failed();
            }
        }, null);
    }

    public boolean checkTimedOut(){
        return false;
    }

    public Cipher cipherInitForEncrypt() throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, CertificateException, UnrecoverableKeyException, KeyStoreException, InvalidKeyException, InvalidParameterSpecException, InvalidAlgorithmParameterException {
        cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        keyStore.load(null);
        SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        IvParameterSpec parameterSpec = cipher.getParameters().getParameterSpec(IvParameterSpec.class);
        iv = parameterSpec.getIV();
        return cipher;
    }

    public Cipher cipherInitForDecrypt(SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, CertificateException, UnrecoverableKeyException, KeyStoreException, InvalidKeyException, InvalidParameterSpecException, InvalidAlgorithmParameterException {
        cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);

        //keyStore.load(null);
        //SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        return cipher;
    }

    private static String padString(String source) {
        char paddingChar = ' ';
        int size = 16;
        int x = source.length() % size;
        int padLength = size - x;
        for (int i = 0; i < padLength; i++) {
            source += paddingChar;
        }
        return source;
    }

    protected SecretKey generateKey() throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, IOException, CertificateException, InvalidAlgorithmParameterException {
        keyStore = KeyStore.getInstance("AndroidKeyStore");
        KeyGenerator keyGenerator;
        keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        keyStore.load(null);
        keyGenerator.init(new
                KeyGenParameterSpec.Builder(KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT |
                        KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(
                        KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build());
        return keyGenerator.generateKey();
    }

    protected SecretKey loadKey() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        return  (SecretKey) keyStore.getKey(KEY_NAME, null);
    }

    static class StoredInfo {
        public String accountName;
        public String password;
    }

    static class EncryptedString {
        public String iv;
        public String string;
    }

    static class EncryptedBytes {
        public byte[] iv;
        public byte[] string;
    }

    public static String build(byte[] encrypted, byte[] iv){
        EncryptedString encryptedString = new EncryptedString();
        encryptedString.iv = Base64.encodeToString(iv, Base64.DEFAULT);
        encryptedString.string = Base64.encodeToString(encrypted, Base64.DEFAULT);
        Gson gson = new Gson();
        return gson.toJson(encryptedString);
    }

    public static EncryptedBytes unbuild(String string){
        Gson gson = new Gson();
        EncryptedString encryptedString = gson.fromJson(string, EncryptedString.class);
        EncryptedBytes encryptedBytes = new EncryptedBytes();
        encryptedBytes.iv = Base64.decode(encryptedString.iv, Base64.DEFAULT);
        encryptedBytes.string = Base64.decode(encryptedString.string, Base64.DEFAULT);
        return encryptedBytes;
    }

    String encryptAccountInfo(String accountName, String password) throws NoSuchProviderException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException {
        StoredInfo storedInfo = new StoredInfo();
        storedInfo.accountName = accountName;
        storedInfo.password = password;
        Gson gson = new Gson();
        String json = gson.toJson(storedInfo);
        String paddedString = padString(json);
        byte[] bytesToEncode = paddedString.getBytes();
        byte[] bytes = cryptoObject.getCipher().doFinal(bytesToEncode);
        return build(bytes, iv);
    }

    String decryptString(String  string) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] passwordBytes = Base64.decode(this.encryptedText, Base64.DEFAULT);
        passwordBytes = cryptoObject.getCipher().doFinal(passwordBytes);
        return new String(passwordBytes);
    }
}
