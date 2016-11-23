package gov.dc.broker;

import android.widget.ImageView;

import gov.dc.broker.models.brokerclient.BrokerClientDetails;
import gov.dc.broker.models.roster.Roster;

/**
 * Created by plast on 7/26/2016.
 */
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
    }


    static public class GetLogin extends CancelableRequest{
    }

    static public class LoginRequest extends CancelableRequest{
        private CharSequence accountName;
        private CharSequence password;
        private boolean rememberMe;

        public LoginRequest(CharSequence accountName, CharSequence password, Boolean rememberMe){
            this.accountName = accountName;
            this.password = password;
            this.rememberMe = rememberMe;
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
    }

    static public class GetEmployerList extends CancelableRequest {
    }

    static public class GetEmployer extends CancelableRequest {
        private int employerId;

        public GetEmployer(int employerId){
            this.employerId = employerId;
        }
        public int getEmployerId() {
            return employerId;
        }
    }

    static public class GetRoster extends CancelableRequest {
        private int employerId;

        public GetRoster(int employerId){
            this.employerId = employerId;
        }
        public int getEmployerId() {
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

    static public class EmployerList extends ResponseToRequest{
        private gov.dc.broker.EmployerList employerList;

        public EmployerList(int id, gov.dc.broker.EmployerList employerList) {
            super(id);
            this.employerList = employerList;
        }

        public gov.dc.broker.EmployerList getEmployerList() {
            return employerList;
        }

        public void setEmployerList(gov.dc.broker.EmployerList employerList) {
            this.employerList = employerList;
        }
    }

    static public class BrokerClient extends ResponseToRequest{
        private final gov.dc.broker.BrokerClient brokerClient;
        private final BrokerClientDetails brokerClientDetails;

        public BrokerClient(int id, gov.dc.broker.BrokerClient brokerClient,
                            BrokerClientDetails brokerClientDetails) {
            super(id);
            this.brokerClient = brokerClient;
            this.brokerClientDetails = brokerClientDetails;
        }

        public gov.dc.broker.BrokerClient getBrokerClient() {
            return brokerClient;
        }

        public BrokerClientDetails getBrokerClientDetails() {
            return brokerClientDetails;
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
        public LoginRequestResult(int loginResult) {
            this.loginResult = loginResult;
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

        public GetLoginResult(CharSequence accountName, CharSequence password, CharSequence securityAnswer, Boolean rememberMe){
            this.accountName = accountName;
            this.password = password;
            this.securityAnswer = securityAnswer;
            this.rememberMe = rememberMe;
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
        private final int employeeId;
        private final int employerId;

        public GetEmployee(int employeeId, int employerId) {
            this.employeeId = employeeId;
            this.employerId = employerId;
        }

        public int getEmployerId() {
            return employerId;
        }

        public int getEmployeeId(){
            return employeeId;
        }
    }

    static public class Employee {
        private final int employeeId;
        private final int employerId;
        private final gov.dc.broker.models.roster.Employee employee;

        public Employee(int employeeId, int employerId, gov.dc.broker.models.roster.Employee employee) {

            this.employeeId = employeeId;
            this.employerId = employerId;
            this.employee = employee;
        }

        public gov.dc.broker.models.roster.Employee getEmployee() {
            return employee;
        }
    }

    static public class CoverageYear {
        private final String year;

        public CoverageYear(String year) {
            this.year = year;
        }

        public String getYear() {
            return year;
        }
    }
}
