package org.dchbx.coveragehq;

import org.dchbx.coveragehq.models.ridp.Questions;
import org.dchbx.coveragehq.models.ridp.VerifiyIdentityResponse;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

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
    public abstract boolean read(ServiceManager.AppConfig appConfig);
    public abstract void store(org.dchbx.coveragehq.models.account.Account account);
    public abstract org.dchbx.coveragehq.models.account.Account readAccount();

    public abstract void store(Questions questions);
    public abstract Questions readQuestions();

    public abstract VerifiyIdentityResponse readVerifiyIdentityResponse();
}

