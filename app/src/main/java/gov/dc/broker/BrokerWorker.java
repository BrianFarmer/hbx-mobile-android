package gov.dc.broker;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.lang.reflect.Type;

import gov.dc.broker.models.brokeragency.BrokerAgency;
import gov.dc.broker.models.brokeragency.BrokerClient;
import gov.dc.broker.models.employer.Employer;
import gov.dc.broker.models.roster.Roster;
import gov.dc.broker.models.roster.RosterEntry;


public class BrokerWorker extends IntentService {
    private static String TAG = "BrokerWorker";

    static EventBus eventBus;
    private AccountInfo inProgressAccountInfo; // this member is used to store the account info while the user is trying to login.

    //static AccountInfoStorage accountInfoStorage = new SharedPreferencesAccountInfoStorage();

    //static HbxSite.ServerSiteConfig enrollFeatureServerSite = new HbxSite.ServerSiteConfig("http", "ec2-54-234-22-53.compute-1.amazonaws.com", 443);
    //static HbxSite.ServerSiteConfig enrollFeatureServerSite = new HbxSite.ServerSiteConfig("http", "54.224.226.203", 443);

    BuildConfig2 config = BuildConfig2.getConfig();


    public BrokerWorker() {
        super("WorkIntentService");
        Log.d(TAG, "BrokerWorker: In constructor");
    }


    // This causes the background functionality to be initialized.

