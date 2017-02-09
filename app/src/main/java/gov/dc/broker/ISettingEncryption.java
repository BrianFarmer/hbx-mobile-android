package gov.dc.broker;

/**
 * Created by plast on 1/31/2017.
 */

public interface ISettingEncryption {
    String encrypt(String str);
    String decrypt(String str);
}
