package gov.dc.broker;

import android.util.Log;

import org.joda.time.DateTime;

import gov.dc.broker.models.brokeragency.BrokerAgency;
import gov.dc.broker.models.brokeragency.BrokerClient;
import gov.dc.broker.models.employer.Employer;
import gov.dc.broker.models.gitaccounts.GitAccounts;
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
    protected final IServerConfigurationStorageHandler storageHandler;

    public CoverageConnection(UrlHandler urlHandler, IConnectionHandler connectionHandler,
                              ServerConfiguration serverConfiguration,
                              JsonParser parser, IDataCache dataCache,
                              IServerConfigurationStorageHandler storageHandler){

        this.urlHandler = urlHandler;
        this.connectionHandler = connectionHandler;
        this.serverConfiguration = serverConfiguration;
        this.parser = parser;
        this.dataCache = dataCache;
        this.storageHandler = storageHandler;
    }

    public abstract boolean validateUserAndPassword(String accountName, String password, Boolean rememberMe) throws Exception;


    public void checkSecurityAnswer(String securityAnswer) throws Exception {
        storageHandler.store(serverConfiguration);
        return;
    }

    private Employer getEmployer(UrlHandler urlHandler, ServerConfiguration serverConfiguration) throws Exception {
        UrlHandler.GetParameters getParameters = urlHandler.getEmployerDetailsParameters();
        IConnectionHandler.GetReponse response = connectionHandler.get(getParameters);
        return urlHandler.processEmployerDetails(response);
    }

    public ServerConfiguration.UserType determineUserType() throws Exception {
        try{
            BrokerAgency brokerAgency = getBrokerAgency(DateTime.now());
            serverConfiguration.userType = gov.dc.broker.ServerConfiguration.UserType.Broker;
            dataCache.store(brokerAgency, DateTime.now());
            return serverConfiguration.userType;
        } catch (Exception e){
            // Eatinng exceptions here is intentional. Failure to get broker object
            // will cause an exception and we then need to try to get an employer.
            Log.d(TAG, "intentionally eating exception caused by failture getting broker agency");
        }

        Employer employer = getEmployer(urlHandler, serverConfiguration);
        dataCache.store(employer, DateTime.now());
        serverConfiguration.userType = gov.dc.broker.ServerConfiguration.UserType.Employer;
        return serverConfiguration.userType;
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
        Log.d(TAG, "CoverageConnection.getEmployer by id");
        checkSessionId();

        Employer employer = dataCache.getEmployer(employerId, time);
        if (employer != null){
            return employer;
        }

        BrokerAgency brokerAgency = dataCache.getBrokerAgency(time);
        BrokerClient brokerClient = BrokerUtilities.getBrokerClient(brokerAgency, employerId);
        UrlHandler.GetParameters employerDetailsParameters = urlHandler.getEmployerDetailsParameters(employerId);
        IConnectionHandler.GetReponse getReponse = connectionHandler.get(employerDetailsParameters);
        employer = urlHandler.processEmployerDetails(getReponse);
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

    public Roster getRoster(DateTime now) throws Exception {
        Log.d(TAG, "CoverageConnection.getRoster");
        checkSessionId();

        Roster roster = dataCache.getRoster(now);
        if (roster != null){
            return roster;
        }
        UrlHandler.GetParameters getParameters = urlHandler.getEmployerRosterParameters();
        IConnectionHandler.GetReponse response = connectionHandler.get(getParameters);
        roster = urlHandler.processRoster(response);
        dataCache.store(roster, now);
        return roster;
    }

    public Roster getRoster(String employerId, DateTime now) throws Exception {
        Log.d(TAG, "CoverageConnection.getRoster");
        checkSessionId();

        BrokerAgency brokerAgency = getBrokerAgency(now);
        String rosterUrl = BrokerUtilities.getRosterUrl(brokerAgency, employerId);
        Roster roster = dataCache.getRoster(rosterUrl, now);
        if (roster != null){
            return roster;
        }
        UrlHandler.GetParameters getParameters = urlHandler.getEmployerRosterParameters(rosterUrl);
        IConnectionHandler.GetReponse response = connectionHandler.get(getParameters);
        roster = urlHandler.processRoster(response);
        dataCache.store(rosterUrl, roster, now);
        return roster;
    }

    public BrokerAgency getBrokerAgency(DateTime now) throws
            Exception, CoverageException {
        checkSessionId();
        BrokerAgency brokerAgency = dataCache.getBrokerAgency(DateTime.now());
        if (brokerAgency == null){
            UrlHandler.GetParameters getParameters;
            try{
                getParameters = urlHandler.getBrokerAgencyParameters();
            } catch (Exception e){
                Log.e(TAG, "gettting parameters", e);
                throw e;
            }
            IConnectionHandler.GetReponse getReponse = connectionHandler.get(getParameters);
            brokerAgency = urlHandler.processBrokerAgency(getReponse);
            dataCache.store(brokerAgency, now);
        }

        return brokerAgency;
    }

    public Carriers getCarriers() throws Exception {
        HttpUrl carriersUrl = urlHandler.getCarriersUrl();
        String response = connectionHandler.get(carriersUrl, null);
        return parser.parseCarriers(response);
    }

    public RosterEntry getEmployee(String employeeId) throws Exception {
        Roster roster = getRoster(DateTime.now());
        DateTime now = DateTime.now();
        return BrokerUtilities.getRosterEntry(roster, employeeId);
    }

    public RosterEntry getEmployee(String employerId, String employeeId) throws Exception {
        Roster roster = getRoster(employerId, DateTime.now());
        DateTime now = DateTime.now();
        return BrokerUtilities.getRosterEntry(roster, employeeId);
    }

    public ServerConfiguration getLogin(){
        storageHandler.read(serverConfiguration);
        return serverConfiguration;
    }

    public Employer getDefaultEmployer(DateTime time) throws Exception {
        Log.d(TAG, "CoverageConnection.getEmployer by id");
        checkSessionId();

        Employer employer = dataCache.getEmployer(time);
        if (employer != null){
            return employer;
        }

        BrokerAgency brokerAgency = dataCache.getBrokerAgency(time);
        UrlHandler.GetParameters employerDetailsParameters = urlHandler.getEmployerDetailsParameters();
        IConnectionHandler.GetReponse getReponse = connectionHandler.get(employerDetailsParameters);
        employer = urlHandler.processEmployerDetails(getReponse);
        dataCache.store(employer, time);
        return employer;
    }

    public GitAccounts getGitAccounts(String urlRoot) throws Exception {
        return null;
    }
}
