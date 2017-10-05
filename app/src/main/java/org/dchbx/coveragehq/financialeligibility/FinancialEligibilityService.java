package org.dchbx.coveragehq.financialeligibility;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.dchbx.coveragehq.BrokerApplication;
import org.dchbx.coveragehq.ConfigurationStorageHandler;
import org.dchbx.coveragehq.ConnectionHandler;
import org.dchbx.coveragehq.Events;
import org.dchbx.coveragehq.IConnectionHandler;
import org.dchbx.coveragehq.JsonParser;
import org.dchbx.coveragehq.Messages;
import org.dchbx.coveragehq.ServiceManager;
import org.dchbx.coveragehq.UrlHandler;
import org.dchbx.coveragehq.Utilities;
import org.dchbx.coveragehq.models.account.Account;
import org.dchbx.coveragehq.models.fe.Family;
import org.dchbx.coveragehq.models.fe.Field;
import org.dchbx.coveragehq.models.fe.FinancialAssistanceApplication;
import org.dchbx.coveragehq.models.fe.Option;
import org.dchbx.coveragehq.models.fe.PersonForCoverage;
import org.dchbx.coveragehq.models.fe.Schema;
import org.dchbx.coveragehq.models.fe.UqhpApplication;
import org.dchbx.coveragehq.models.fe.UqhpDetermination;
import org.dchbx.coveragehq.models.startup.Status;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
    This file is part of DC.

    DC Health Link SmallBiz is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DC Health Link SmallBiz is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DC Health Link SmallBiz.  If not, see <http://www.gnu.org/licenses/>.
    This statement should go near the beginning of every source file, close to the copyright notices. When using the Lesser GPL, insert the word “Lesser” before “General” in all three places. When using the GNU AGPL, insert the word “Affero” before “General” in all three places.
*/
public class FinancialEligibilityService {
    private static String TAG = "FinancialEligibility";

    public static String EaPersonId = "eapersonid";

    public static String UqhpDetermination = "UqhpDetermination";

    private final Messages messages;
    private final ServiceManager serviceManager;
    private Schema schema;

