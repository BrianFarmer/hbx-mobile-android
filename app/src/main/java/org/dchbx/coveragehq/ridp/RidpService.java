package org.dchbx.coveragehq.ridp;

import android.content.Intent;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.dchbx.coveragehq.BrokerApplication;
import org.dchbx.coveragehq.ConfigurationStorageHandler;
import org.dchbx.coveragehq.ConnectionHandler;
import org.dchbx.coveragehq.DateTimeDeserializer;
import org.dchbx.coveragehq.Events;
import org.dchbx.coveragehq.IConnectionHandler;
import org.dchbx.coveragehq.IServiceManager;
import org.dchbx.coveragehq.JsonParser;
import org.dchbx.coveragehq.LocalDateSerializer;
import org.dchbx.coveragehq.LocalTimeDeserializer;
import org.dchbx.coveragehq.Messages;
import org.dchbx.coveragehq.StateProcessor;
import org.dchbx.coveragehq.UrlHandler;
import org.dchbx.coveragehq.models.account.Account;
import org.dchbx.coveragehq.models.fe.Family;
import org.dchbx.coveragehq.models.ridp.Address;
import org.dchbx.coveragehq.models.ridp.Answer;
import org.dchbx.coveragehq.models.ridp.Answers;
import org.dchbx.coveragehq.models.ridp.Email;
import org.dchbx.coveragehq.models.ridp.PersonDemographics;
import org.dchbx.coveragehq.models.ridp.PersonName;
import org.dchbx.coveragehq.models.ridp.Phone;
import org.dchbx.coveragehq.models.ridp.Question;
import org.dchbx.coveragehq.models.ridp.QuestionResponse;
import org.dchbx.coveragehq.models.ridp.Questions;
import org.dchbx.coveragehq.models.ridp.SignUp.Person;
import org.dchbx.coveragehq.models.ridp.SignUp.SignUp;
import org.dchbx.coveragehq.models.ridp.SignUp.SignUpResponse;
import org.dchbx.coveragehq.models.ridp.VerifyIdentity;
import org.dchbx.coveragehq.models.ridp.VerifyIdentityPerson;
import org.dchbx.coveragehq.models.ridp.VerifyIdentityResponse;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.HashMap;

import static org.dchbx.coveragehq.statemachine.StateManager.AppEvents.Error;
import static org.dchbx.coveragehq.statemachine.StateManager.AppEvents.GetQuestionsOperationComplete;

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
public class RidpService extends StateProcessor {
    private static String TAG = "RidpService";

    public static String QuestionsAndAnswers = "QuestionsAndAnswers";
    public static String VerifyIdentityResponse = "VerifyIdentityResponse";
    public static String Account = "Account";
    private final IServiceManager serviceManager;
    private Messages messages;

    public RidpService(IServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        messages = BrokerApplication.getBrokerApplication().getMessages(this);
    }

    private VerifyIdentity veriftyIdentityFromAccount(org.dchbx.coveragehq.models.account.Account account) {
        VerifyIdentity identity = new VerifyIdentity();
        identity.person = new VerifyIdentityPerson();
        identity.person.addresses = new ArrayList<>();
        Address address = new Address();
        address.address = new Address.InternalAddress();
        address.address.type = "home";
        address.address.addressLine1 = account.address.address1;
        if (account.address.address2 != null) {
            address.address.addressLine2 = account.address.address2;
        } else {
            address.address.addressLine2 = "";
        }
        address.address.locationCityName = account.address.city;
        address.address.locationStateCode = account.address.state;
        address.address.postalCode = account.address.zipCode;
        identity.person.addresses.add(address);
        identity.person.emails = new ArrayList<>();
        Email email = new Email();
        email.email = new Email.InternalEmail();
        email.email.emailAddress = account.emailAddress;
        email.email.type = "home";
        identity.person.emails.add(email);
        identity.person.phones = new ArrayList<>();
        Phone phone = new Phone();
        phone.phone = new Phone.InternalPhone();
        phone.phone.phoneNumber = "2025551212";
        phone.phone.type = "home";
        identity.person.phones.add(phone);
        identity.person.personName = new PersonName();
        identity.person.personName.personGivenName = account.firstName;
        identity.person.personName.personSurname = account.lastName;

        identity.personDemographics = new PersonDemographics();
        identity.personDemographics.sex = account.gender;
        identity.personDemographics.ssn = account.ssn;
        DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDate();
        if (account.birthdate != null){
            identity.personDemographics.birthDate = dateTimeFormatter.print(account.birthdate);
        }
        identity.personDemographics.createdAt = dateTimeFormatter.print(LocalDate.now());
        identity.personDemographics.modifiedAt = dateTimeFormatter.print(LocalDate.now());
        return identity;
    }

