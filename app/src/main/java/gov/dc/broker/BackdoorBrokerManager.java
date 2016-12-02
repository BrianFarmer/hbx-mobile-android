package gov.dc.broker;

/**
 * Created by plast on 9/13/2016.
 */
public class BackdoorBrokerManager implements BrokerManagerInterface {
    private static final String TAG = "BackdoorBrokerManager";
    static private boolean loggedIn = false;
    static private String userName;
    static private boolean rememberUserName = true;
    static private String password;
    static private boolean rememberMe;

    @Override
    public boolean isLoggedIn() {
        return loggedIn;
    }

    @Override
    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    @               Override
    public String getUserName() {
        return userName;
    }

    @Override
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public Boolean getRememberUserName() {
        return this.rememberMe;
    }

    @Override
    public void setRememberUserName(boolean rememberUserName) {
        this.rememberMe = rememberUserName;
    }
}
