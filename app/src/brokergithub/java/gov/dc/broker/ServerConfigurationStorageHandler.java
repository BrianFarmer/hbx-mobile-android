package gov.dc.broker;

/**
 * Created by plast on 12/15/2016.
 */
public abstract class ServerConfigurationStorageHandler {
    public abstract void store(ServerConfiguration serverConfiguration);
}

class GitServerConfigurationStorageHandler extends ServerConfigurationStorageHandler{

    @Override
    public void store(ServerConfiguration serverConfiguration) {

    }
}