    public org.dchbx.coveragehq.models.account.Account getCreateAccountInfo() {
        return serviceManager.getConfigurationStorageHandler().readAccount();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.CreateAccount createAccount){
        Log.d(TAG, "in RidpService.verificationRequest");

        try {
            final UrlHandler urlHandler = serviceManager.getUrlHandler();
            ConnectionHandler connectionHandler = serviceManager.getConnectionHandler();
            final EventParameters eventParameters = createAccount.getEventParameters();
            VerifyIdentityResponse verifiyIdentityResponse = (org.dchbx.coveragehq.models.ridp.VerifyIdentityResponse) eventParameters.getObject(VerifyIdentityResponse, org.dchbx.coveragehq.models.ridp.VerifyIdentityResponse.class);
            final Account account = (org.dchbx.coveragehq.models.account.Account) eventParameters.getObject(Account, Account.class);
            SignUp signUp = buildSignUp(verifiyIdentityResponse, account);

            UrlHandler.HttpRequest request = urlHandler.getCreateAccount(signUp);
            connectionHandler.process(request, new IConnectionHandler.OnCompletion() {
                @Override
                public void onCompletion(IConnectionHandler.HttpResponse response) {
                    if (response.getResponseCode() == 201){
                        JsonParser parser = serviceManager.getParser();
                        SignUpResponse signUpResponse = parser.parseSignUpResponse(response.getBody());
                        serviceManager.getConfigurationStorageHandler().store(signUpResponse);
                        serviceManager.getUrlHandler().populateLinks(signUpResponse.links);

                        if (signUpResponse.error != null){
                            if (signUpResponse.error.type.compareTo("userHasActiveMedicaid") == 0){
                                messages.getEventBus().post(new Events.AppEvent(StateManager.AppEvents.SignUpUserInAceds, eventParameters.add("error_msg", signUpResponse.error.message)));
                            } else {
                                messages.getEventBus().post(new Events.AppEvent(StateManager.AppEvents.Error, eventParameters.add("error_msg", signUpResponse.error.message)));
                            }
                        } else {
                            messages.getEventBus().post(new Events.AppEvent(StateManager.AppEvents.SignUpSuccessful, eventParameters));
                        }
                    } else {
                        messages.getEventBus().post(new Events.Error("Bad Http response processing RidpService.signUp", "Events.GetCreateAccountInfo"));
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "exception processing http request");
            messages.getEventBus().post(new Events.Error("Error processing RidpService.signUp", e.getMessage()));
        }
    }

    private void configureFamily(Account account) {
        Family family = new Family();
        JsonArray personArray = new JsonArray();
        family.Person = personArray;
        JsonObject person = new JsonObject();
        person.addProperty("", "");
        family.Relationship = new HashMap<>();
        family.Attestation = new JsonObject();
    }

    private SignUp buildSignUp(VerifyIdentityResponse verifiyIdentityResponse, Account account) {
        SignUp signUp = new SignUp();
        signUp.person = new Person();
        signUp.person.firstName = account.firstName;
        signUp.person.lastName = account.lastName;
        signUp.person.ssn = account.ssn;
        DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDate();
        signUp.person.dob = dateTimeFormatter.print(account.birthdate);
        signUp.username = account.emailAddress;
        signUp.password = account.password;
        signUp.token = verifiyIdentityResponse.token;
        return signUp;
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetRidpQuestions verificationResponse) throws Exception {
        Log.d(TAG, "in RidpService.doThis(Events(GetVerificationResponse)");

        final EventParameters eventParameters = verificationResponse.getEventParameters();
        Account account = (Account) eventParameters.getObject("Account", Account.class);

        UrlHandler urlHandler = serviceManager.getUrlHandler();
        ConnectionHandler connectionHandler = serviceManager.getConnectionHandler();
        VerifyIdentity verifyIdentity = veriftyIdentityFromAccount(account);

        UrlHandler.HttpRequest request = urlHandler.getRidpVerificationParameters(verifyIdentity);
        connectionHandler.process(request, new IConnectionHandler.OnCompletion() {
            @Override
            public void onCompletion(IConnectionHandler.HttpResponse response) {
            if (response.getResponseCode() == 200){
                Log.d(TAG, "verification request successful");
                JsonParser parser = new JsonParser();
                Questions questions = parser.parseRidpQuestions(response.getBody());
                QuestionsAndAnswers questionsAndAnswers = new QuestionsAndAnswers();
                questionsAndAnswers.questions = questions;
                questionsAndAnswers.answers = buildAnswerObject(questions);
                messages.appEvent(GetQuestionsOperationComplete, eventParameters.add(QuestionsAndAnswers, questionsAndAnswers));
            } else {
                messages.appEvent(Error, EventParameters.build().add("error_msg", "An error happened getting the verificaiton questions"));
            }
        }});
    }

    public void updateAnswers(Answers answers) {
        ConfigurationStorageHandler storageHandler = serviceManager.getConfigurationStorageHandler();
        storageHandler.store(answers);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(final Events.VerifyUser verificationResponse) throws Exception {
        Log.d(TAG, "in RidpService.verificationRequest");

        UrlHandler urlHandler = serviceManager.getUrlHandler();
        final EventParameters eventParameters = verificationResponse.getEventParameters();
        QuestionsAndAnswers questionsAndAnswers = (RidpService.QuestionsAndAnswers) eventParameters.getObject(QuestionsAndAnswers, QuestionsAndAnswers.class);

        UrlHandler.HttpRequest request = urlHandler.getAnswersRequest(questionsAndAnswers.answers);
        serviceManager.getConnectionHandler().process(request, new IConnectionHandler.OnCompletion() {
            @Override
            public void onCompletion(IConnectionHandler.HttpResponse response) {
                if (response.getResponseCode() == 200){
                    JsonParser parser = serviceManager.getParser();
                    VerifyIdentityResponse verifiyIdentityResponse = parser.parseVerificationResponse(response.getBody());
                    eventParameters.add(VerifyIdentityResponse, verifiyIdentityResponse);
                    switch (determineState(verifiyIdentityResponse)){
                        case InRoster:
                            messages.appEvent(StateManager.AppEvents.UserVerifiedSsnWithEmployer, eventParameters);
                            break;
                        case InEnroll:
                            messages.appEvent(StateManager.AppEvents.UserVerifiedFoundYou, eventParameters);
                            break;
                        case OkToCreateAccount:
                            messages.appEvent(StateManager.AppEvents.UserVerifiedOkToCreate, eventParameters);
                            break;
                    }
                } else {
                    messages.appEvent(StateManager.AppEvents.Error, EventParameters.build().add("error_msg", "Bad Http response processing RidpService.verificationRequest"));
                }

            }
        });
    }

    private VerifiyIdentityResponseStates determineState(VerifyIdentityResponse verifiyIdentityResponse) {
        if (verifiyIdentityResponse.ridpVerified && verifiyIdentityResponse.userFoundInEnroll){
            return VerifiyIdentityResponseStates.InEnroll;
        }
        if (verifiyIdentityResponse.ridpVerified
            && verifiyIdentityResponse.employers != null
            && verifiyIdentityResponse.employers.size() > 0){
            return VerifiyIdentityResponseStates.InRoster;
        }
        return VerifiyIdentityResponseStates.OkToCreateAccount;
    }

    public VerifyIdentityResponse getVerificationResponse() {
        ConfigurationStorageHandler configurationStorageHandler = serviceManager.getConfigurationStorageHandler();
        return configurationStorageHandler.readVerifiyIdentityResponse();
    }

    public static Account getNewAccount() {
        return new Account();
    }

    public static Account getAccountFromIntent(Intent intent){
        String jsonString = intent.getStringExtra("Account");
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateSerializer());
        gsonBuilder.registerTypeAdapter(LocalTime.class, new LocalTimeDeserializer());
        return gsonBuilder.create().fromJson(jsonString, Account.class);
    }

    public static QuestionsAndAnswers getQuestionsFromIntent(Intent intent) {
        String jsonString = intent.getStringExtra(QuestionsAndAnswers);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateSerializer());
        gsonBuilder.registerTypeAdapter(LocalTime.class, new LocalTimeDeserializer());
        return gsonBuilder.create().fromJson(jsonString, QuestionsAndAnswers.class);
    }

    public static class QuestionsAndAnswers {
        public Questions questions;
        public Answers answers;
    }

    public QuestionsAndAnswers getRidpQuestions() {
        Questions questions = new Questions();
        Answers answers = new Answers();
        if (answers == null && questions != null){
            answers = buildAnswerObject(questions);
        }
        QuestionsAndAnswers questionsAndAnswers = new QuestionsAndAnswers();
        questionsAndAnswers.questions = questions;
        questionsAndAnswers.answers = answers;
        return questionsAndAnswers;
    }

    private Answers buildAnswerObject(Questions questions) {
        Answers answers = new Answers();
        answers.sessionId = questions.session.sessionId;
        answers.transactionId = questions.session.transactionId;
        answers.questionResponse = new ArrayList<>();
        for (Question question : questions.session.questions) {
            QuestionResponse questionResponse = new QuestionResponse();
            questionResponse.questionId = question.questionId;
            questionResponse.answer = new Answer();
            answers.questionResponse.add(questionResponse);
        }
        return answers;
    }

    private enum VerifiyIdentityResponseStates {
        InRoster,
        InEnroll,
        OkToCreateAccount
    }

    public static String stripSsnDashes(String ssn){
        String unmasked = "";
        for (int i = 0; i < ssn.length(); i ++){
            if (!ssn.substring(i, i + 1).equals("-")){
                unmasked = unmasked + ssn.substring(i, i + 1);
            }
        }
        return unmasked;
    }
}
