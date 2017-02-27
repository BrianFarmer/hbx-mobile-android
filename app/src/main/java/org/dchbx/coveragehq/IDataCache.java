package org.dchbx.coveragehq;

import org.joda.time.DateTime;

import org.dchbx.coveragehq.models.brokeragency.BrokerAgency;
import org.dchbx.coveragehq.models.employer.Employer;
import org.dchbx.coveragehq.models.roster.Roster;

public interface IDataCache {
    void store(BrokerAgency brokerAgency, DateTime time);
    void store(String id, Employer employer, DateTime time);
    void store(Employer employer, DateTime time);
    void store(Roster roster, DateTime time);
    void store(String id, Roster roster, DateTime time);
    BrokerAgency getBrokerAgency(DateTime time);
    Employer getEmployer(DateTime time);
    Employer getEmployer(String id, DateTime time);
    Roster getRoster(DateTime time);
    Roster getRoster(String id, DateTime time);
    void clear();

}
