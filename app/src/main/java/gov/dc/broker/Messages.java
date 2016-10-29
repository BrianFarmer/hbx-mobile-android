package gov.dc.broker;

public interface Messages {
    void getEmployer(int employerId);
    void getLogin();

    void securityAnswer(String securityAnswer);

    void loginRequest(Events.LoginRequest loginRequest);

    void getEmployerList();
}
