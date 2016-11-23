package gov.dc.broker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;

import gov.dc.broker.models.brokerclient.BrokerClientDetails;
import gov.dc.broker.models.roster.Employee;
import gov.dc.broker.models.roster.Roster;

/**
 * Created by plast on 10/27/2016.
 */

public class JsonParser {
    public gov.dc.broker.EmployerList parseEmployerList(String string){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(string, EmployerList.class);
    }

    public BrokerClientDetails parseEmployerDetails(String s){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        Gson gson = gsonBuilder.create();

        return gson.fromJson(s, BrokerClientDetails.class);
    }

    public Roster parseRoster(String s){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        Gson gson = gsonBuilder.create();

        return gson.fromJson(s, Roster.class);
    }

    public Carriers parseCarriers(String s) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(s, Carriers.class);
    }

    public Employee parseEmployeDetails(String employee) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(employee, Employee.class);

    }
}
