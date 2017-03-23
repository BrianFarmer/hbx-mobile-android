package org.dchbx.coveragehq;

import android.util.Log;

import org.dchbx.coveragehq.models.brokeragency.BrokerAgency;
import org.dchbx.coveragehq.models.employer.Employer;
import org.dchbx.coveragehq.models.gitaccounts.GitAccounts;
import org.dchbx.coveragehq.models.roster.Roster;
import org.dchbx.coveragehq.models.roster.RosterEntry;
import org.joda.time.DateTime;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public abstract class CoverageConnection {
    private static final String TAG = "CoverageConnection";
    protected final UrlHandler urlHandler;
    protected final IConnectionHandler connectionHandler;
    protected final ServerConfiguration serverConfiguration;
    protected final JsonParser parser;
    private final IDataCache dataCache;
    protected final IServerConfigurationStorageHandler clearStorageHandler;

    public CoverageConnection(UrlHandler urlHandler, IConnectionHandler connectionHandler,
                              ServerConfiguration serverConfiguration,
                              JsonParser parser, IDataCache dataCache,
                              IServerConfigurationStorageHandler clearStorageHandler){

        this.urlHandler = urlHandler;
        this.connectionHandler = connectionHandler;
        this.serverConfiguration = serverConfiguration;
        this.parser = parser;
        this.dataCache = dataCache;
        this.clearStorageHandler = clearStorageHandler;
    }

    public LoginResult loginAfterFingerprintAuthenticated() throws Exception{
        clearStorageHandler.read(serverConfiguration);
        return validateUserAndPassword(serverConfiguration.accountName, serverConfiguration.password, true, true);
    }

    public void saveLoginInfo(boolean useEncrypted) throws BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidKeyException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException {
        clearStorageHandler.store(serverConfiguration);
    }

    enum LoginResult {
        Error,
        Failure,
        NeedSecurityQuestion,
        Success
    }

    public abstract LoginResult validateUserAndPassword(String accountName, String password, Boolean rememberMe, boolean useFingerprintSensor) throws Exception;
    public abstract LoginResult revalidateUserAndPassword() throws Exception;

    public void checkSecurityAnswer(String securityAnswer) throws Exception {
        clearStorageHandler.store(serverConfiguration);
    }

    private Employer getEmployer(UrlHandler urlHandler, ServerConfiguration serverConfiguration) throws Exception {
        UrlHandler.GetParameters getParameters = urlHandler.getEmployerDetailsParameters(null);
        IConnectionHandler.GetReponse response = connectionHandler.get(getParameters);
        Employer employer;
        try{
            employer = urlHandler.processEmployerDetails(response);
        } catch (Exception e){
            Log.e(TAG, "exception parsing json", e);
            throw  e;
        }
        return employer;
    }

    public ServerConfiguration.UserType determineUserType() throws Exception {
        try{
            BrokerAgency brokerAgency = getBrokerAgency(DateTime.now());
            serverConfiguration.userType = org.dchbx.coveragehq.ServerConfiguration.UserType.Broker;
            dataCache.store(brokerAgency, DateTime.now());
            return serverConfiguration.userType;
        } catch (Exception e){
            // Eatinng exceptions here is intentional. Failure to get broker object
            // will cause an exception and we then need to try to get an employer.
                Log.d(TAG, "intentionally eating exception caused by failture getting broker agency");
        }

        try {
            Employer employer = getEmployer(urlHandler, serverConfiguration);
            dataCache.store(employer, DateTime.now());
            serverConfiguration.userType = org.dchbx.coveragehq.ServerConfiguration.UserType.Employer;
            return serverConfiguration.userType;
        } catch (CoverageException e){
            return ServerConfiguration.UserType.Unknown;
        }
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

    public void logout(boolean clearAccount) {
        serverConfiguration.sessionId = null;
        serverConfiguration.password = null;
        serverConfiguration.securityAnswer = null;
        serverConfiguration.securityQuestion = null;
        serverConfiguration.rememberMe = false;
        serverConfiguration.authenticityToken = null;
        serverConfiguration.userType = ServerConfiguration.UserType.Unknown;

        if (clearAccount){
            serverConfiguration.accountName = null;
            clearStorageHandler.clear();
        }
    }


    public Employer getEmployer(String employerId, DateTime time) throws CoverageException, Exception {
        Log.d(TAG, "CoverageConnection.getEmployer by id");
        checkSessionId();

        Employer employer = dataCache.getEmployer(employerId, time);
        if (employer != null){
            return employer;
        }

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
        if (rosterUrl == null){
            // this is most likely valid, just means the url in the broker agency was null.
            return null;
        }
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
        UrlHandler.GetParameters carriersUrl = urlHandler.getCarriersUrl();
        IConnectionHandler.GetReponse response = connectionHandler.getHackedSSL(carriersUrl);
        return urlHandler.processCarrier(response);
    }

    public RosterEntry getEmployee(String employeeId) throws Exception {
        if (employeeId == null){
            UrlHandler.GetParameters getParameters = urlHandler.getEmployeeDetailsParameters();
            IConnectionHandler.GetReponse getReponse = connectionHandler.get(getParameters);
            return urlHandler.processEmployeeDetails(getReponse);

        }
        Roster roster = getRoster(DateTime.now());
        DateTime now = DateTime.now();
        return BrokerUtilities.getRosterEntry(roster, employeeId);
    }

    public RosterEntry getEmployee(String employerId, String employeeId) throws Exception {
        Roster roster = getRoster(employerId, DateTime.now());
        DateTime now = DateTime.now();
        return BrokerUtilities.getRosterEntry(roster, employeeId);
    }

    public ServerConfiguration getLogin() throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, KeyStoreException, IllegalBlockSizeException {
        if (serverConfiguration.accountName == null){
            clearStorageHandler.read(serverConfiguration);
        }
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
        UrlHandler.GetParameters employerDetailsParameters = urlHandler.getEmployerDetailsParameters(null);
        IConnectionHandler.GetReponse getReponse = connectionHandler.get(employerDetailsParameters);
        employer = urlHandler.processEmployerDetails(getReponse);
        dataCache.store(employer, time);
        return employer;
    }

    public GitAccounts getGitAccounts(String urlRoot) throws Exception {
        return null;
    }

    public void stayLoggedIn() throws Exception {
        return;
    }

}
