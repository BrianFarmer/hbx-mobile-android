package gov.dc.broker;

import org.joda.time.LocalDate;

public interface Messages {
    void getEmployer();
    void getEmployer(String employerId);
    void getLogin();
    void securityAnswer(String securityAnswer);
    void loginRequest(Events.LoginRequest loginRequest);
    void getEmployerList();
    void logoutRequest();
    void release();
    void getRoster();
    void getRoster(String employerId);
    void getEmployee(String employeeId, String employerId);
    void coverageYearChanged(LocalDate coverageYear);
}
