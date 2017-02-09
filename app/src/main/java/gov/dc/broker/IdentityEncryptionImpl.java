package gov.dc.broker;

/**
 * Created by plast on 1/31/2017.
 */
public class IdentityEncryptionImpl implements ISettingEncryption {
    @Override
    public String encrypt(String str) {
        return str;
    }

    @Override
    public String decrypt(String str) {
        return str;
    }
}
