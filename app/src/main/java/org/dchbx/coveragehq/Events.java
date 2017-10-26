package org.dchbx.coveragehq;

import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.JsonObject;

import org.dchbx.coveragehq.models.Glossary;
import org.dchbx.coveragehq.models.brokeragency.BrokerAgency;
import org.dchbx.coveragehq.models.employer.Employer;
import org.dchbx.coveragehq.models.fe.Family;
import org.dchbx.coveragehq.models.fe.FinancialAssistanceApplication;
import org.dchbx.coveragehq.models.fe.Schema;
import org.dchbx.coveragehq.models.fe.UqhpDetermination;
import org.dchbx.coveragehq.models.planshopping.Plan;
import org.dchbx.coveragehq.models.ridp.Answers;
import org.dchbx.coveragehq.models.ridp.Questions;
import org.dchbx.coveragehq.models.ridp.VerifyIdentityResponse;
import org.dchbx.coveragehq.models.roster.Roster;
import org.dchbx.coveragehq.models.roster.RosterEntry;
import org.dchbx.coveragehq.models.roster.SummaryOfBenefits;
import org.dchbx.coveragehq.models.services.Service;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.OnActivityResultListener;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.joda.time.LocalDate;

import java.util.List;

import static org.dchbx.coveragehq.Events.GetLoginResult.UserType.SignUpIndividual;

public class Events {
    static int lastCancelableRequestId = 0;

    static public class RequestError {

    }

    static public class CancelRequest {
        private int id;

        public CancelRequest(int id) {
            this.id = id;
        }
    }

    static public class CancelableRequest {
        static private int id = -1;

        public CancelableRequest() {
            id++;
        }

        public int getId() {
            return id;
        }
    }

    static public class LogoutRequest extends CancelableRequest {
        private final boolean clearAccount;

        public LogoutRequest(boolean clearAccount) {
            this.clearAccount = clearAccount;
        }

        public boolean getClearAccount() {
            return clearAccount;
        }
    }


    static public class GetLogin extends CancelableRequest {
        private String TAG = "GetLogin";

        public GetLogin() {
            Log.d(TAG, "Get login ctor");
        }
    }

    static public class LoginRequest extends CancelableRequest {
        private CharSequence accountName;
        private CharSequence password;
        private boolean rememberMe;
        private final boolean useFingerprintSensor;

        public LoginRequest(CharSequence accountName, CharSequence password, Boolean rememberMe, boolean useFingerprintSensor) {
            this.accountName = accountName;
            this.password = password;
            this.rememberMe = rememberMe;
            this.useFingerprintSensor = useFingerprintSensor;
        }

        public CharSequence getAccountName() {
            return accountName;
        }

        public CharSequence getPassword() {
            return password;
        }

        public boolean getRememberMe() {
            return rememberMe;
        }

        public boolean useFingerprintSensor() {
            return useFingerprintSensor;
        }
    }

    static public class GetBrokerAgency extends CancelableRequest {
    }

    static public class GetEmployer extends CancelableRequest {
        private String employerId;

        public GetEmployer(String employerId) {
            this.employerId = employerId;
        }

        public String getEmployerId() {
            return employerId;
        }
    }

    static public class GetRoster extends CancelableRequest {
        private String employerId;

        public GetRoster(String employerId) {
            this.employerId = employerId;
        }

        public String getEmployerId() {
            return employerId;
        }
    }

    static public class GetCarrierImage extends CancelableRequest {
        private String url;
        private ImageView imageView;

        public GetCarrierImage(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }


    }

    static public class GetCarriers extends CancelableRequest {
        // https://dchealthlink.com/shared/json/carriers.json
    }

    static public class ResponseToRequest {
        private int id;

