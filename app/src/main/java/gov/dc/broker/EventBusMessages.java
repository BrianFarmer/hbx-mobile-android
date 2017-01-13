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
}

