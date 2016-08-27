package gov.dc.broker;

/**
 * Created by plast on 7/26/2016.
 */
public class BrokerResult {
    Broker broker;

    public BrokerResult(Broker broker) {
        this.broker = broker;
    }

    public Broker getBroker(){
        return broker;
    }
}
