package org.dchbx.coveragehq;

import android.util.Log;

import org.dchbx.coveragehq.models.brokeragency.BrokerAgency;
import org.dchbx.coveragehq.models.employer.Employer;
import org.dchbx.coveragehq.models.roster.Roster;
import org.dchbx.coveragehq.models.roster.RosterEntry;
import org.dchbx.coveragehq.models.services.Service;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;

/**
 * Created by plast on 12/16/2016.
 */

public class DataCache implements IDataCache {
    private static String TAG = "DataCache";
    private static String Default_Id = "default";
    private BrokerAgency brokerAgency;
    private DateTime brokerAgencyTime;
    private HashMap<String, CachedData<Employer>> employers = new HashMap<>();
    private HashMap<String, CachedData<Roster>> rosters = new HashMap<>();
    private HashMap<String, CachedData<List<Service>>> servicesCache = new HashMap<>();
    private HashMap<String, CachedData<RosterEntry>> individuals = new HashMap<>();

    @Override
    public void store(BrokerAgency brokerAgency, DateTime time) {
        this.brokerAgency = brokerAgency;
        brokerAgencyTime = time;
    }

    @Override
    public void store(Employer employer, DateTime time) {
        store(Default_Id, employer, time);
    }

    @Override
    public void store(String id, Employer employer, DateTime time) {
        employers.put(id, new CachedData<>(employer, time));
    }

    @Override
    public void store(Roster roster, DateTime time) {
        store(Default_Id, roster, time);
    }

    @Override
    public void store(String id, Roster roster, DateTime time) {
        rosters.put(id, new CachedData<>(roster, time));
    }

    @Override
    public void store(String id, List<Service> services, DateTime time) {
        servicesCache.put(id, new CachedData<>(services, time));
    }

    @Override
    public void store(String id, RosterEntry individual, DateTime now) {
        individuals.put(id, new CachedData<>(individual, now));
    }

    @Override
    public BrokerAgency getBrokerAgency(DateTime time) {
        if (brokerAgencyTime != null
        && time.compareTo(brokerAgencyTime.plus(BrokerWorkerConfig.config().enrollConfig().getCacheTimeout()))<=0){
            return brokerAgency;
        }
        return null;
    }

    @Override
    public Employer getEmployer(DateTime time) {
        return getEmployer(Default_Id, time);
    }

    @Override
    public Employer getEmployer(String id, DateTime time) {
        if (employers.containsKey(id)){
            CachedData<Employer> employerCachedData = employers.get(id);
            if (time.compareTo(employerCachedData.time.plus(BrokerWorkerConfig.config().enrollConfig().getCacheTimeout())) <= 0){
                return employerCachedData.storedData;
            }
        }
        return null;
    }

    //@Override
    //public Roster getRoster(DateTime time) {
    //    return getRoster(Default_Id, time);
    //}

    @Override
    public Roster getRoster(String id, DateTime time) {
        if (rosters.containsKey(id)){
            CachedData<Roster> rosterCachedData = rosters.get(id);
            if (time.compareTo(rosterCachedData.time.plus(BrokerWorkerConfig.config().enrollConfig().getCacheTimeout())) <= 0){
                return rosterCachedData.storedData;
            }
        }
        return null;
    }

    @Override
    public List<Service> getServices(String id, DateTime time) {
        if (servicesCache.containsKey(Default_Id)){
            CachedData<List<Service>> cachedData = servicesCache.get(Default_Id);
            if (time.compareTo(cachedData.time.plus(BrokerWorkerConfig.config().enrollConfig().getCacheTimeout())) <= 0){
                Log.d(TAG, "Found cached service: " + id + " size: " + cachedData.storedData.size());
                return cachedData.storedData;
            }
        }
        Log.d(TAG, "No cached service found: " + id);
        return null;
    }

    @Override
    public Roster getRoster(DateTime time) {
        if (rosters.containsKey(Default_Id)){
            CachedData<Roster> rosterCachedData = rosters.get(Default_Id);
            if (time.compareTo(rosterCachedData.time.plus(BrokerWorkerConfig.config().enrollConfig().getCacheTimeout())) <= 0){
                return rosterCachedData.storedData;
            }
        }
        return null;
    }

    @Override
    public void clear() {
        brokerAgency = null;
        employers.clear();
        rosters.clear();
    }

}

