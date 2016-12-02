package gov.dc.broker;

import android.util.Log;

import java.io.IOException;

/**
 * Created by plast on 10/27/2016.
 */

public class GitSite extends Site {
    private final String TAG = "GitSite";
    JsonParser parser = new JsonParser();

    private static String BrokerAgencyUrl = "https://raw.githubusercontent.com/dchealthlink/HBX-mobile-app-APIs/v0.2/enroll/broker/employers_list/response/example.json";

    public GitSite(){
        Log.d(TAG, "GitSite: In GitSite.GitSite");
    }

    @Override
    public void Login(AccountInfo accountInfo) {
        accountInfo.sessionId = "sessionid";
        accountInfo.securityQuestion = "This is a test question?";
    }

    @Override
    public void Logout(Events.LogoutRequest logout) {
    }

    @Override
    public String GetBrokerAgency(Events.GetEmployerList getEmployerList, AccountInfo accountInfo) throws IOException {
        return getUrl(BrokerAgencyUrl);
    }

    @Override
    public String GetEmployer(Events.GetEmployer getEmployer, String url, AccountInfo accountInfo) throws IOException{
        return getUrl(url);
    }

    @Override
    String getRoster(Events.GetRoster getRoster, String url, AccountInfo accountInfo) throws Exception {
        return getUrl(url);
    }

    @Override
    public String GetCarriers(Events.GetCarriers getCarriers) throws IOException{
        return getUrl("https://dchealthlink.com/shared/json/carriers.json");
    }

    @Override
    public void checkSecurityAnswer(AccountInfo accountInfo) {

    }
}
