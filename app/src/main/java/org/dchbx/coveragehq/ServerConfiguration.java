package org.dchbx.coveragehq;

import org.dchbx.coveragehq.models.gitaccounts.GitAccounts;

public class ServerConfiguration {



    public enum UserType {
        Unknown,
        Broker,
        Employer,
        Employee,
        SignUpIndividual, Individual
    }

    public String accountName = null;
    public String password = null;
    public Boolean rememberMe = true;
    public boolean useFingerprintSensor = true;
    public String securityQuestion = "this is the default security question.";
    public String securityAnswer = null;
    public String encryptedString;
    public String location;
    public UserType userType = UserType.Unknown;
    public String sessionId = null;
    public GitAccounts gitAccounts;

    public static class HostInfo {
        public String scheme;
        public String host;
        public int port;
    }

    public HostInfo loginInfo;
    public HostInfo dataInfo;
    public HostInfo carrierInfo;

    public String employerDetailPath;
    public String brokerDetailPath;
    public String employerRosterPathForBroker;
    public String individualPath;
    public String loginPath;
    public String securityAnswerPath;
    public String carrierPath;
    public String authenticityToken;
    public Boolean haveFrontInsuranceCard;
    public Boolean haveRearInsuranceCard;

    public String stayLoggedInPath = "reset_user_clock";

    //public String enrollServer;

    public static String defaultEmployersList = "/api/v1/mobile_api/employers_list";
    public static String defaultEmployerDetails = "/api/v1/mobile_api/employer_details";
    public static String defaultEmployerRoster = "/api/v1/mobile_api/employer_details";

    public boolean isPasswordEmpty() {
        return password == null
                || password.length() == 0;
    }

    public String getSessionCookies() {
        return "";
    }
}
