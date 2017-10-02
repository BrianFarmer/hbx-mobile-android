package org.dchbx.coveragehq;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

/**
 * Created by plast on 2/7/2017.
 */

public class EnrollCoverageUnitTest {
    @Test
    public void validateUserAndPasswordTest() throws Exception {
        EnrollConfigBase config = null;
        BrokerApplication brokerApplication = new BrokerApplication();
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        JsonParser jsonParser = new JsonParser();
        EnrollUrlHandler enrollUrlHandler = new EnrollUrlHandler(serverConfiguration, jsonParser);
        EnrollConnectionHandler enrollConnectionHandler = new EnrollConnectionHandler(serverConfiguration);
        IDataCache dataCache = new DataCache();
        IdentityEncryptionImpl identityEncryption = new IdentityEncryptionImpl();
        ServiceManager serviceManager = ServiceManager.getServiceManager();
        ConfigurationStorageHandler configurationStorageHandler = serviceManager.getConfigurationStorageHandler();
        EnrollCoverageConnection enrollCoverageConnection = new EnrollCoverageConnection(enrollUrlHandler, enrollConnectionHandler,
                serverConfiguration,jsonParser, dataCache, configurationStorageHandler, serviceManager);
        CoverageConnection.LoginResult loadTest16 = enrollCoverageConnection.validateUserAndPassword("LoadTest16", "Beta123!", true, false);
        Assert.assertThat(loadTest16 == CoverageConnection.LoginResult.Success, is(true));
        enrollCoverageConnection.checkSecurityAnswer("Test");
    }
}
