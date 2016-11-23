package gov.dc.broker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by plast on 9/23/2016.
 */

public abstract class Site {

    abstract void Login(AccountInfo accountInfo) throws IOException, Exception;
    abstract void Logout(Events.LogoutRequest logout) throws IOException;

    public void initEnrollServerInfo(String enrollServerBaseUrl) throws URISyntaxException {}

    abstract String GetEmployerList(Events.GetEmployerList getEmployerList, AccountInfo accountInfo) throws Exception;
    abstract String GetEmployer(Events.GetEmployer getEmployer, String url, AccountInfo accountInfo) throws Exception;
    abstract String getRoster(Events.GetRoster getRoster, String url, AccountInfo accountInfo) throws Exception;
    abstract String GetCarriers(Events.GetCarriers getCarriers) throws IOException;
    abstract void checkSecurityAnswer(AccountInfo accountInfo) throws Exception;

    static protected String getUrl(String urlString) throws IOException {
        URL url = new URL(urlString);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        return response.toString();
    }

    public abstract String getEmployee(Events.GetEmployee getEmployee, BrokerClient client);
}
