package gov.dc.broker;

import org.joda.time.LocalDate;

public interface Messages {
    void getEmployer();
    void getEmployer(String employerId);
    void getLogin();
    void securityAnswer(String securityAnswer);
    void loginRequest(Events.LoginRequest loginRequest);
    void getBrokerAgency();
    void logoutRequest();
    void logoutRequest(boolean clearAccount);
    void release();
    void getRoster();
    void getRoster(String employerId);
    void getEmployee(String employeeId, String employerId);
    void coverageYearChanged(LocalDate coverageYear);
    void getGitAccounts(String s);
    void startSessioniTimeoutCountdown();
    void sessionTimeoutCountdownTick(int secondsLeft);
    void sessionAboutToTimeout();
    void sessionTimedout();
    void stayLoggedIn();
    void getFingerprintStatus(boolean watching);
    void authenticateFingerprint(boolean autoLogin);
    void relogin();
}
