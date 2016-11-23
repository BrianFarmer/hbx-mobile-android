package gov.dc.broker;

import org.greenrobot.eventbus.EventBus;

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
    public void getEmployer(int employerId) {
        eventBus.post(new Events.GetEmployer(employerId));
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
    public void getEmployerList() {
        eventBus.post(new Events.GetEmployerList());
    }

    @Override
    public void logoutRequest() {
        eventBus.post(new Events.LogoutRequest());

    }

    @Override
    public void release() {
        eventBus.unregister(object);
        eventBus = null;
    }

    @Override
    public void getRoster(int employerId) {
        eventBus.post(new Events.GetRoster(employerId));
    }

    @Override
    public void getEmployee(int employeeId, int employerId) {
        eventBus.post(new Events.GetEmployee(employeeId, employerId));
    }

    @Override
    public void coverageYearChanged(String year) {
        eventBus.post(new Events.CoverageYear(year));
    }
}

