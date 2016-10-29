package gov.dc.broker;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by plast on 10/27/2016.
 */

public class EventBusMessages implements Messages {

    public EventBusMessages(){
    }

    @Override
    public void getEmployer(int employerId) {
        EventBus.getDefault().post(new Events.GetEmployer(employerId));
    }

    @Override
    public void getLogin() {
        EventBus.getDefault().post(new Events.GetLogin());
    }

    @Override
    public void securityAnswer(String securityAnswer) {
        EventBus.getDefault().post(new Events.SecurityAnswer(securityAnswer));
    }

    @Override
    public void loginRequest(Events.LoginRequest loginRequest) {
        EventBus.getDefault().post(loginRequest);
    }

    @Override
    public void getEmployerList() {
        EventBus.getDefault().post(new Events.GetEmployerList());
    }
}

