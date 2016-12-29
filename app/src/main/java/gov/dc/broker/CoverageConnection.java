package gov.dc.broker;

import android.util.Log;

import org.joda.time.DateTime;

import gov.dc.broker.models.brokeragency.BrokerAgency;
import gov.dc.broker.models.brokeragency.BrokerClient;
import gov.dc.broker.models.employer.Employer;
import gov.dc.broker.models.roster.Roster;
import gov.dc.broker.models.roster.RosterEntry;
import okhttp3.HttpUrl;


public abstract class CoverageConnection {
    private static final String TAG = "CoverageConnection";
    protected final UrlHandler urlHandler;
    protected final IConnectionHandler connectionHandler;
    protected final ServerConfiguration serverConfiguration;
    protected final JsonParser parser;
    private final IDataCache dataCache;

    public CoverageConnection(UrlHandler urlHandler, IConnectionHandler connectionHandler,
                              ServerConfiguration serverConfiguration,
                              JsonParser parser, IDataCache dataCache){

        this.urlHandler = urlHandler;
        this.connectionHandler = connectionHandler;
        this.serverConfiguration = serverConfiguration;
        this.parser = parser;
        this.dataCache = dataCache;
    }

    public abstract void validateUserAndPassword(String accountName, String password, Boolean rememberMe) throws Exception;


    public void checkSecurityAnswer(String securityAnswer) throws Exception {
        return;
    }

    private Employer getEmployer(UrlHandler urlHandler, ServerConfiguration serverConfiguration) throws Exception {
        HttpUrl employerDetailsUrl1 = urlHandler.getEmployerDetailsUrl();
        String response = connectionHandler.get(employerDetailsUrl1, null);
        return parser.parseEmployerDetails(response);
    }

    public void determineUserType() throws Exception {
        try{
            BrokerAgency brokerAgency = getBrokerAgency();
            serverConfiguration.userType = gov.dc.broker.ServerConfiguration.UserType.Broker;
            dataCache.store(brokerAgency, DateTime.now());
            return;
        } catch (Exception e){
            // Eatinng exceptions here is intentional. Failure to get broker object
            // will cause an exception and we then need to try to get an employer.
            Log.d(TAG, "intentionally eating exception caused by failture getting broker agency");
        }

        Employer employer = getEmployer(urlHandler, serverConfiguration);
        dataCache.store(employer, DateTime.now());
        serverConfiguration.userType = gov.dc.broker.ServerConfiguration.UserType.Employer;
    }

    public Events.GetLoginResult.UserType userTypeFromAccountInfo(){
        if (serverConfiguration.userType == null){
            return Events.GetLoginResult.UserType.Unknown;
        }
        switch (serverConfiguration.userType){
            case Broker:
                return Events.GetLoginResult.UserType.Broker;
            case Employer:
                return Events.GetLoginResult.UserType.Employer;
            case Employee:
                return Events.GetLoginResult.UserType.Employee;
        }
        return Events.GetLoginResult.UserType.Unknown;
    }

    public void logout() {
        serverConfiguration.accountName = null;
        serverConfiguration.password = null;
        serverConfiguration.securityAnswer = null;
        serverConfiguration.securityQuestion = null;
        serverConfiguration.rememberMe = false;
    }


    public Employer getEmployer(String employerId, DateTime time) throws CoverageException, Exception {
        Log.d(TAG, "CoverageConnection.getEmployer");
        checkSessionId();

        Employer employer = dataCache.getEmployer(employerId, time);
        if (employer != null){
            return employer;
        }

        BrokerAgency brokerAgency = dataCache.getBrokerAgency(time);
        BrokerClient brokerClient = BrokerUtilities.getBrokerClient(brokerAgency, employerId);
        HttpUrl employerDetailsUrl = urlHandler.getEmployerDetailsUrl(employerId);
        String body = connectionHandler.get(employerDetailsUrl, urlHandler.buildSessionCookies());
        employer = parser.parseEmployerDetails(body);
        dataCache.store(employerId, employer, time);
        return employer;
    }

    protected void checkSessionId() throws Exception, CoverageException {
        Log.d(TAG, "CoverageConnection.checkSessionId");
        if (serverConfiguration.isPasswordEmpty()){
            throw new CoverageException("no password");
        }
        if (serverConfiguration.sessionId == null
                || serverConfiguration.sessionId.length() == 0){
            throw new CoverageException("no sessionId");
        }
        //validateUserAndPassword(serverConfiguration.accountName, serverConfiguration.password, serverConfiguration.rememberMe);
    }

    public Roster getRoster(String employerId) throws Exception {
        Log.d(TAG, "CoverageConnection.getRoster");
        checkSessionId();

        BrokerAgency brokerAgency = getBrokerAgency();
        String rosterUrl = BrokerUtilities.getRosterUrl(brokerAgency, employerId);
        HttpUrl employerRosterUrl = urlHandler.getEmployerRosterUrl(rosterUrl);
        String response = connectionHandler.get(employerRosterUrl, urlHandler.buildSessionCookies());
        return parser.parseRoster(response);
    }

    public BrokerAgency getBrokerAgency() throws Exception, CoverageException {
        checkSessionId();
        BrokerAgency brokerAgency = dataCache.getBrokerAgency(DateTime.now());
        if (brokerAgency == null){
            UrlHandler.GetParameters getParameters = urlHandler.getBrokerAgencyParameters();
            String response = connectionHandler.get(getParameters, null);
            brokerAgency = parser.parseEmployerList(response);
            dataCache.store(brokerAgency, DateTime.now());
        }

        return brokerAgency;
    }

    public Carriers getCarriers() throws Exception {
        HttpUrl carriersUrl = urlHandler.getCarriersUrl();
        String response = connectionHandler.get(carriersUrl, null);
        return parser.parseCarriers(response);
    }

    public RosterEntry getEmployee(String employerId, String employeeId) throws Exception {
        Roster roster = getRoster(employerId);
        DateTime now = DateTime.now();
        return BrokerUtilities.getRosterEntry(roster, employeeId);
    }
}
