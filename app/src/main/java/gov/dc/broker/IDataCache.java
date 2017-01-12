package gov.dc.broker;

import org.joda.time.DateTime;

import gov.dc.broker.models.brokeragency.BrokerAgency;
import gov.dc.broker.models.employer.Employer;
import gov.dc.broker.models.roster.Roster;

public interface IDataCache {
    void store(BrokerAgency brokerAgency, DateTime time);
    void store(String id, Employer employer, DateTime time);
    void store(Employer employer, DateTime time);
    void store(Roster roster, DateTime time);
    void store(String id, Roster roster, DateTime time);
    BrokerAgency getBrokerAgency(DateTime time);
    Employer getEmployer(DateTime time);
    Employer getEmployer(String id, DateTime time);
    Roster getRoster(String id, DateTime time);
    void clear();

}
