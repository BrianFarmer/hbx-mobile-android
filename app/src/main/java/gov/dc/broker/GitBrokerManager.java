package gov.dc.broker;

/**
 * Created by plast on 7/28/2016.
 */
public class GitBrokerManager implements BrokerManagerInterface {

    @Override
    public boolean isLoggedIn() {
        return true;
    }

    @Override
    public void setLoggedIn(boolean loggedIn) {}

    @Override
    public String getUserName() {
        return "Test User";
    }

    @Override
    public void setUserName(String userName) {}

    @Override
    public Boolean getRememberUserName() {
        return true;
    }

    @Override
    public void setRememberUserName(boolean rememberUserName) {}
}