    @Override
    protected void onHandleIntent(Intent intent) {
        try {

        eventBus = EventBus.getDefault();
        Log.d(TAG, "Calling eventbus.register");
        eventBus.register(this);
        Log.d(TAG, "back from eventbus.register");
        }
        catch (Exception e){
            Log.e(TAG, "Error setting up event bus", e);
        }
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.LoginRequest loginRequest) {

        try {
            ServerConfiguration serverConfiguration = config.getServerConfiguration();
            UrlHandler urlHandler = config.getUrlHandler();
            Log.d(TAG, "Received LoginRequest message");
            String accountName = loginRequest.getAccountName().toString();
            String password = loginRequest.getPassword().toString();
            Boolean rememberMe = loginRequest.getRememberMe();
            Log.d(TAG,"LoginRequest: Getting sessionid");
            config.getCoverageConnection().validateUserAndPassword(accountName, password, rememberMe);
            Log.d(TAG,"LoginRequest: got sessionid");
            BrokerWorker.eventBus.post(new Events.GetSecurityAnswer(serverConfiguration.securityQuestion));
        }
        catch (Exception e) {
            Log.e(TAG, "Exception processing LoginReqeust", e);
            e.printStackTrace();
            BrokerWorker.eventBus.post(new Events.Error("Error logging in"));
        }
        return;
    }



    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.SecurityAnswer securityAnswer) {

        try {
            ServerConfiguration serverConfiguration = config.getServerConfiguration();

            Log.d(TAG, "Received SecurityAnswer message");
            String securityAnswerString = securityAnswer.getSecurityAnswer();
            Log.d(TAG,"LoginRequest: Getting sessionid");
            config.getCoverageConnection().checkSecurityAnswer(securityAnswerString);
            Log.d(TAG,"LoginRequest: got sessionid");
            config.getCoverageConnection().determineUserType();
            BrokerWorker.eventBus.post(new Events.LoginRequestResult(Events.LoginRequestResult.Success));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing SecurityAnswer");
            e.printStackTrace();
            BrokerWorker.eventBus.post(new Events.Error("Error logging in"));
        }
    }



    /**
     * Retrieves the stored login information.
     * <p/>
     * This method handles the event GetLogin which is sent
     * when something wants the current login information.
     * The login information is returned in the event
     * GetLoginRequest.
     * @param getLogin
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetLogin getLogin) {
        try {
            Log.d(TAG, "Received GetLogin message");

            ;
            ServerConfiguration serverConfiguration = config.getCoverageConnection().getLogin();
            BrokerWorker.eventBus.post(new Events.GetLoginResult(serverConfiguration.accountName, serverConfiguration.password,
                                                                 serverConfiguration.securityAnswer, serverConfiguration.rememberMe,
                                                                 config.getCoverageConnection().userTypeFromAccountInfo()));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetLogin");
            BrokerWorker.eventBus.post(new Events.Error("Error logging in"));
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.LogoutRequest logoutRequest){
        Log.d(TAG, "Received LogoutRequest message");
        config.getCoverageConnection().logout();
        BrokerWorker.eventBus.post(new Events.LoggedOutResult());
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetEmployer getEmployer) {
        try {
            Log.d(TAG, "Received GetEmployer");

            DateTime now = DateTime.now();
            CoverageConnection coverageConnection = config.getCoverageConnection();
            BrokerClient brokerClient = BrokerUtilities.getBrokerClient(coverageConnection.getBrokerAgency(now), getEmployer.getEmployerId());
            Employer employer = coverageConnection.getEmployer(getEmployer.getEmployerId(), now);
            BrokerWorker.eventBus.post(new Events.BrokerClient(getEmployer.getId(), brokerClient, employer));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetEmployer");
            BrokerWorker.eventBus.post(new Events.Error("Error getting employer details"));
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetRoster getRoster) {
        try {
            Log.d(TAG, "Received GetRoster");
            DateTime now = DateTime.now();
            config.getCoverageConnection().getRoster(getRoster.getEmployerId(), now);
            Roster roster = config.getCoverageConnection().getRoster(getRoster.getEmployerId(), now);
            BrokerWorker.eventBus.post(new Events.RosterResult (getRoster.getId(), roster));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetEmployer");
            BrokerWorker.eventBus.post(new Events.Error("Error getting employer details"));
        }
    }


    private void sendLogin(String reason){
        BrokerWorker.eventBus.post(new Events.Finish(reason));
    }

    private void sendError() {
        BrokerWorker.eventBus.post(new Events.Error("Error getting employee"));

    }




    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetEmployerList getEmployerList) {
        try {
            Log.d(TAG, "Received GetBrokerAgency message.");
            BrokerAgency brokerAgency = config.getCoverageConnection().getBrokerAgency(DateTime.now());
            BrokerWorker.eventBus.post(new Events.EmployerList (getEmployerList.getId(), brokerAgency));
        }
        catch(Throwable e) {
            Log.e(TAG, "Exception processing GetBrokerAgency");
            BrokerWorker.eventBus.post(new Events.Error("Error getting employer list"));
        }
    }

    @Subscribe(threadMode =  ThreadMode.BACKGROUND)
    public void doThis(Events.GetCarriers getCarriers)
    {
        try {
            Log.d(TAG, "Received GetCarriers message");
            Carriers carriers = config.getCoverageConnection().getCarriers();
            BrokerWorker.eventBus.post(new Events.Carriers(getCarriers.getId(), carriers));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetCarriers");
            BrokerWorker.eventBus.post(new Events.Error("Error getting carriers"));
        }
    }

    @Subscribe(threadMode =  ThreadMode.BACKGROUND)
    public void doThis(Events.GetEmployee getEmployee)
    {
        try {
            Log.d(TAG, "Received GetEmployee message");

            RosterEntry rosterEntry = config.getCoverageConnection().getEmployee(getEmployee.getEmployerId(), getEmployee.getEmployeeId());
            BrokerWorker.eventBus.post(new Events.Employee(getEmployee.getId(),getEmployee.getEmployeeId(), getEmployee.getEmployerId(), rosterEntry));
        } catch (Exception e) {
            Log.e(TAG, "Exception processing GetEmployee");
            BrokerWorker.eventBus.post(new Events.Error("Error getting employee"));
        }
    }
}


class DateTimeDeserializer implements JsonDeserializer<DateTime> {
    public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonPrimitive primitive = json.getAsJsonPrimitive();
        if (primitive.isString()){
            String s = primitive.toString();
            if (s.length() == 0){
                return null;
            }
        }
        return new DateTime(primitive.getAsString());
    }
}

class LocalDateDeserializer implements JsonDeserializer<LocalDate> {
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonPrimitive primitive = json.getAsJsonPrimitive();
        if (primitive.isString()){
            String s = primitive.toString();
            if (s.length() == 0){
                return null;
            }
        }
        return new LocalDate(primitive.getAsString());
    }
}