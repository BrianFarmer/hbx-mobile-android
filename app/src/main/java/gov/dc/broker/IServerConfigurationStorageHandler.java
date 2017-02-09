package gov.dc.broker;

/**
 * Created by plast on 12/15/2016.
 */
public abstract class IServerConfigurationStorageHandler {
    public abstract void store(ServerConfiguration serverConfiguration);
    public abstract void read(ServerConfiguration serverConfiguration);
    public abstract void clear();
}