    public FinancialEligibilityService(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        messages = BrokerApplication.getBrokerApplication().getMessages(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetUqhpFamily getUqhpFamily) {
        Family family = getUqhpFamily();
        messages.getUqhpFamilyResponse(family);
    }

    private Family getUqhpFamily() {
        ConfigurationStorageHandler configurationStorageHandler = serviceManager.getConfigurationStorageHandler();

        return configurationStorageHandler.readUqhpFamily();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.SaveUqhpFamily saveUqhpFamily) {
        ConfigurationStorageHandler configurationStorageHandler = serviceManager.getConfigurationStorageHandler();
        Family family = saveUqhpFamily.getFamily();
        configurationStorageHandler.storeUqhpFamily(family);
        messages.saveUqhpFamilyResponse();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetFinancialAssistanceApplication getFinancialAssistanceApplication) {
        messages.getFinancialAssistanceApplicationResponse(new FinancialAssistanceApplication());
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetFinancialEligibilityJson getFinancialEligibilityJson) throws Exception {
        Log.d(TAG, "in FinancialEligibilityService.doThis(Events.GetFinancialEligibilityJson)");
        if (schema == null) {
            UrlHandler urlHandler = serviceManager.getUrlHandler();
            ConnectionHandler connectionHandler = serviceManager.getConnectionHandler();

            UrlHandler.HttpRequest request = urlHandler.getFinancialEligibilityJson();
            IConnectionHandler.HttpResponse response = connectionHandler.process(request);
            connectionHandler.process(request, new IConnectionHandler.OnCompletion() {
                public void onCompletion(IConnectionHandler.HttpResponse response) {
                    if (response.getResponseCode() == 200) {
                        JsonParser parser = serviceManager.getParser();
                        schema = parser.parseSchema(response.getBody());
                    } else {
                        messages.appEvent(StateManager.AppEvents.Error, EventParameters.build().add("error_msg", "Error getting Schema"));
                    }
                }
            });
        }
        messages.getFinancialEligibilityJsonResponse(schema);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.SendHavenApplication getUqhpSchema) throws Exception {
        Log.d(TAG, "in FinancialEligibilityService.doThis(Events.GetFinancialEligibilityJson)");
        UrlHandler urlHandler = serviceManager.getUrlHandler();
        ConnectionHandler connectionHandler = serviceManager.getConnectionHandler();

        Family uqhpFamily = getUqhpFamily();
        UqhpApplication uqhpApplication = new UqhpApplication();
        uqhpApplication.Person = uqhpFamily.Person;
        uqhpApplication.Attestation = uqhpFamily.Attestation;
        uqhpApplication.Relationship = new JsonArray();
        if (uqhpFamily.Person.size() >= 2) {
            for (int i = 0; i < uqhpFamily.Person.size() - 1; i++) {
                for (int j = i + 1; j < uqhpFamily.Person.size(); j++) {
                    HashMap<String, JsonObject> crosses = uqhpFamily.Relationship.get(uqhpFamily.Person.get(i).getAsJsonObject().get("eapersonid").getAsString());
                    JsonObject relationship = crosses.get(uqhpFamily.Person.get(j).getAsJsonObject().get("eapersonid").getAsString());
                    uqhpApplication.Relationship.add(relationship);
                }
            }
        }

        UrlHandler.HttpRequest request = urlHandler.getHavenApplication(uqhpApplication);
        connectionHandler.process(request, new IConnectionHandler.OnCompletion() {
            public void onCompletion(IConnectionHandler.HttpResponse response) {
            if (response.getResponseCode() == 201) {
                JsonParser parser = FinancialEligibilityService.this.serviceManager.getParser();
                UqhpDetermination uqhpDetermination = parser.parseUqhpDeterminationResponse(response.getBody());
                ConfigurationStorageHandler configurationStorageHandler = serviceManager.getConfigurationStorageHandler();
                configurationStorageHandler.storeUqhpDetermination(uqhpDetermination);
                if (uqhpDetermination.ineligibleForQhp.size() > 0){
                    messages.appEvent(StateManager.AppEvents.ReceivedUqhpDeterminationHasIneligible);
                } else {
                    messages.appEvent(StateManager.AppEvents.ReceivedUqhpDeterminationOnlyEligible);
                }
            } else {
                messages.appEvent(StateManager.AppEvents.Error, EventParameters.build().add("error_msg", "Error in UQHP determination"));
            }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetUqhpSchema getUqhpSchema) throws Exception {
        Log.d(TAG, "in FinancialEligibilityService.doThis(Events.GetFinancialEligibilityJson)");
        if (schema == null) {
            UrlHandler urlHandler = serviceManager.getUrlHandler();
            ConnectionHandler connectionHandler = serviceManager.getConnectionHandler();

            UrlHandler.HttpRequest request = urlHandler.getUqhpSchema();
            connectionHandler.process(request, new IConnectionHandler.OnCompletion() {
                public void onCompletion(IConnectionHandler.HttpResponse response) {
                    if (response.getResponseCode() == 200) {
                        JsonParser parser = serviceManager.getParser();
                        schema = parser.parseSchema(response.getBody());
                    } else {
                        messages.appEvent(StateManager.AppEvents.Error, EventParameters.build().add("error_msg", "Error getting Schema"));
                    }
                }
            });
        }
        messages.getFinancialEligibilityJsonResponse(schema);
    }

    public static JsonObject getNewPerson() {
        return new JsonObject();
    }

    public static JsonObject getNewPerson(Account account, Schema schema) {
        JsonObject person = build(schema.Person);

        person.addProperty("personfirstname", account.getFirstName());
        person.addProperty("personlastname", account.getLastName());
        person.addProperty("persondob", Utilities.DateAsIso8601(account.getBirthdate()));
        person.addProperty("isssntrue", "Y");
        person.addProperty("personssn", account.getSsn());
        if (account.isMale()) {
            person.addProperty("gender", "M");
        } else {
            person.addProperty("gender", "F");
        }
        person.addProperty("homeemail", account.emailAddress);

        /*
        This is a nice to have feature, skipping for now.

        JsonArray addressArray = new JsonArray();
        person.add("Address", addressArray);
        JsonObject address = new JsonObject();
        addressArray.add(address);

        Address accountAddress = account.getAddress();
        address.addProperty("streetaddress1", accountAddress.address1);
        address.addProperty("streetaddress2", accountAddress.address2);
        address.addProperty("city", accountAddress.city);
        address.addProperty("postalcode", accountAddress.zipCode);
        for (Field field : schema.Person) {
            if (field.field == "statecd") {
                for (Option option : field.options) {
                    if (option.value == accountAddress.state) {
                        address.addProperty("statecd", accountAddress.state);
                        break;
                    }
                }
                break;
            }
        }
        */

        return person;
    }

    public static void removeFamilyMember(Family family, int i) {
        String eapersonid = family.Person.get(i).getAsJsonObject().get("eapersonid").getAsString();
        family.Relationship.remove(eapersonid);

        for (Map.Entry<String, HashMap<String, JsonObject>> entry : family.Relationship.entrySet()) {
            if (entry.getValue().containsKey(eapersonid)) {
                entry.getValue().remove(eapersonid);
            }
        }

        family.Person.remove(i);
    }

    public static JsonObject build(ArrayList<Field> dependentFields) {
        JsonObject result = new JsonObject();
        for (Field dependentField : dependentFields) {
            if (dependentField.defaultValue != null) {
                result.addProperty(dependentField.field, dependentField.defaultValue);
            }
        }
        return result;
    }


    public static boolean checkObject(JsonObject object, ArrayList<Field> objectSchema) {
        for (Field field : objectSchema) {
            if (field.optional.equals("N")) {
                switch (Schema.fieldTypes.get(field.type.toLowerCase())) {
                    case text:
                    case numeric:
                        if (!object.has(field.field)
                                || object.get(field.field).getAsString().length() == 0) {
                            return false;
                        }
                        break;
                    case date:
                        if (!object.has(field.field)
                                || object.get(field.field).getAsString().length() < 8) {
                            return false;
                        }
                        break;
                    case dropdown:
                        if (object.has(field.field)) {
                            boolean found = false;
                            for (Option option : field.options) {
                                if (option.value.equals(object.get(field.field).getAsString())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                return false;
                            }
                        } else {
                            return false;
                        }
                        break;
                    case multidropdown:
                        if (!object.has(field.field)
                                || !field.options.contains(object.get(field.field).getAsString())) {
                            return false;
                        }
                        break;
                    case section:
                        if (!object.has(field.field)) {
                            return false;
                        }
                        break;
                    case ssn:
                        if (!object.has(field.field)
                                || object.get(field.field).getAsString().length() < 8) {
                            return false;
                        }
                        break;
                    case yesnoradio:
                        break;
                    case zip:
                        break;

                }
            }
        }

        return true;
    }

    public static void addPersonToFamily(Family family, JsonObject person) {
        family.Person.add(person);
        family.Relationship.put(person.get("eapersonid").getAsString(), new HashMap<String, JsonObject>());
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetUqhpDeterminationFromServer getUqhpDeterminationFromServer) throws Exception {
        final EventParameters eventParameters = getUqhpDeterminationFromServer.getEventParameters();
        Status status = (Status) eventParameters.getObject("Status", Status.class);

        UrlHandler urlHandler = serviceManager.getUrlHandler();
        ConnectionHandler connectionHandler = serviceManager.getConnectionHandler();
        UrlHandler.HttpRequest request = urlHandler.getUqhpDetermination(status.eaid);
        connectionHandler.process(request, new IConnectionHandler.OnCompletion() {
            public void onCompletion(IConnectionHandler.HttpResponse response) {
                if (response.getResponseCode() == 200) {
                    JsonParser parser = FinancialEligibilityService.this.serviceManager.getParser();
                    UqhpDetermination uqhpDetermination = parser.parseUqhpDeterminationResponse(response.getBody());
                    ConfigurationStorageHandler configurationStorageHandler = serviceManager.getConfigurationStorageHandler();
                    configurationStorageHandler.storeUqhpDetermination(uqhpDetermination);
                    if (uqhpDetermination.ineligibleForQhp.size() > 0){
                        messages.appEvent(StateManager.AppEvents.ReceivedUqhpDeterminationHasIneligible);
                    } else {
                        messages.appEvent(StateManager.AppEvents.ReceivedUqhpDeterminationOnlyEligible);
                    }
                } else {
                    messages.appEvent(StateManager.AppEvents.Error, EventParameters.build().add("error_msg", "Error in UQHP determination"));
                }
            }
        });
    }
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetUqhpDetermination getUqhpDetermination) throws Exception {
        ConfigurationStorageHandler configurationStorageHandler = serviceManager.getConfigurationStorageHandler();
        UqhpDetermination uqhpDetermination = configurationStorageHandler.readUqhpDetermination();
        messages.getUqhpDeterminationResponse(uqhpDetermination);
    }

    public static String getNameForPersonForCoverage(PersonForCoverage personForCoverage) {
        return personForCoverage.personFirstName + " " + personForCoverage.personLastName;
    }
}