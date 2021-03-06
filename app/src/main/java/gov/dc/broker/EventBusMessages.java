package gov.dc.broker;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.LocalDate;

/**
 * Created by plast on 10/27/2016.
 */

public class EventBusMessages implements Messages {
    private Object object;
    private EventBus eventBus;

    public EventBusMessages(Object object){
        this.object = object;
        eventBus = EventBus.getDefault();
        eventBus.register(object);
    }

    @Override
    public void getEmployer(String employerId) {
        eventBus.post(new Events.GetEmployer(employerId));
    }

    @Override
    public void getEmployer() {
        eventBus.post(new Events.GetEmployer(null));
    }

    @Override
    public void getLogin() {
        eventBus.post(new Events.GetLogin());
    }

    @Override
    public void securityAnswer(String securityAnswer) {
        eventBus.post(new Events.SecurityAnswer(securityAnswer));
    }

    @Override
    public void loginRequest(Events.LoginRequest loginRequest) {
        eventBus.post(loginRequest);
    }

    @Override
    public void getBrokerAgency() {
        eventBus.post(new Events.GetBrokerAgency());
    }

    @Override
    public void logoutRequest() {
        logoutRequest(true);
    }

    @Override
    public void logoutRequest(boolean clearAccount) {
        eventBus.post(new Events.LogoutRequest(clearAccount));
    }

    @Override
    public void release() {
        eventBus.unregister(object);
        eventBus = null;
    }

    @Override
    public void getRoster(String employerId) {
        eventBus.post(new Events.GetRoster(employerId));
    }

    @Override
    public void getRoster() {
        eventBus.post(new Events.GetRoster(null));
    }

    @Override
    public void getEmployee(String employeeId, String employerId) {
        eventBus.post(new Events.GetEmployee(employeeId, employerId));
    }

    @Override
    public void coverageYearChanged(LocalDate year) {
        eventBus.post(new Events.CoverageYear(year));
    }

    @Override
    public void getGitAccounts(String s) {
        eventBus.post(new Events.GetGitAccounts(s));
    }

    @Override
    public void startSessioniTimeoutCountdown() {
        eventBus.post(new Events.StartSessionTimeout());
    }

    @Override
    public void sessionTimeoutCountdownTick(int secondsLeft) {
        eventBus.post(new Events.SessionTimeoutCountdownTick(secondsLeft));
    }

    @Override
    public void sessionAboutToTimeout() {
        eventBus.post(new Events.SessionAboutToTimeout());
    }

    @Override
    public void sessionTimedout() {
        eventBus.post(new Events.SessionTimedOut());
    }

    @Override
    public void stayLoggedIn() {
        eventBus.post(new Events.StayLoggedIn());
    }

    @Override
    public void getFingerprintStatus(boolean watching) {
        eventBus.post(new Events.GetFingerprintStatus(watching));
    }

    @Override
    public void authenticateFingerprint(boolean autoLogin) {
        eventBus.post(new Events.AuthenticateFingerprint(autoLogin));
    }

    @Override
    public void relogin() {
        eventBus.post(new Events.Relogin());
    }
}

