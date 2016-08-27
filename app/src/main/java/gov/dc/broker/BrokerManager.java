package gov.dc.broker;

/**
 * Created by plast on 7/28/2016.
 */
public class BrokerManager {
    static private GitBrokerManager gitBrokerManager = new GitBrokerManager();

    static public  BrokerManagerInterface getDefault(){
        return gitBrokerManager;
    }
}
