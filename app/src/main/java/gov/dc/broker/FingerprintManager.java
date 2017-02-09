package gov.dc.broker;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;

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

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static android.content.Context.KEYGUARD_SERVICE;
import static android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import static android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import static android.hardware.fingerprint.FingerprintManager.CryptoObject;

/**
 * Created by plast on 2/1/2017.
 */

@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintManager {
    private static final String KEY_NAME = "androidHive";

    private static FingerprintManager fingerprintManagerSingleton = null;

    private final EventBus eventBus;
    private android.hardware.fingerprint.FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private Cipher cipher;
    private KeyStore keyStore;
    private boolean enrolledFingerprints;
    private boolean keyguardSecure;

    private int watchedCount = 0;
    private boolean badState;
    private CryptoObject cryptoObject;
    private CancellationSignal cancellationSignal;

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

    public InitializationResult init(Events.GetFingerprintStatus getFingerprintStatus){
        watchedCount ++;

        if (watchedCount == 1) {
            badState = false;

            fingerprintManager = (android.hardware.fingerprint.FingerprintManager)BrokerApplication.getBrokerApplication().getSystemService(Context.FINGERPRINT_SERVICE);
            if (!fingerprintManager.isHardwareDetected()) {
                InitializationResult initializationResult = new InitializationResult();
                initializationResult.requestStatus = RequestStatus.NotInitialized;
                return initializationResult;
            }

            keyguardManager = (KeyguardManager) BrokerApplication.getBrokerApplication().getSystemService(KEYGUARD_SERVICE);
            boolean enrolledFingerprints = fingerprintManager.hasEnrolledFingerprints();
            boolean keyguardSecure = keyguardManager.isKeyguardSecure();
            if (enrolledFingerprints
                    && keyguardSecure) {
                try {
                    generateKey();
                    cipherInit();
                    cryptoObject = new CryptoObject(cipher);
                    cancellationSignal = new CancellationSignal();
                }
                catch (NoSuchAlgorithmException | NoSuchPaddingException | KeyStoreException
                       | CertificateException | UnrecoverableKeyException | IOException
                       | InvalidKeyException | NoSuchProviderException e) {
                    badState = true;

                    InitializationResult initializationResult = new InitializationResult();
                    initializationResult.requestStatus = RequestStatus.Error;
                    return initializationResult;
                }
            }
        }
        InitializationResult initializationResult = new InitializationResult();
        initializationResult.requestStatus = RequestStatus.Success;
        initializationResult.enrolledFingerprints = enrolledFingerprints;
        initializationResult.keyguardSecure = keyguardSecure;
        return initializationResult;
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

    public interface IAuthenticationResult {
        void error(CharSequence errString);
        void help(CharSequence helpString);
        void success();
        void failed();
    }

    public void authenticate(final IAuthenticationResult authenticationResult ){
        if (watchedCount == 0){
            eventBus.post(new Events.FingerprintStatus("Fingerprint manager isn't initialized."));
            return;
        }

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
                authenticationResult.success();
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

    public void cipherInit() throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, CertificateException, UnrecoverableKeyException, KeyStoreException, InvalidKeyException {
        cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);

        keyStore.load(null);
        SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
        cipher.init(Cipher.ENCRYPT_MODE, key);
    }

    protected void generateKey() throws NoSuchProviderException, NoSuchAlgorithmException {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }


        KeyGenerator keyGenerator;
        keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

        try {
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
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
