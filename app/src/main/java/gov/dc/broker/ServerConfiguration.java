package gov.dc.broker;

public class ServerConfiguration {


    public boolean isPasswordEmpty() {
        return password == null
                || password.length() == 0;
    }

    public enum UserType {
        Broker,
        Employer,
        Employee,
        Unknown
    }


    public String accountName = null;
    public String password = null;
    public Boolean rememberMe = true;
    public String securityQuestion = "this is the default security question.";
    public String securityAnswer = null;
    public String location;
    public UserType userType = UserType.Unknown;
    public static String sessionId = null;

    public static class HostInfo {
        public String scheme;
        public String host;
        public int port;
    }

    public HostInfo loginInfo;
    public HostInfo dataInfo;
    public HostInfo carrierInfo;

    public String employerListPath;
    public String employerDetailPathForBroker;
    public String employerDetailPathForEmployer;
    public String employerRosterPathForBroker;
    public String employerRosterPathForEmployer;
    public String loginPath;
    public String securityAnswerPath;
    public String carrierPath;

    public String enrollServer;

    private static String defaultEmployersList = "/api/v1/mobile_api/employers_list";
    private static String defaultEmployerDetails = "/api/v1/mobile_api/employer_details";
    private static String defaultEmployerRoster = "/api/v1/mobile_api/employer_details";
}
