package org.dchbx.coveragehq;

import org.dchbx.coveragehq.models.fe.Family;
import org.dchbx.coveragehq.models.fe.UqhpDetermination;
import org.dchbx.coveragehq.models.ridp.Answers;
import org.dchbx.coveragehq.models.ridp.Questions;
import org.dchbx.coveragehq.models.ridp.SignUp.SignUpResponse;
import org.dchbx.coveragehq.models.ridp.VerifyIdentityResponse;
import org.dchbx.coveragehq.models.startup.EffectiveDate;
import org.dchbx.coveragehq.models.startup.OpenEnrollmentStatus;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by plast on 12/15/2016.
 */
public abstract class IServerConfigurationStorageHandler {
    public abstract void store(ServerConfiguration serverConfiguration) throws BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchProviderException, InvalidKeyException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException;
    public abstract void read(ServerConfiguration serverConfiguration) throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, KeyStoreException, IllegalBlockSizeException;
    public abstract void clear();
    public abstract void store(ServiceManager.AppConfig appConfig);
    public abstract boolean read(ServiceManager.AppConfig appConfig) throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException;

    abstract void setAccountName(String accountName);

    public abstract void storeAccount(org.dchbx.coveragehq.models.account.Account account) throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeyException, InvalidKeySpecException;
    public abstract org.dchbx.coveragehq.models.account.Account readAccount() throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException;
    public abstract void storeQuestions(Questions questions) throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeyException, InvalidKeySpecException;
    public abstract Questions readQuestions() throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException;
    public abstract VerifyIdentityResponse readVerifiyIdentityResponse() throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException;
    public abstract String readStateString();
    public abstract Family readUqhpFamily() throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException;
    public abstract void storeUqhpFamily(Family family) throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeyException, InvalidKeySpecException;
    public abstract UqhpDetermination readUqhpDetermination() throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException;
    public abstract void storeEffectiveDate(EffectiveDate effectiveDate) throws NoSuchPaddingException, InvalidKeySpecException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidParameterSpecException;
    public abstract EffectiveDate readEffectiveDate() throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException;
    public abstract void storeOpenEnrollmentStatus(OpenEnrollmentStatus openEnrollmentStatus) throws NoSuchPaddingException, InvalidKeySpecException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidParameterSpecException;
    public abstract void storeUqhpDetermination(UqhpDetermination uqhpDetermination) throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeyException, InvalidKeySpecException;
    public abstract void storeSignupResponse(SignUpResponse signUpResponse) throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeyException, InvalidKeySpecException;
    public abstract void storeAnswers(Answers answers) throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeyException, InvalidKeySpecException;
}

