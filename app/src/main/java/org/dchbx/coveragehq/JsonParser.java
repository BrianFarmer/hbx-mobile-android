package org.dchbx.coveragehq;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.dchbx.coveragehq.models.Glossary;
import org.dchbx.coveragehq.models.Security.Endpoints;
import org.dchbx.coveragehq.models.Security.LoginResponse;
import org.dchbx.coveragehq.models.Security.SecurityAnswerResponse;
import org.dchbx.coveragehq.models.brokeragency.BrokerAgency;
import org.dchbx.coveragehq.models.employer.Employer;
import org.dchbx.coveragehq.models.fe.Schema;
import org.dchbx.coveragehq.models.gitaccounts.AccountInfo;
import org.dchbx.coveragehq.models.gitaccounts.GitAccounts;
import org.dchbx.coveragehq.models.planshopping.Plan;
import org.dchbx.coveragehq.models.ridp.Questions;
import org.dchbx.coveragehq.models.ridp.SignUp.SignUpResponse;
import org.dchbx.coveragehq.models.ridp.VerifyIdentityResponse;
import org.dchbx.coveragehq.models.roster.Roster;
import org.dchbx.coveragehq.models.roster.RosterEntry;
import org.dchbx.coveragehq.models.roster.SummaryOfBenefits;
import org.dchbx.coveragehq.models.services.Service;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by plast on 10/27/2016.
 */

public class JsonParser {
    private static final String TAG = "JsonParser";

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

    public GitAccounts parseGitAccounts(String body) throws Exception {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        Gson gson = gsonBuilder.create();
        Type type = new TypeToken<List<Map<String, AccountInfo>>>() {
        }.getType();
        Object object = null;
        try {
            object = gson.fromJson(body.replace("\"\"", "null"), type);
        } catch (Throwable t){
            Log.e(TAG, "exception parsing accounts", t);
        }
        GitAccounts gitAccounts = new GitAccounts();
        List<Map<String, AccountInfo>> list = (List<Map<String, AccountInfo>>) object;
        HashMap<String, AccountInfo> map = new HashMap<>();
        for (Map<String, AccountInfo> stringAccountInfoMap : list) {
            if (map.containsKey(stringAccountInfoMap.keySet().iterator().next())){
                throw new Exception("duplicate account name key found");
            }
            map.putAll(stringAccountInfoMap);
        }

        gitAccounts.setAccountInfo(map);
        return gitAccounts;
    }

    public List<SummaryOfBenefits> parseSummaryOfBenefits(String body) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Type type = new TypeToken<List<SummaryOfBenefits>>() {}.getType();
        Object object = gson.fromJson(body.replace("\"\"", "null"), type);
        return (List<SummaryOfBenefits>)object;
    }

    public List<Service> parseServices(String body) {
        Type type = new TypeToken<List<Service>>() {}.getType();
        return (List<Service>)parse(body, type);
    }

    public RosterEntry parseIndividual(String body) {
        return parseEmployeeDetails(body);
    }

    private static <T> T parse(String jsonString, Type type){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        Gson gson = gsonBuilder.create();
        Object object = gson.fromJson(jsonString.replace("\"\"", "null"), type);
        return (T)object;
    }

    private static <T> T parse(String jsonString, Class<T> c){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(jsonString.replace("\"\"", "null"), c);
    }

    public static PlanShoppingParameters parsePlanShoppingParameters(String body) {
        return parse(body, PlanShoppingParameters.class);
    }

    public Endpoints parseEndpoionts(String body) {
        return parse(body, Endpoints.class);
    }

    public List<Plan> parsePlans(String body) {
        Type type = new TypeToken<List<Plan>>() {}.getType();
        return (List<Plan>)parse(body, type);
    }

    public Questions parseRidpQuestions(String body) {
        return parse(body, Questions.class);
    }

    public VerifyIdentityResponse parseVerificationResponse(String body) {
        return parse(body, VerifyIdentityResponse.class);
    }

    public SignUpResponse parseSignUpResponse(String body) {
        return parse(body, SignUpResponse.class);
    }

    public List<Glossary.GlossaryItem> parseGlossary(String json) {
        Type type = new TypeToken<List<Glossary.GlossaryItem>>() {}.getType();
        return (List<Glossary.GlossaryItem>)parse(json, type);
    }

    public Schema parseSchema(String json) {
        return parse(json, Schema.class);
    }
}
