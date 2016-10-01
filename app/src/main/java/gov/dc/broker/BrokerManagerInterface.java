package gov.dc.broker;

/**
 * Created by plast on 7/28/2016.
 */


public interface BrokerManagerInterface {
    public boolean isLoggedIn();
    public void setLoggedIn(boolean loggedIn);
    public String getUserName();
    public void setUserName(String userName);
    public String getPassword();
    public void setPassword(String userName);
    public Boolean getRememberUserName();
    public void setRememberUserName(boolean rememberUserName);
}


