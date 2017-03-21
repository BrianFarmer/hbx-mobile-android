package org.dchbx.coveragehq;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.dchbx.coveragehq.models.Security.LoginResponse;
import org.dchbx.coveragehq.models.Security.SecurityAnswerResponse;
import org.dchbx.coveragehq.models.brokeragency.BrokerAgency;
import org.dchbx.coveragehq.models.employer.Employer;
import org.dchbx.coveragehq.models.gitaccounts.*;
import org.dchbx.coveragehq.models.gitaccounts.AccountInfo;
import org.dchbx.coveragehq.models.roster.Roster;
import org.dchbx.coveragehq.models.roster.RosterEntry;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by plast on 10/27/2016.
 */

public class JsonParser {
    public org.dchbx.coveragehq.models.brokeragency.BrokerAgency   parseEmployerList(String string) throws Exception {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        Gson gson = gsonBuilder.create();
        BrokerAgency brokerAgency = gson.fromJson(string.replace("\"\"", "null"), BrokerAgency.class);
        if (brokerAgency == null
            || (brokerAgency.brokerAgency == null
                && (brokerAgency.brokerClients == null
                    || brokerAgency.brokerClients.size() == 0)
                && brokerAgency.brokerName == null)){
            throw new Exception("BrokerAgency not found.");
        }
        return brokerAgency;
    }

    public Employer parseEmployerDetails(String s){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        Gson gson = gsonBuilder.create();

        return gson.fromJson(s.replace("\"\"", "null"), Employer.class);
    }

    public Roster parseRoster(String s){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        Gson gson = gsonBuilder.create();

        return gson.fromJson(s.replace("\"\"", "null"), Roster.class);
    }

    public Carriers parseCarriers(String s) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(s.replace("\"\"", "null"), Carriers.class);
    }

    public Roster parseEmployeDetails(String employee) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(employee.replace("\"\"", "null"), Roster.class);
    }

    public RosterEntry parseEmployeeDetails(String employee) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(employee.replace("\"\"", "null"), RosterEntry.class);
    }

    public LoginResponse parseLogin(String body) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(body.replace("\"\"", "null"), LoginResponse.class);
    }

    public SecurityAnswerResponse parseSecurityAnswerResponse(String body) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(body.replace("\"\"", "null"), SecurityAnswerResponse.class);
    }

    public GitAccounts parseGitAccounts(String body) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        Gson gson = gsonBuilder.create();
        Type type = new TypeToken<Map<String, AccountInfo>>() {
        }.getType();
        Object object = gson.fromJson(body.replace("\"\"", "null"), type);
        GitAccounts gitAccounts = new GitAccounts();
        gitAccounts.setAccountInfo((Map<String, AccountInfo>) object);
        return gitAccounts;
    }
}