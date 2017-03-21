package org.dchbx.coveragehq;

import android.util.Log;
import android.widget.ImageView;

import org.dchbx.coveragehq.models.roster.RosterEntry;
import org.joda.time.LocalDate;

import org.dchbx.coveragehq.models.brokeragency.BrokerAgency;
import org.dchbx.coveragehq.models.employer.Employer;
import org.dchbx.coveragehq.models.roster.Roster;

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
        static private int id =- 1;

        public CancelableRequest(){
            id ++;
        }

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

    static public class GetBrokerAgencyResult extends ResponseToRequest{

        private final BrokerAgency brokerAgency;

        public GetBrokerAgencyResult(int id, org.dchbx.coveragehq.models.brokeragency.BrokerAgency brokerAgency) {
            super(id);
            this.brokerAgency = brokerAgency;
        }

        public BrokerAgency getBrokerAgency() {
            return brokerAgency;
        }
    }

    static public class BrokerClient extends ResponseToRequest{

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

    static public class RosterResult extends ResponseToRequest{
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

        public int getLoginResult(){
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
        private final CharSequence errorMessagge;
        private Exception exception;
        private CharSequence accountName;
        private CharSequence password;
        private CharSequence securityAnswer;
        private boolean rememberMe;
        private final boolean useFingerprintSensor;
        private UserType userType;

        public GetLoginResult(CharSequence errorMessagge, Exception e){
            this.errorMessagge = errorMessagge;
            this.exception = e;
            useFingerprintSensor = false;
        }


        public GetLoginResult(CharSequence accountName, CharSequence password, CharSequence securityAnswer, Boolean rememberMe, boolean useFingerprintSensor, UserType userType){
            this.accountName = accountName;
            this.password = password;
            this.securityAnswer = securityAnswer;
            this.rememberMe = rememberMe;
            this.useFingerprintSensor = useFingerprintSensor;
            this.userType = userType;
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

        public CharSequence getErrorMessagge() {
            return errorMessagge;
        }

        public Exception getException() {
            return exception;
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

        public String getEmployeeId(){
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

        public GetFingerprintStatus(){
        }
    }

    static public class FingerprintStatus{

        private final boolean hardwareDetected;
        private final boolean fingerprintsEnrolled;
        private final boolean error;
        private final String errorMessage;
        private final boolean osSupportsFingerprint;

        public FingerprintStatus(boolean osSupportsFingerprint, boolean hardwareDetected, boolean fingerprintsEnrolled){
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

        public ReloginResult(ReloginResultEnum reloginResultEnum, String securityQuestion){
            this.reloginResultEnum = reloginResultEnum;
            this.securityQuestion = securityQuestion;
        }

        public String getSecurityQuestion() {
            return securityQuestion;
        }
    }

    static public class EmployerActivityReady {
    }

    static public class EmployeeFragmentUpdate {
        public RosterEntry employee;
        public LocalDate currentEnrollmentStartDate;
    }
}