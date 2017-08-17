package org.dchbx.coveragehq;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import org.dchbx.coveragehq.exceptions.BrokerNotFoundException;
import org.dchbx.coveragehq.exceptions.EmployerNotFoundException;
import org.dchbx.coveragehq.exceptions.IndividualNotFoundException;
import org.dchbx.coveragehq.models.Glossary;
import org.dchbx.coveragehq.models.brokeragency.BrokerAgency;
import org.dchbx.coveragehq.models.employer.Employer;
import org.dchbx.coveragehq.models.gitaccounts.GitAccounts;
import org.dchbx.coveragehq.models.planshopping.Plan;
import org.dchbx.coveragehq.models.roster.Enrollment;
import org.dchbx.coveragehq.models.roster.Roster;
import org.dchbx.coveragehq.models.roster.RosterEntry;
import org.dchbx.coveragehq.models.roster.SummaryOfBenefits;
import org.dchbx.coveragehq.models.services.Service;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public abstract class CoverageConnection {
    private static final String TAG = "CoverageConnection";
    private static final String FrontCardFileName = "/frontcard.png";
    private static final String RearCardFileName = "/rearcard.png";


    protected final UrlHandler urlHandler;
    protected final IConnectionHandler connectionHandler;
    protected final ServerConfiguration serverConfiguration;
    protected final JsonParser parser;
    private final IDataCache dataCache;
    protected final IServerConfigurationStorageHandler clearStorageHandler;
    private static Glossary glossary;

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

    public void configureForSignUp(String endPointUrl) throws IOException, CoverageException {
        serverConfiguration.userType = ServerConfiguration.UserType.SignUpIndividual;
        if (endPointUrl != null) {
            serverConfiguration.endpointsPath = endPointUrl;
            getEndPoints();
        }
    }

    public PlanShoppingParameters getPlanShoppingParameters() {
        return serverConfiguration.planShoppingParameters;
    }

    public void updatePlanShopping(Events.UpdatePlanShopping updatePlanShopping) throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, KeyStoreException, NoSuchProviderException, IllegalBlockSizeException {
        serverConfiguration.planShoppingParameters = updatePlanShopping.getPlanShoppingParameters();
        clearStorageHandler.store(serverConfiguration);
    }

    public void updatePlanFilters(double premiumFilter, double deductibleFilter) throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, KeyStoreException, NoSuchProviderException, IllegalBlockSizeException {
        serverConfiguration.premiumFilter = premiumFilter;
        serverConfiguration.deductibleFilter = deductibleFilter;
        clearStorageHandler.store(serverConfiguration);
    }

    public double getPremiumFilter() {
        return serverConfiguration.premiumFilter;
    }

    public double getDeductibleFilter() {
        return serverConfiguration.deductibleFilter;
    }

    public Plan getPlan(String planId) throws IOException, CoverageException {
        for (Plan plan : getPlans()) {
            if (plan.id.compareTo(planId) == 0){
                return plan;
            }
        }
        throw new CoverageException("plan not found");
    }

    public List<Service> getSummaryForPlan(Plan plan) throws IOException, CoverageException {
        DateTime now = DateTime.now();
        List<Service> services = dataCache.getServices(plan.id, now);
        if (services != null){
            return services;
        }
        UrlHandler.GetParameters getParameters = urlHandler.getSummaryParameters(plan.links.servicesRates.substring(1));
        IConnectionHandler.GetResponse response = connectionHandler.get(getParameters);
        services = urlHandler.processSummaryAndBenefits(response);
        dataCache.store(plan.id, services, now);

        return services;
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

    public RosterEntry getIndividual(UrlHandler urlHandler, ServerConfiguration serverConfiguration) throws IOException, CoverageException, IndividualNotFoundException {
        UrlHandler.GetParameters getParameters = urlHandler.getIndividualParameters(null);
        IConnectionHandler.GetResponse response = connectionHandler.get(getParameters);
        RosterEntry individual;
        try{
            individual = urlHandler.processIndividual(response);
        } catch (Exception e){
            Log.e(TAG, "exception parsing json", e);
            throw  e;
        }
        return individual;
    }

    private Employer getEmployer(UrlHandler urlHandler, ServerConfiguration serverConfiguration) throws Exception {
        UrlHandler.GetParameters getParameters = urlHandler.getEmployerDetailsParameters(null);
        IConnectionHandler.GetResponse response = connectionHandler.get(getParameters);
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
        long startMilliSeconds = System.currentTimeMillis();
        try{
            BrokerAgency brokerAgency = getBrokerAgency(DateTime.now());
            serverConfiguration.userType = org.dchbx.coveragehq.ServerConfiguration.UserType.Broker;
            dataCache.store(brokerAgency, DateTime.now());
            long endMilliSeconds = System.currentTimeMillis();
            Log.d(TAG, "Milliseconds to response: " + ((Long)(endMilliSeconds - startMilliSeconds)).toString());
            return serverConfiguration.userType;
        } catch (BrokerNotFoundException e) {
            // Eatinng exceptions here is intentional. Failure to get broker object
            // will cause an exception and we then need to try to get an employer.
            Log.d(TAG, "intentionally eating exception caused by failture getting broker agency");
        }

        try {
            Employer employer = getEmployer(urlHandler, serverConfiguration);
            dataCache.store(employer, DateTime.now());
            serverConfiguration.userType = org.dchbx.coveragehq.ServerConfiguration.UserType.Employer;
            return serverConfiguration.userType;
        } catch (EmployerNotFoundException e){
            // Eatinng exceptions here is intentional. Failure to get broker object
            // will cause an exception and we then need to try to get an employer.
            Log.d(TAG, "intentionally eating exception caused by failture getting employer");
        }
        try {
            RosterEntry individual = getIndividual(urlHandler, serverConfiguration);
            dataCache.store(serverConfiguration.individualEndpoint, individual, DateTime.now());
            serverConfiguration.userType = ServerConfiguration.UserType.Individual;
            return serverConfiguration.userType;
        } catch (IndividualNotFoundException e){
            // Eatinng exceptions here is intentional. Failure to get broker object
            // will cause an exception and we then need to try to get an employer.
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
            case Individual:
                return Events.GetLoginResult.UserType.IndividualEmployee;
            case SignUpIndividual:
                return Events.GetLoginResult.UserType.SignUpIndividual;
        }
        return Events.GetLoginResult.UserType.Unknown;
    }

    public void logout(boolean clearAccount) throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, KeyStoreException, NoSuchProviderException, IllegalBlockSizeException {
        serverConfiguration.sessionId = null;
        serverConfiguration.password = null;
        serverConfiguration.securityAnswer = null;
        serverConfiguration.securityQuestion = null;
        //serverConfiguration.rememberMe = false;
        serverConfiguration.authenticityToken = null;
        serverConfiguration.userType = ServerConfiguration.UserType.Unknown;

        if (clearAccount){
            if (!serverConfiguration.rememberMe) {
                serverConfiguration.accountName = null;
            }
            clearStorageHandler.clear();
            clearStorageHandler.store(serverConfiguration);
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
        IConnectionHandler.GetResponse getReponse = connectionHandler.get(employerDetailsParameters);
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
        IConnectionHandler.GetResponse response = connectionHandler.get(getParameters);
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
        IConnectionHandler.GetResponse response = connectionHandler.get(getParameters);
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
            IConnectionHandler.GetResponse getReponse = connectionHandler.get(getParameters);
            brokerAgency = urlHandler.processBrokerAgency(getReponse);
            dataCache.store(brokerAgency, now);
        }

        return brokerAgency;
    }

    public Carriers getCarriers() throws Exception {
        UrlHandler.GetParameters carriersUrl = urlHandler.getCarriersUrl();
        IConnectionHandler.GetResponse response = connectionHandler.getHackedSSL(carriersUrl);
        return urlHandler.processCarrier(response);
    }

    public RosterEntry getEmployee(String employeeId) throws Exception {
        if (employeeId == null){
            UrlHandler.GetParameters getParameters = urlHandler.getEmployeeDetailsParameters();
            IConnectionHandler.GetResponse getReponse = connectionHandler.get(getParameters);
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

    public ServerConfiguration getLogin() throws Exception {
        if (serverConfiguration.accountName == null){
            clearStorageHandler.read(serverConfiguration);
        }
        getEndPoints();
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
        IConnectionHandler.GetResponse getReponse = connectionHandler.get(employerDetailsParameters);
        employer = urlHandler.processEmployerDetails(getReponse);
        dataCache.store(employer, time);
        return employer;
    }

    public GitAccounts getGitAccounts(String urlRoot) throws Exception {
        clearStorageHandler.read(serverConfiguration);
        return null;
    }

    public void stayLoggedIn() throws Exception {
        return;
    }

    public UserEmployee getUserEmployee(){
        UserEmployee userEmployee = new UserEmployee();
        File frontFile = new File(getFrontCardFileName());
        if (serverConfiguration.haveFrontInsuranceCard && frontFile.exists()){
            userEmployee.insuranceCardFrontFileName = getFrontCardFileName();
        }

        File rearFile = new File(getRearCardFileName());
        if (serverConfiguration.haveRearInsuranceCard && rearFile.exists()){
            userEmployee.insuranceCardRearFileName = getRearCardFileName();
        }

        return userEmployee;
    }

    @NonNull
    private String getRearCardFileName() {
        return BrokerApplication.getBrokerApplication().getFilesDir() + RearCardFileName;
    }

    @NonNull
    private String getFrontCardFileName() {
        return BrokerApplication.getBrokerApplication().getFilesDir() + FrontCardFileName;
    }

    public void moveImageToData(boolean frontOfCard, Uri fileName) throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, KeyStoreException, NoSuchProviderException, IllegalBlockSizeException {
        String filename;
        if (frontOfCard){
            filename = getFrontCardFileName();
        } else {
            filename = getRearCardFileName();
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            final int chunkSize = 1024;  // We'll read in one kB at a time
            byte[] imageData = new byte[chunkSize];
            inputStream = BrokerApplication.getBrokerApplication().getContentResolver().openInputStream(fileName);
            outputStream = new FileOutputStream(filename);
            int bytesRead;
            while ((bytesRead = inputStream.read(imageData)) > 0) {
                outputStream.write(Arrays.copyOfRange(imageData, 0, Math.max(0, bytesRead)));
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }

        if (frontOfCard) {
            serverConfiguration.haveFrontInsuranceCard = true;

            File frontFile = new File(getFrontCardFileName());
            if (frontFile.exists()){
                Log.d(TAG, "front file exists: " + getFrontCardFileName());
            } else {
                Log.d(TAG, "front file is missing: " + getFrontCardFileName());
            }
        } else {
            serverConfiguration.haveRearInsuranceCard = true;
        }

        clearStorageHandler.store(serverConfiguration);
    }

    public void removeInsuraceCardImage(boolean front) throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, KeyStoreException, NoSuchProviderException, IllegalBlockSizeException {
        String fileName;
        if (front){
            fileName = getFrontCardFileName();
            serverConfiguration.haveFrontInsuranceCard = false;
        } else {
            fileName = getRearCardFileName();
            serverConfiguration.haveRearInsuranceCard = false;
        }
        File file = new File(fileName);
        file.delete();
        clearStorageHandler.store(serverConfiguration);
    }

    public static class InsuredAndSummary {
        private final RosterEntry insured;
        private final List<SummaryOfBenefits> summaries;

        public InsuredAndSummary(RosterEntry insured, List<SummaryOfBenefits> summaries){
            this.insured = insured;
            this.summaries = summaries;
        }

        public RosterEntry getInsured() {
            return insured;
        }

        public List<SummaryOfBenefits> getSummaries() {
            return summaries;
        }
    }

    public static class InsuredAndServices {
        private final RosterEntry insured;
        private final List<Service> services;

        public InsuredAndServices(RosterEntry insured, List<Service> services){
            this.insured = insured;
            this.services = services;
        }

        public RosterEntry getInsured() {
            return insured;
        }

        public List<Service> getServices() {
            return services;
        }
    }

    public InsuredAndServices getInsuredAndServices(LocalDate enrollmentDate) throws    Exception {
        Log.d(TAG, "in CoverageConnection.getInsuredAndServices(" + enrollmentDate.toString() + ")");
        RosterEntry insured = getEmployee(null);

        Enrollment enrollment = BrokerUtilities.getEnrollment(insured, enrollmentDate);
        if (enrollment == null){
            Log.d(TAG, "no enrollment found returning null");
            return null;
        }
        List<Service> services = dataCache.getServices(enrollment.health.servicesRatesUrl, DateTime.now());
        if (services == null){
            Log.d(TAG, "no cached services found retrieving");
            UrlHandler.GetParameters servicesParameters = urlHandler.getServicesParameters(enrollment.health.servicesRatesUrl);
            IConnectionHandler.GetResponse getReponse = connectionHandler.get(servicesParameters);
            services = urlHandler.processServices(getReponse);
        }
        Log.d(TAG, "number of services: " + services.size());
        return new InsuredAndServices(insured, services);
    }


    public List<Plan> getPlans() throws IOException, CoverageException {
        UrlHandler.GetParameters getParameters = urlHandler.getPlansParameters();
        IConnectionHandler.GetResponse getResponse = connectionHandler.get(getParameters);
        return urlHandler.processPlans(getResponse);
    }

    public void getEndPoints() throws IOException, CoverageException {
        UrlHandler.GetParameters endPointParameters = urlHandler.getEndpointsParameters();
        if (endPointParameters == null) {
            return;
        }
        IConnectionHandler.GetResponse getResponse = connectionHandler.simpleGet(endPointParameters);
        urlHandler.processEndpoints(getResponse);
    }

    public Glossary getGlossary() throws Exception {
        if (glossary == null) {
            UrlHandler.HttpRequest httpRequest = urlHandler.getGlossaryParameters();
            JsonParser jsonParser = new JsonParser();
            List<Glossary.GlossaryItem> glossaryItems = jsonParser.parseGlossary(connectionHandler.process(httpRequest).getBody());
            HashMap<String, Glossary.GlossaryItem> glossaryItemHashMap = new HashMap<>();
            glossary = new Glossary(glossaryItems, glossaryItemHashMap);
            for (Glossary.GlossaryItem glossaryItem : glossaryItems) {
                glossaryItemHashMap.put(glossaryItem.term.toLowerCase(), glossaryItem);
            }
        }
        return glossary;
    }

    public Glossary.GlossaryItem getGlossaryItem(String term) throws Exception {
        Glossary glossary = getGlossary();
        Glossary.GlossaryItem item = glossary.getItem(term.toLowerCase());
        return item;
    }
}