        ResponseToRequest(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    static public class Account extends ResponseToRequest {
        private org.dchbx.coveragehq.Account account;

        Account(int id) {
            super(id);
        }

        public org.dchbx.coveragehq.Account getAccount() {
            return account;
        }

        public void setAccount(org.dchbx.coveragehq.Account account) {
            this.account = account;
        }
    }

    static public class GetBrokerAgencyResult extends ResponseToRequest {

        private final BrokerAgency brokerAgency;

        public GetBrokerAgencyResult(int id, org.dchbx.coveragehq.models.brokeragency.BrokerAgency brokerAgency) {
            super(id);
            this.brokerAgency = brokerAgency;
        }

        public BrokerAgency getBrokerAgency() {
            return brokerAgency;
        }
    }

    static public class BrokerClient extends ResponseToRequest {

        private final org.dchbx.coveragehq.models.brokeragency.BrokerClient brokerClient;
        private final Employer employer;

        public BrokerClient(int id, org.dchbx.coveragehq.models.brokeragency.BrokerClient brokerClient, Employer employer) {
            super(id);
            this.brokerClient = brokerClient;
            this.employer = employer;
        }

        public Employer getEmployer() {
            return employer;
        }

        public org.dchbx.coveragehq.models.brokeragency.BrokerClient getBrokerClient() {
            return brokerClient;
        }
    }

    static public class RosterResult extends ResponseToRequest {
        private final Roster roster;

        public RosterResult(int id, org.dchbx.coveragehq.models.roster.Roster roster) {
            super(id);
            this.roster = roster;
        }

        public Roster getRoster() {
            return roster;
        }
    }

    static public class Carriers extends ResponseToRequest {
        private final org.dchbx.coveragehq.Carriers carriers;

        Carriers(int id, org.dchbx.coveragehq.Carriers carriers) {
            super(id);
            this.carriers = carriers;
        }

        public org.dchbx.coveragehq.Carriers getCarriers() {
            return carriers;
        }
    }

    static public class FingerprintLoginResult {
        static final int Failure = 0;
        static final int Success = 1;
        static final int Error = 2;

        private final int loginResult;
        private final String errorMsg;
        private final ServerConfiguration.UserType userType;

        public FingerprintLoginResult(int loginResult) {
            this.loginResult = loginResult;
            this.errorMsg = null;
            this.userType = null;
        }

        public FingerprintLoginResult(int loginResult, ServerConfiguration.UserType userType) {
            this.loginResult = loginResult;
            this.errorMsg = null;
            this.userType = userType;
        }

        public FingerprintLoginResult(int loginResult, String errorMsg) {
            this.loginResult = loginResult;
            this.errorMsg = errorMsg;
            this.userType = null;
        }

        public int getLoginResult() {
            return loginResult;
        }

        public String getErrorMsg() {
            return errorMsg;
        }
    }

    static public class LoginRequestResult {
        static final int Failure = 0;
        static final int Success = 1;
        static final int Error = 2;

        private final int loginResult;
        private ServerConfiguration.UserType userType;

        public LoginRequestResult(int loginResult, ServerConfiguration.UserType userType) {
            this.loginResult = loginResult;
            this.userType = userType;
        }

        public LoginRequestResult(int loginResult) {
            this.loginResult = loginResult;
        }

        public int getLoginResult() {
            return loginResult;
        }
    }

    static public class SecurityAnswerResult {
        static final int Failure = 0;
        static final int Success = 1;
        static final int Error = 2;

        private final int loginResult;

        public SecurityAnswerResult(int loginResult) {
            this.loginResult = loginResult;
        }

        public int getLoginResult() {
            return loginResult;
        }
    }

    static public class GetLoginResult {
        private final CharSequence errorMessagge;
        private Exception exception;
        private CharSequence accountName;
        private CharSequence password;
        private CharSequence securityAnswer;
        private boolean rememberMe;
        private final boolean useFingerprintSensor;
        private UserType userType;
        private boolean timedout;

        public GetLoginResult(CharSequence errorMessagge, Exception e) {
            this.errorMessagge = errorMessagge;
            this.exception = e;
            useFingerprintSensor = false;
        }


        public GetLoginResult(CharSequence accountName, CharSequence password, CharSequence securityAnswer, Boolean rememberMe, boolean useFingerprintSensor, UserType userType, boolean timedout) {
            this.accountName = accountName;
            this.password = password;
            this.securityAnswer = securityAnswer;
            this.rememberMe = rememberMe;
            this.useFingerprintSensor = useFingerprintSensor;
            this.userType = userType;
            this.timedout = timedout;
            errorMessagge = null;
        }

        public CharSequence getAccountName() {
            return accountName;
        }

        public CharSequence getPassword() {
            return password;
        }

        public boolean getRememberMe() {
            return rememberMe;
        }

        public boolean isLoggedIn() {
            return (getUserType() == SignUpIndividual
                    || (this.accountName != null
                        && this.accountName.length() > 0));
        }

        public CharSequence getSecurityAnswer() {
            return securityAnswer;
        }

        public UserType getUserType() {
            return userType;
        }

        public boolean useFingerprintSensor() {
            return useFingerprintSensor;
        }

        public CharSequence getErrorMessagge() {
            return errorMessagge;
        }

        public Exception getException() {
            return exception;
        }

        public boolean isTimedout() {
            return timedout;
        }

        public enum UserType {
            Broker,
            Employer,
            Employee,
            SignUpIndividual,
            IndividualEmployee,
            Unknown
        }
    }

    static public class LoggedOutResult {
        public LoggedOutResult() {
        }
    }

    static public class Error {
        private final String errorMsg;
        private final String message;

        public Error(String s, String message) {
            errorMsg = s;
            this.message = message;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public String getMessage() {
            return message;
        }
    }

    static public class GetSecurityAnswer {
        public String question;

        public GetSecurityAnswer(String securityQuestion) {
            this.question = securityQuestion;
        }
    }

    static public class SecurityAnswer extends CancelableRequest {
        private final String securityAnswer;

        public SecurityAnswer(String securityAnswer) {
            this.securityAnswer = securityAnswer;
        }

        public String getSecurityAnswer() {
            return securityAnswer;
        }
    }

    static public class GetEmployee extends CancelableRequest {
        private final String employeeId;
        private final String employerId;

        public GetEmployee(String employeeId, String employerId) {
            this.employeeId = employeeId;
            this.employerId = employerId;
        }

        public String getEmployerId() {
            return employerId;
        }

        public String getEmployeeId() {
            return employeeId;
        }
    }

    static public class Employee extends ResponseToRequest {
        private final String employeeId;
        private final String employerId;
        private final org.dchbx.coveragehq.models.roster.RosterEntry employee;

        public Employee(int id, String employeeId, String employerId, org.dchbx.coveragehq.models.roster.RosterEntry employee) {
            super(id);

            this.employeeId = employeeId;
            this.employerId = employerId;
            this.employee = employee;
        }

        public org.dchbx.coveragehq.models.roster.RosterEntry getEmployee() {
            return employee;
        }
    }

    static public class CoverageYear {
        private final LocalDate year;

        public CoverageYear(LocalDate year) {
            this.year = year;
        }

        public LocalDate getYear() {
            return year;
        }
    }

    static public class Finish {
        private final String reason;

        public Finish(String reason) {
            this.reason = reason;
        }
    }

    static public class GetGitAccounts {
        private final String urlRoot;

        public GetGitAccounts(String urlRoot) {
            this.urlRoot = urlRoot;
        }

        public String getUrlRoot() {
            return urlRoot;
        }
    }

    static public class GitAccounts {
        private final org.dchbx.coveragehq.models.gitaccounts.GitAccounts gitAccounts;

        public GitAccounts(org.dchbx.coveragehq.models.gitaccounts.GitAccounts gitAccounts) {
            this.gitAccounts = gitAccounts;
        }

        public org.dchbx.coveragehq.models.gitaccounts.GitAccounts getGitAccounts() {
            return gitAccounts;
        }
    }

    static public class StartSessionTimeout {
    }

    static public class SessionAboutToTimeout {
    }

    static public class SessionTimedOut {
    }

    static public class SessionTimeoutCountdownTick {
        private final int secondsLeft;

        public SessionTimeoutCountdownTick(int secondsLeft) {
            this.secondsLeft = secondsLeft;
        }

        public int getSecondsLeft() {
            return secondsLeft;
        }
    }

    static public class StayLoggedInResult {
        private final boolean success;

        public StayLoggedInResult(boolean success) {
            this.success = success;
        }

        public boolean getSuccess() {
            return success;
        }
    }

    static public class StayLoggedIn {
    }

    static public class GetFingerprintStatus {

        public GetFingerprintStatus() {
        }
    }

    static public class FingerprintStatus {

        private final boolean hardwareDetected;
        private final boolean fingerprintsEnrolled;
        private final boolean error;
        private final String errorMessage;
        private final boolean osSupportsFingerprint;

        public FingerprintStatus(boolean osSupportsFingerprint, boolean hardwareDetected, boolean fingerprintsEnrolled) {
            error = false;
            errorMessage = null;
            this.osSupportsFingerprint = osSupportsFingerprint;
            this.hardwareDetected = hardwareDetected;
            this.fingerprintsEnrolled = fingerprintsEnrolled;
        }

        //
        // this is the error constructor.
        //

        public FingerprintStatus(String s) {
            error = true;
            errorMessage = s;
            hardwareDetected = false;
            fingerprintsEnrolled = false;
            osSupportsFingerprint = false;
        }


        public boolean isHardwareDetected() {
            return hardwareDetected;
        }

        public boolean isFingerprintsEnrolled() {
            return fingerprintsEnrolled;
        }

        public boolean error() {
            return error;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public boolean isOsSupportsFingerprint() {
            return osSupportsFingerprint;
        }
    }

    static public class FingerprintAuthenticationEncryptResult {

        private final String encryptedText;
        private final String helpString;
        private final String errorMessage;


        public FingerprintAuthenticationEncryptResult(String encryptedText, CharSequence helpString, CharSequence errorMessage) {
            this.encryptedText = encryptedText;
            this.helpString = (String) helpString;
            this.errorMessage = (String) errorMessage;
        }

        public String getEncryptedText() {
            return encryptedText;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public CharSequence getHelpString() {
            return helpString;
        }
    }

    static public class FingerprintAuthenticationDecryptResult {

        private final String accountName;
        private final String password;
        private final String helpString;
        private final String errMessage;

        public FingerprintAuthenticationDecryptResult(String accountName, String password, CharSequence helpString, CharSequence errMessage) {
            this.accountName = accountName;
            this.password = password;
            this.helpString = (String) helpString;
            this.errMessage = (String) errMessage;
        }

        public String getAccountName() {
            return accountName;
        }

        public String getPassword() {
            return password;
        }

        public String getHelpString() {
            return helpString;
        }

        public String getErrorMessage() {
            return errMessage;
        }
    }

    static public class AuthenticateFingerprintDecrypt {
        public AuthenticateFingerprintDecrypt() {
        }
    }

    static public class AuthenticateFingerprintEncrypt {
        public AuthenticateFingerprintEncrypt() {
        }
    }

    static public class FingerprintLogin {
    }

    static public class Relogin {
        private final String accountName;
        private final String password;

        public Relogin(String accountName, String password) {
            this.accountName = accountName;
            this.password = password;
        }

        public String getPassword() {
            return password;
        }

        public String getAccountName() {
            return accountName;
        }
    }

    static public class ReloginResult {
        private final ReloginResultEnum reloginResultEnum;
        private final String securityQuestion;

        public ReloginResultEnum getReloginResultEnum() {
            return reloginResultEnum;
        }

        public enum ReloginResultEnum {
            Success,
            Error,
            Failed
        }

        public ReloginResult(ReloginResultEnum reloginResultEnum, String securityQuestion) {
            this.reloginResultEnum = reloginResultEnum;
            this.securityQuestion = securityQuestion;
        }

        public String getSecurityQuestion() {
            return securityQuestion;
        }
    }

    static public class EmployerActivityReady {
        public EmployerActivityReady() {

        }
    }

    static public class EmployeeFragmentUpdate {
        public RosterEntry employee;
        public LocalDate currentEnrollmentStartDate;

        public EmployeeFragmentUpdate(RosterEntry employee, LocalDate currentEnrollmentStartDate){

            this.employee = employee;
            this.currentEnrollmentStartDate = currentEnrollmentStartDate;
        }
    }

    static public class TestTimeoutResult {
        public boolean timedOut;

        public TestTimeoutResult(boolean timedOut) {
            this.timedOut = timedOut;
        }
    }

    static public class TestTimeout {
    }

    static public class GetUserEmployee {
    }

    static public class GetUserEmployeeResults {
        private UserEmployee userEmployee;

        public GetUserEmployeeResults(UserEmployee userEmployee) {
            this.userEmployee = userEmployee;
        }

        public UserEmployee getUserEmployee() {
            return userEmployee;
        }
    }

    public static class CapturePhoto {
        private boolean front;

        public CapturePhoto(boolean front) {
            this.front = front;
        }

        public boolean isFront() {
            return front;
        }
    }

    public static class UpdateInsuranceCard {

        public UpdateInsuranceCard() {
        }
    }

    static public class MoveImageToData {
        private boolean frontOfCard;
        private Uri uri;

        public MoveImageToData(boolean frontOfCard, Uri fileName) {

            this.frontOfCard = frontOfCard;
            this.uri = fileName;
        }

        public boolean isFrontOfCard() {
            return frontOfCard;
        }

        public Uri getUri() {
            return uri;
        }
    }

    static public class MoveImageToDataResult {
        private boolean success;
        private String message;

        public MoveImageToDataResult(boolean success) {
            this.success = success;
        }

        public MoveImageToDataResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    static public class RemoveInsuraceCardImage {
        private boolean front;

        public RemoveInsuraceCardImage(boolean front) {
            this.front = front;
        }

        public boolean isFront() {
            return front;
        }
    }

    public static class RemoveInsuraceCardImageResult {
        private boolean success;

        public RemoveInsuraceCardImageResult(boolean success) {
            this.success = success;
        }

        public boolean isSuccess() {
            return success;
        }
    }

    public static class GetInsuredAndBenefits {
        private LocalDate currentDate;

        public GetInsuredAndBenefits(LocalDate currentDate) {
            this.currentDate = currentDate;
        }

        public LocalDate getCurrentDate() {
            return currentDate;
        }
    }

    public static class GetInsuredAndSummaryOfBenefitsResult {
        private final RosterEntry insured;
        private final List<SummaryOfBenefits> summaryOfBenefitsList;

        public GetInsuredAndSummaryOfBenefitsResult(RosterEntry insured, List<SummaryOfBenefits> summaryOfBenefitsList) {
            this.insured = insured;
            this.summaryOfBenefitsList = summaryOfBenefitsList;
        }

        public RosterEntry getInsured() {
            return insured;
        }

        public List<SummaryOfBenefits> getSummaryOfBenefitsList() {
            return summaryOfBenefitsList;
        }
    }

    public static class GetInsuredAndServices {
        private final LocalDate enrollmentDate;

        public GetInsuredAndServices(LocalDate enrollmentDate) {
            this.enrollmentDate = enrollmentDate;
        }

        public LocalDate getEnrollmentDate() {
            return enrollmentDate;
        }
    }

    public static class GetInsuredAndServicesResult {
        private final RosterEntry insured;
        private final List<Service> servicesList;

        public GetInsuredAndServicesResult(RosterEntry insured, List<Service> servicesList) {
            this.insured = insured;
            this.servicesList = servicesList;
        }

        public RosterEntry getInsured() {
            return insured;
        }

        public List<Service> getServicesList() {
            return servicesList;
        }
    }

    public static class SignUp {
        private final String planShoppingPath;

        public SignUp(String planShoppingPath) {

            this.planShoppingPath = planShoppingPath;
        }

        public String getEndPointUrl() {
            return planShoppingPath;
        }
    }

    public static class SignUpResult {
    }

    public static class GetPlanShopping {
    }

    public static class GetPlanShoppingResult {
        private final PlanShoppingParameters planShoppingParameters;

        public GetPlanShoppingResult(PlanShoppingParameters planShoppingParameters) {
            this.planShoppingParameters = planShoppingParameters;
        }

        public PlanShoppingParameters getPlanShoppingParameters() {
            return planShoppingParameters;
        }
    }

    public static class UpdatePlanShopping {
        private final PlanShoppingParameters planShoppingParameters;

        public UpdatePlanShopping(PlanShoppingParameters planShoppingParameters) {
            this.planShoppingParameters = planShoppingParameters;
        }

        public PlanShoppingParameters getPlanShoppingParameters() {
            return planShoppingParameters;
        }
    }

    public static class ResetPlanShopping {
    }

    public static class ResetPlanShoppingResult {
    }

    public static class UpdatePlanShoppingResult {
    }

    public static class GetPlans extends BackgroundProcess  {
    }

    public static class GetPlansResult {
        private final List<Plan> planList;
        private final double premiumFilter;
        private final double deductibleFilter;

        public GetPlansResult(List<Plan> planList, double premiumFilter, double deductibleFilter) {
            this.planList = planList;
            this.premiumFilter = premiumFilter;
            this.deductibleFilter = deductibleFilter;
        }

        public List<Plan> getPlanList() {
            return planList;
        }

        public double getPremiumFilter() {
            return premiumFilter;
        }

        public double getDeductibleFilter() {
            return deductibleFilter;
        }
    }

    public static class SetPlanFilter {
        private final double premiumFilter;
        private final double deductibleFilter;

        public SetPlanFilter(double premiumFilter, double deductibleFilter) {
            this.premiumFilter = premiumFilter;
            this.deductibleFilter = deductibleFilter;
        }

        public double getPremiumFilter() {
            return premiumFilter;
        }

        public double getDeductibleFilter() {
            return deductibleFilter;
        }
    }

    public static class GetPlan extends BackgroundProcess {
        private final String planId;
        private final boolean getSummaryAndBenefits;

        public GetPlan(String planId, boolean getSummaryAndBenefits) {
            this.planId = planId;
            this.getSummaryAndBenefits = getSummaryAndBenefits;
        }

        public String getPlanId() {
            return planId;
        }

        public boolean isGetSummaryAndBenefits() {
            return getSummaryAndBenefits;
        }
    }

    public static class GetPlanResult {
        private final Plan plan;
        private final List<Service> services;

        public GetPlanResult(Plan plan, List<Service> services) {
            this.plan = plan;
            this.services = services;
        }

        public Plan getPlan() {
            return plan;
        }

        public List<Service> getServices() {
            return services;
        }
    }

    public static class GetAppConfig {
        public GetAppConfig() {
        }
    }

    public static class UpdateAppConfig {
        private final ServiceManager.AppConfig appConfig;

        public UpdateAppConfig(ServiceManager.AppConfig appConfig) {

            this.appConfig = appConfig;
        }

        public ServiceManager.AppConfig getAppConfig() {
            return appConfig;
        }
    }

    public static class GetAppConfigResult {
        private ServiceManager.AppConfig appConfig;

        public GetAppConfigResult(ServiceManager.AppConfig appConfig){
            this.appConfig = appConfig;
        }

        public ServiceManager.AppConfig getAppConfig() {
            return appConfig;
        }
    }

    public static class GetRidpQuestionsAndAnswers {
    }

    public static class GetRidpQuestionsResult {
        private final Questions ridpQuestions;
        private Answers ridpAnswers;

        public GetRidpQuestionsResult(Questions ridpQuestions, Answers answers) {
            this.ridpQuestions = ridpQuestions;
            this.ridpAnswers = answers;
        }

        public Questions getRidpQuestions() {
            return ridpQuestions;
        }

        public Answers getRidpAnswers() {
            return ridpAnswers;
        }
    }

    public static class AccountButtonClicked {
        private final org.dchbx.coveragehq.models.account.Account  account;
        private final Answers answers;
        private final StateManager.AppEvents appEvent;

        public AccountButtonClicked(StateManager.AppEvents appEvent, org.dchbx.coveragehq.models.account.Account account,
                                    Answers answers) {
            this.appEvent = appEvent;
            this.account = account;
            this.answers = answers;
        }

        public org.dchbx.coveragehq.models.account.Account  getAccount() {
            return account;
        }

        public StateManager.AppEvents getAppEvent() {
            return appEvent;
        }

        public Answers getAnswers() {
            return answers;
        }
    }

    public static class ButtonClicked {
        private final StateManager.AppEvents appEvent;

        public ButtonClicked(StateManager.AppEvents appEvent){
            this.appEvent = appEvent;
        }

        public StateManager.AppEvents getAppEvent() {
            return appEvent;
        }
    }

    public static class StateAction {

        public Action getAction() {
            return action;
        }

        public EventParameters getEventParameters() {
            return eventParameters;
        }

        public enum Action {
            Finish,
            LaunchActivity,
            LaunchDialog,
            HideWait,
            ShowWait,
            PopAndLaunchActivity, Pop, PopAndServerErrorMessage, LaunchHome, Dismiss
        }

        private final Action action;
        private int uiActivityId;
        private final EventParameters eventParameters;

        public StateAction(Action action, int uiActivityId, EventParameters eventParameters){
            this.action = action;
            this.uiActivityId = uiActivityId;
            this.eventParameters = eventParameters;
        }

        public StateAction(Action action, EventParameters eventParameters){
            this.action = action;
            this.eventParameters = eventParameters;
        }



        public int getUiActivityId() {
            return uiActivityId;
        }
    }

    public static class GetCreateAccountInfo {
    }

    public static class GetCreateAccountInfoResult {
        private final org.dchbx.coveragehq.models.account.Account account;

        public GetCreateAccountInfoResult(org.dchbx.coveragehq.models.account.Account account) {

            this.account = account;
        }

        public org.dchbx.coveragehq.models.account.Account getAccount() {
            return account;
        }
    }

    public static class UpdateCreateAccountInfo {
        private final org.dchbx.coveragehq.models.account.Account account;

        public UpdateCreateAccountInfo(org.dchbx.coveragehq.models.account.Account account) {
            this.account = account;
        }

        public org.dchbx.coveragehq.models.account.Account getAccount() {
            return account;
        }
    }

    public static class UpdateAnswers {
        private final Answers answers;

        public UpdateAnswers(Answers answers) {
            this.answers = answers;
        }

        public Answers getAnswers() {
            return answers;
        }
    }

    public static class GetRidpQuestions extends BackgroundProcess{
    }

    public static class GetVerificationResponseResponse {
        private final VerifyIdentityResponse verificationResponse;

        public GetVerificationResponseResponse(VerifyIdentityResponse verificationResponse) {

            this.verificationResponse = verificationResponse;
        }

        public VerifyIdentityResponse getVerificationResponse() {
            return verificationResponse;
        }
    }

    public static class GetCurrentActivity {
    }

    public static class GetCurrentActivityResult {
        private final StateManager.UiActivity uiActivity;

        public GetCurrentActivityResult(StateManager.UiActivity uiActivity){
            this.uiActivity = uiActivity;
        }

        public StateManager.UiActivity getUiActivity() {
            return uiActivity;
        }
    }

    public static class AppEvent {
        private final StateManager.AppEvents event;
        private final EventParameters intentParameters;
        private final String s;
        private OnActivityResultListener onActivityResultListener;

        public AppEvent(StateManager.AppEvents event) {
            this.event = event;
            this.s = null;
            intentParameters = null;
        }

        public AppEvent(StateManager.AppEvents event, EventParameters intentParameters) {
            this.event = event;
            this.intentParameters = intentParameters;
            s = null;
        }

        public StateManager.AppEvents getEvent() {
            return event;
        }

        public String getString1() {
            return s;
        }

        public EventParameters getIntentParameters() {
            return intentParameters;
        }
    }

    public static class CreateAccount extends BackgroundProcess {
    }

    public static class VerifyUser extends BackgroundProcess{
    }

    public static class GetGlossary {
    }

    public static class GetGlossaryResponse {
        private final Glossary glossary;

        public GetGlossaryResponse(Glossary glossary) {
            this.glossary = glossary;
        }

        public Glossary getGlossary() {
            return glossary;
        }
    }

    public static class GetGlossaryItemResponse {
        private final Glossary.GlossaryItem glossaryItem;

        public GetGlossaryItemResponse(Glossary.GlossaryItem glossaryItem) {
            this.glossaryItem = glossaryItem;
        }

        public Glossary.GlossaryItem getGlossaryItem() {
            return glossaryItem;
        }
    }

    public static class GetGlossaryItem {
        private final String term;

        public GetGlossaryItem(String term) {
            this.term = term;
        }

        public String getTerm() {
            return term;
        }
    }

    public static class GetFinancialEligibilityJson {
    }

    public static class GetFinancialEligibilityJsonResponse {
        private final Schema schema;

        public GetFinancialEligibilityJsonResponse(Schema schema){
            this.schema = schema;
        }

        public Schema getSchema() {
            return schema;
        }
    }

    public static class GetFinancialAssistanceApplication {
    }

    public static class GetFinancialAssistanceApplicationResponse {

        private final FinancialAssistanceApplication financialAssistanceApplication;

        public GetFinancialAssistanceApplicationResponse(FinancialAssistanceApplication financialAssistanceApplication){
            this.financialAssistanceApplication = financialAssistanceApplication;
        }

        public FinancialAssistanceApplication getFinancialAssistanceApplication() {
            return financialAssistanceApplication;
        }
    }

    public static class GetApplicationPerson {
        private final String eapersonid;

        public GetApplicationPerson(String eapersonid) {
            this.eapersonid = eapersonid;
        }

        public String getEaPersonId() {
            return eapersonid;
        }
    }

    public static class GetApplicationPersonResponse {
        private final JsonObject person;

        public GetApplicationPersonResponse(JsonObject person){
            this.person = person;
        }

        public JsonObject getPerson() {
            return person;
        }
    }

    public static class GetUqhpSchema {
    }

    public static class GetUqhpFamily {
    }

    public static class GetUqhpFamilyResponse {
        private Family family;

        public GetUqhpFamilyResponse(Family family){
            this.family = family;
        }

        public Family getFamily() {
            return family;
        }
    }

    public static class SaveUqhpFamily {
        private Family family;

        public SaveUqhpFamily(Family family){
            this.family = family;
        }

        public Family getFamily() {
            return family;
        }
    }

    public static class SaveUqhpFamilyResponse {
    }

    public static class SendHavenApplication extends BackgroundProcess {
    }

    public static class GetUqhpDetermination {
    }

    public static class GetUqhpDeterminationResponse {
        private final UqhpDetermination uqhpDetermination;

        public GetUqhpDeterminationResponse(UqhpDetermination uqhpDetermination) {
            this.uqhpDetermination = uqhpDetermination;
        }

        public UqhpDetermination getUqhpDetermination() {
            return uqhpDetermination;
        }
    }

    public static class BackgroundProcess {
        EventParameters eventParameters;

        public BackgroundProcess(){
        }

        public void setEventParameters(EventParameters eventParameters){
            this.eventParameters = eventParameters;
        }

        public EventParameters getEventParameters(){
            return eventParameters;
        }
    }

    public static class ResumeApplication extends BackgroundProcess {
    }

    public static class CheckOpenEnrollment extends BackgroundProcess {
    }

    static public class IvlLoginRequest extends BackgroundProcess {
    }

    public static class GetEffectiveDate extends BackgroundProcess {
    }

    static public class ClearPIIRequest extends BackgroundProcess {
    }

    public static class GetUqhpDeterminationFromServer extends BackgroundProcess{
    }

    public static class GetPlanShoppingFamily {
    }

    public static class GetPlanShoppingFamilyResponse {
    }

    public class GetPlansFromServer {
    }

    public static class SubmitApplication extends BackgroundProcess{
    }

    public static class RidpCheckOverride extends BackgroundProcess {
    }

    public static class ServerError {
        private final EventParameters intentParameters;

        public ServerError(EventParameters intentParameters) {
            this.intentParameters = intentParameters;
        }

        public EventParameters getIntentParameters() {
            return intentParameters;
        }
    }
}
