package org.dchbx.coveragehq;

/**
 * Created by plast on 7/25/2016.
 */
public interface ModelManagerInterface {
    void login(String accountName, String password);
    void getBrokers();
    void getBroker();
}
