package gov.dc.broker;

import java.util.ArrayList;

/**
 * Created by plast on 7/27/2016.
 */
public class Account {
    private Broker broker;
    private ArrayList<SubscriberPlan> subscriberPlans;

    public Account(){}
    public Account(Broker broker){
        this.broker = broker;
    }

    public Broker getBroker() {
        return broker;
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    public ArrayList<SubscriberPlan> getSubscriberPlans() {
        return subscriberPlans;
    }

    public void setSubscriberPlans(ArrayList<SubscriberPlan> subscriberPlans) {
        this.subscriberPlans = subscriberPlans;
    }
}
