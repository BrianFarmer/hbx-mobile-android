package gov.dc.broker;

import android.util.Log;
import android.widget.ImageView;

import org.joda.time.LocalDate;

import gov.dc.broker.models.brokeragency.BrokerAgency;
import gov.dc.broker.models.employer.Employer;
import gov.dc.broker.models.roster.Roster;

public class Events {
    static int lastCancelableRequestId = 0;

    static public class RequestError {

    }

    static public class CancelRequest {
        private int id;

        public CancelRequest(int id){
            this.id = id;
        }
    }

    static public class CancelableRequest {
        private int id;

        public int getId (){
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


    static public class GetLogin extends CancelableRequest{
        private String TAG = "GetLogin";
        public GetLogin(){
            Log.d(TAG, "Get login ctor");
        }
    }

    static public class LoginRequest extends CancelableRequest{
        private CharSequence accountName;
        private CharSequence password;
        private boolean rememberMe;
        private final boolean useFingerprintSensor;

        public LoginRequest(CharSequence accountName, CharSequence password, Boolean rememberMe, boolean useFingerprintSensor){
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

        public GetEmployer(String employerId){
            this.employerId = employerId;
        }
        public String getEmployerId() {
            return employerId;
        }
    }

    static public class GetRoster extends CancelableRequest {
        private String employerId;

        public GetRoster(String employerId){
            this.employerId = employerId;
        }
        public String getEmployerId() {
            return employerId;
        }
    }

    static public class GetCarrierImage extends CancelableRequest {
        private String url;
        private ImageView imageView;

        public GetCarrierImage(String url, ImageView imageView){
            this.url = url;
            this.imageView = imageView;
        }


    }

    static public class GetCarriers extends CancelableRequest{
        // https://dchealthlink.com/shared/json/carriers.json
    }

    static public class ResponseToRequest{
        private int id;

        ResponseToRequest(int id){
            this.id = id;
        }

        public int getId(){
            return id;
        }
    }

    static public class Account extends ResponseToRequest {
        private gov.dc.broker.Account account;

        Account(int id) {
            super(id);
        }

        public gov.dc.broker.Account getAccount() {
            return account;
        }

        public void setAccount(gov.dc.broker.Account account) {
            this.account = account;
        }
    }

    static public class GetBrokerAgencyResult extends ResponseToRequest{

        private final BrokerAgency brokerAgency;

        public GetBrokerAgencyResult(int id, gov.dc.broker.models.brokeragency.BrokerAgency brokerAgency) {
            super(id);
            this.brokerAgency = brokerAgency;
        }

        public BrokerAgency getBrokerAgency() {
            return brokerAgency;
        }
    }

    static public class BrokerClient extends ResponseToRequest{

        private final gov.dc.broker.models.brokeragency.BrokerClient brokerClient;
        private final Employer employer;

        public BrokerClient(int id, gov.dc.broker.models.brokeragency.BrokerClient brokerClient, Employer employer) {
            super(id);
            this.brokerClient = brokerClient;
            this.employer = employer;
        }

        public Employer getEmployer() {
            return employer;
        }

        public gov.dc.broker.models.brokeragency.BrokerClient getBrokerClient() {
            return brokerClient;
        }
    }

    static public class RosterResult extends ResponseToRequest{
        private final Roster roster;

        public RosterResult(int id, gov.dc.broker.models.roster.Roster roster) {
            super(id);
            this.roster = roster;
        }

        public Roster getRoster() {
            return roster;
        }
    }

    static public class Carriers extends ResponseToRequest {
        private final gov.dc.broker.Carriers carriers;

        Carriers(int id, gov.dc.broker.Carriers carriers) {
            super(id);
            this.carriers = carriers;
        }

        public gov.dc.broker.Carriers getCarriers() {
            return carriers;
        }
    }

    static public class LoginRequestResult {
        static final int Failure = 0;
        static final int Success = 1;
        static final int Error = 2;

        private final int loginResult;
        private final ServerConfiguration.UserType userType;

        public LoginRequestResult(int loginResult, ServerConfiguration.UserType userType) {
            this.loginResult = loginResult;
            this.userType = userType;
        }

        public int getLoginResult(){
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

        public int getLoginResult(){
            return loginResult;
        }
    }

    static public class GetLoginResult {
        private CharSequence accountName;
        private CharSequence password;
        private CharSequence securityAnswer;
        private boolean rememberMe;
        private final boolean useFingerprintSensor;
        private UserType userType;

        public GetLoginResult(CharSequence accountName, CharSequence password, CharSequence securityAnswer, Boolean rememberMe, boolean useFingerprintSensor, UserType userType){
            this.accountName = accountName;
            this.password = password;
            this.securityAnswer = securityAnswer;
            this.rememberMe = rememberMe;
            this.useFingerprintSensor = useFingerprintSensor;
            this.userType = userType;
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
            return (this.accountName != null
            && this.accountName.length() > 0);
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

        public enum UserType {
            Broker,
            Employer,
            Employee,
            Unknown
        }
    }

    static public class LoggedOutResult {
        public LoggedOutResult() {
        }
    }

    static public class Error {
        private final String errorMsg;

        public Error(String s) {
            errorMsg = s;
        }

        public String getErrorMsg() {
            return errorMsg;
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

        public String getEmployeeId(){
            return employeeId;
        }
    }

    static public class Employee extends ResponseToRequest {
        private final String employeeId;
        private final String employerId;
        private final gov.dc.broker.models.roster.RosterEntry employee;

        public Employee(int id, String employeeId, String employerId, gov.dc.broker.models.roster.RosterEntry employee) {
            super(id);

            this.employeeId = employeeId;
            this.employerId = employerId;
            this.employee = employee;
        }

        public gov.dc.broker.models.roster.RosterEntry getEmployee() {
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
        private final gov.dc.broker.models.gitaccounts.GitAccounts gitAccounts;

        public GitAccounts(gov.dc.broker.models.gitaccounts.GitAccounts gitAccounts) {
            this.gitAccounts = gitAccounts;
        }

        public gov.dc.broker.models.gitaccounts.GitAccounts getGitAccounts() {
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

        public SessionTimeoutCountdownTick(int secondsLeft){
            this.secondsLeft = secondsLeft;
        }

        public int getSecondsLeft() {
            return secondsLeft;
        }
    }

    static public class StayLoggedInResult {
        private final boolean success;

        public StayLoggedInResult(boolean success){
            this.success = success;
        }

        public boolean getSuccess() {
            return success;
        }
    }

    static public class StayLoggedIn{}

    static public class GetFingerprintStatus {
        private final boolean watching;

        public GetFingerprintStatus(boolean watching){
            this.watching = watching;
        }

        public boolean isWatching() {
            return watching;
        }
    }

    static public class FingerprintStatus{

        public enum Messages{
            None,
            AuthenticationError,
            AuthenticationHelp,
            AuthenticationFailed,
            AuthenticationSucceeded
        }

        private final boolean hardwareDetected;
        private final boolean fingerprintsEnrolled;
        private final boolean keyguardSecure;
        private final Messages message;
        private final boolean error;
        private final String errorMessage;

        public FingerprintStatus(boolean hardwareDetected, boolean fingerprintsEnrolled, boolean keyguardSecure){
            error = false;
            errorMessage = null;
            this.hardwareDetected = hardwareDetected;
            this.fingerprintsEnrolled = fingerprintsEnrolled;
            this.keyguardSecure = keyguardSecure;
            this.message = Messages.None;
        }

        //
        // this is the error constructor.
        //

        public FingerprintStatus(String s) {
            error = true;
            errorMessage = s;
            hardwareDetected = false;
            fingerprintsEnrolled = false;
            keyguardSecure = false;
            this.message = Messages.None;
        }

        public FingerprintStatus(Messages message){
            error = false;
            errorMessage = null;
            hardwareDetected = true;
            fingerprintsEnrolled = true;
            keyguardSecure = true;
            this.message = message;
        }

        public boolean isHardwareDetected() {
            return hardwareDetected;
        }

        public Messages getMessage() {
            return message;
        }

        public boolean isFingerprintsEnrolled() {
            return fingerprintsEnrolled;
        }

        public boolean isKeyguardSecure() {
            return keyguardSecure;
        }

        public boolean error() {
            return error;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    static public class FingerprintAuthenticationUpdate {
        private final FingerprintStatus.Messages message;
        private String securityQuestion;

        public FingerprintAuthenticationUpdate(FingerprintStatus.Messages message, String securityQuestion) {
            this.message = message;
        }

        public FingerprintStatus.Messages getMessage() {
            return message;
        }

        public String getSecurityQuestion() {
            return securityQuestion;
        }
    }

    static public class AuthenticateFingerprint {
        private final boolean autoLogin;

        public AuthenticateFingerprint(boolean autoLogin) {
            this.autoLogin = autoLogin;
        }
    }

    static public class FingerprintLogin {
    }

    static public class Relogin {
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

        public ReloginResult(ReloginResultEnum reloginResultEnum, String securityQuestion){
            this.reloginResultEnum = reloginResultEnum;
            this.securityQuestion = securityQuestion;
        }

        public String getSecurityQuestion() {
            return securityQuestion;
        }
    }
}
