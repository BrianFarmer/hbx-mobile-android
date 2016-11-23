package gov.dc.broker;

public interface Messages {
    void getEmployer(int employerId);
    void getLogin();
    void securityAnswer(String securityAnswer);
    void loginRequest(Events.LoginRequest loginRequest);
    void getEmployerList();
    void logoutRequest();
    void release();
    void getRoster(int employerId);
    void getEmployee(int employeeId, int employerId);
    void coverageYearChanged(String year);
}
