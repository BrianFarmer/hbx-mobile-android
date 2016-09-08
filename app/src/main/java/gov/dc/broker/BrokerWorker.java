package gov.dc.broker;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BrokerWorker extends IntentService {
    private final String TAG = "BrokerWorker";
    static EventBus eventBus;


    private interface Site {
        void Login(Events.LoginRequest loginRequest);
        void GetEmployerList(Events.GetEmployerList getEmployerList);
        void GetEmployer(Events.GetEmployer getEmployer);
        void GetAccount(Events.GetAccount getAccount);
        void GetCarriers(Events.GetCarriers getCarriers);
    }

    static private class GitSite implements Site{
        private final String TAG = "GitSite";
        private static EmployerList employerList;
        OkHttpClient client;

        String employersList = "https://raw.githubusercontent.com/dchealthlink/HBX-mobile-app-APIs/master/enroll/broker/employers_list/response/example.json";

        public GitSite(){
            client = new OkHttpClient();
            Log.d(TAG, "GitSite: In GitSite.GitSite");
        }

        @Override
        public void Login(Events.LoginRequest loginRequest) {

        }

        @Override
        public void GetEmployerList(Events.GetEmployerList getEmployerList) {
            try{
                int requestId = getEmployerList.getId();
                Request request = new Request.Builder().url(employersList).build();
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(Date.class, new DateTimeDeserializer());
                Gson gson = gsonBuilder.create();
                employerList = gson.fromJson(body, EmployerList.class);
                BrokerWorker.eventBus.post(new Events.EmployerList(requestId, employerList));
            } catch (IOException ex){

            }
        }

        @Override
        public void GetEmployer(Events.GetEmployer getEmployer) {
            BrokerClient brokerClient = employerList.brokerClients.get(getEmployer.getId());

            try{
                int requestId = getEmployer.getId();
                Request request = new Request.Builder().url(brokerClient.employerDetailsUrl).build();
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(Date.class, new DateTimeDeserializer());
                Gson gson = gsonBuilder.create();
                BrokerClientDetails brokerClientDetails = gson.fromJson(body, BrokerClientDetails.class);
                BrokerWorker.eventBus.post(new Events.BrokerClient (requestId, brokerClient, brokerClientDetails));
            } catch (IOException ex){

            }
        }

        @Override
        public void GetAccount(Events.GetAccount getAccount) {

        }

        @Override
        public void GetCarriers(Events.GetCarriers getCarriers) {
            try{
                int requestId = getCarriers.getId();
                Request request = new Request.Builder().url("https://dchealthlink.com/shared/json/carriers.json").build();
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(Date.class, new DateTimeDeserializer());
                Gson gson = gsonBuilder.create();
                Carriers carriers = gson.fromJson(body, Carriers.class);
                BrokerWorker.eventBus.post(new Events.Carriers (requestId, carriers));
            } catch (IOException ex){

            }
        }
    }

    static GitSite gitSite = new GitSite();

    static Site[] sites = {
        gitSite
    };

    Site currentSite = sites[0];

    public BrokerWorker() {
        super("WorkIntentService");
        Log.d(TAG, "BrokerWorker: In constructor");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        eventBus = EventBus.getDefault();
        Log.d(TAG, "Calling eventbus.register");
        eventBus.register(this);
        Log.d(TAG, "back from eventbus.register");
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(
            Events.LoginRequest loginRequest) {
        currentSite.Login(loginRequest);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetAccount getAccount) {
        currentSite.GetAccount(getAccount);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetEmployer getEmployer) {
        currentSite.GetEmployer(getEmployer);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetEmployerList getEmployerList) {
        currentSite.GetEmployerList(getEmployerList);
    }

    @Subscribe(threadMode =  ThreadMode.BACKGROUND)
    public void doThis(Events.GetCarriers getCarriers){
        currentSite.GetCarriers(getCarriers);
    }
}


class DateTimeDeserializer implements JsonDeserializer<Date> {
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        String dateString = json.getAsJsonPrimitive().getAsString();
        String[] parts = dateString.split("-");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.parseInt(parts[2]), Integer.parseInt(parts[0]) - 1, Integer.parseInt(parts[1]));

        return calendar.getTime();
    }
}