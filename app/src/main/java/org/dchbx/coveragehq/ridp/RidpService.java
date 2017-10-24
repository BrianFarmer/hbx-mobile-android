package org.dchbx.coveragehq.ridp;

import android.content.Intent;
import android.util.Log;

import com.google.gson.GsonBuilder;

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
import org.dchbx.coveragehq.models.ridp.SignUp.Error;
import org.dchbx.coveragehq.models.ridp.SignUp.Person;
import org.dchbx.coveragehq.models.ridp.SignUp.SignUp;
import org.dchbx.coveragehq.models.ridp.SignUp.SignUpResponse;
import org.dchbx.coveragehq.models.ridp.VerifyIdentity;
import org.dchbx.coveragehq.models.ridp.VerifyIdentityPerson;
import org.dchbx.coveragehq.models.ridp.VerifyIdentityResponse;
import org.dchbx.coveragehq.models.ridp.WrongAnswersResponse;
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

import static org.dchbx.coveragehq.statemachine.StateManager.AppEvents.Error;
import static org.dchbx.coveragehq.statemachine.StateManager.AppEvents.GetQuestionsOperationComplete;
import static org.dchbx.coveragehq.statemachine.StateManager.AppEvents.RidpConnectionFailure;
import static org.dchbx.coveragehq.statemachine.StateManager.AppEvents.RidpUserNotFound;
import static org.dchbx.coveragehq.statemachine.StateManager.AppEvents.RidpWrongAnswersLockout;
import static org.dchbx.coveragehq.statemachine.StateManager.AppEvents.RidpWrongAnswersRecoverable;

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

    private VerifyIdentity verifyIdentityFromAccount(org.dchbx.coveragehq.models.account.Account account) {
        VerifyIdentity identity = new VerifyIdentity();
        identity.person = new VerifyIdentityPerson();
        identity.person.addresses = new ArrayList<>();
        Address address = new Address();
        address.address = new Address.InternalAddress();
        address.address.type = "home";
        address.address.addressLine1 = account.getAddress().address1;
        if (account.address.address2 != null) {
            address.address.addressLine2 = account.getAddress().address2;
        } else {
            address.address.addressLine2 = "";
        }
        address.address.locationCityName = account.getAddress().city;
        address.address.locationStateCode = account.getAddress().state;
        address.address.postalCode = account.getAddress().zipCode;
        identity.person.addresses.add(address);
        identity.person.emails = new ArrayList<>();
        Email email = new Email();
        email.email = new Email.InternalEmail();
        email.email.emailAddress = account.getEmailAddress();
        email.email.type = "home";
        identity.person.emails.add(email);
        identity.person.phones = new ArrayList<>();
        Phone phone = new Phone();
        phone.phone = new Phone.InternalPhone();
        phone.phone.phoneNumber = account.getPhone();
        phone.phone.type = "home";
        identity.person.phones.add(phone);
        identity.person.personName = new PersonName();
        identity.person.personName.personGivenName = account.getFirstName();
        identity.person.personName.personSurname = account.getLastName();

        identity.personDemographics = new PersonDemographics();
        identity.personDemographics.sex = account.getGender();
        identity.personDemographics.ssn = account.getSsn();
        DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDate();
        if (account.birthdate != null){
            identity.personDemographics.birthDate = dateTimeFormatter.print(account.getBirthdate());
        }
        identity.personDemographics.createdAt = dateTimeFormatter.print(LocalDate.now());
        identity.personDemographics.modifiedAt = dateTimeFormatter.print(LocalDate.now());
        return identity;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void doThis(Events.GetCreateAccountInfo getCreateAccountInfo){
            org.dchbx.coveragehq.models.account.Account account = serviceManager.getRidpService().getCreateAccountInfo();
            messages.getCreateAccountInfoResult(account);
    }


    public org.dchbx.coveragehq.models.account.Account getCreateAccountInfo() {
        return serviceManager.getConfigurationStorageHandler().readAccount();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
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
                    //regardless of response code, we assume the server responds with the correct structure. If not,
                    //we'd throw an error caught down below. We treat any 200-206 response without an error as success.
                    boolean success = response.getResponseCode() >= 200 && response.getResponseCode() <= 206;

                    JsonParser parser = serviceManager.getParser();
                    SignUpResponse signUpResponse = parser.parseSignUpResponse(response.getBody());
                    if (success) {
                        serviceManager.getConfigurationStorageHandler().store(signUpResponse);
                        serviceManager.getUrlHandler().populateLinks(signUpResponse.links);
                    }

                    Error error = signUpResponse.error;

                    if (error != null) {
                        if ("userHasActiveMedicaid".equals(error.type)) {
                            messages.getEventBus().post(new Events.AppEvent(StateManager.AppEvents.SignUpUserInAceds, eventParameters.add("error_msg", error.message)));
                        } else if ("UserError".equals(error.type)) {
                            messages.getEventBus().post(new Events.AppEvent(StateManager.AppEvents.SignUpFailed, eventParameters.add("error_msg", error.message).add("Account", account)));
                        } else {
                            messages.getEventBus().post(new Events.AppEvent(StateManager.AppEvents.SignUpFailed, eventParameters.add("Account", account)));
                        }
                    } else {
                        if (success) {
                            messages.getEventBus().post(new Events.AppEvent(StateManager.AppEvents.SignUpSuccessful, eventParameters));
                        } else {
                            messages.getEventBus().post(new Events.Error("Bad Http response processing RidpService.signUp", "Events.GetCreateAccountInfo"));
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "exception processing http request");
            messages.getEventBus().post(new Events.Error("Error processing RidpService.signUp", e.getMessage()));
        }
    }

    private SignUp buildSignUp(VerifyIdentityResponse verifiyIdentityResponse, Account account) {
        SignUp signUp = new SignUp();
        signUp.person = new Person();
        signUp.person.firstName = account.firstName;
        signUp.person.lastName = account.lastName;
        signUp.person.ssn = account.ssn.replace("-", "");
        DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDate();
        signUp.person.dob = dateTimeFormatter.print(account.birthdate);
        signUp.username = account.emailAddress;
        signUp.password = account.password;
        signUp.token = verifiyIdentityResponse.token;
        return signUp;
    }


    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void doThis(Events.GetRidpQuestions verificationResponse) throws Exception {
        Log.d(TAG, "in RidpService.doThis(Events(GetVerificationResponse)");

        final EventParameters eventParameters = verificationResponse.getEventParameters();
        Account account = (Account) eventParameters.getObject("Account", Account.class);

        UrlHandler urlHandler = serviceManager.getUrlHandler();
        ConnectionHandler connectionHandler = serviceManager.getConnectionHandler();
        VerifyIdentity verifyIdentity = verifyIdentityFromAccount(account);

        UrlHandler.HttpRequest request = urlHandler.getRidpVerificationParameters(verifyIdentity);

        connectionHandler.process(request, new IConnectionHandler.OnCompletion() {
            @Override
            public void onCompletion(IConnectionHandler.HttpResponse response) {
                int responseCode = response.getResponseCode();

                //COMMENT THIS IN TO TEST RIDP ACCOUNT NOT FOUND
                //responseCode = 401;

                //COMMENT THIS IN TO TEST RIDP UNREACHABLE
                //responseCode = 503;

                if (responseCode == 200){
                    Log.d(TAG, "verification request successful");
                    JsonParser parser = new JsonParser();
                    Questions questions = parser.parseRidpQuestions(response.getBody());
                    QuestionsAndAnswers questionsAndAnswers = new QuestionsAndAnswers();
                    questionsAndAnswers.questions = questions;
                    questionsAndAnswers.answers = buildAnswerObject(questions);
                    messages.appEvent(GetQuestionsOperationComplete, eventParameters.add(QuestionsAndAnswers, questionsAndAnswers));
                } else if (responseCode == 503) {
                    messages.appEvent(RidpConnectionFailure, eventParameters); //TODO “We're sorry. The third-party service used to confirm your identity is currently unavailable. Please try again later. If you continue to receive this message after trying several times, please call DC Health Link customer service for assistance at 1-855-532-5464.”
                } else if (responseCode == 401) {
                    messages.appEvent(RidpUserNotFound, eventParameters);
                } else {
                    messages.appEvent(Error, EventParameters.build().add("error_msg", "An error happened getting the verificaiton questions"));
                }
        }});
    }

    public void updateAnswers(Answers answers) {
        ConfigurationStorageHandler storageHandler = serviceManager.getConfigurationStorageHandler();
        storageHandler.store(answers);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void doThis(final Events.VerifyUser verificationResponse) throws Exception {
        Log.d(TAG, "in RidpService.verificationRequest");

        UrlHandler urlHandler = serviceManager.getUrlHandler();
        final EventParameters eventParameters = verificationResponse.getEventParameters();
        QuestionsAndAnswers questionsAndAnswers = (RidpService.QuestionsAndAnswers) eventParameters.getObject(QuestionsAndAnswers, QuestionsAndAnswers.class);
        final Account account = (org.dchbx.coveragehq.models.account.Account) eventParameters.getObject(Account, Account.class);

        UrlHandler.HttpRequest request = urlHandler.getAnswersRequest(questionsAndAnswers.answers);
        serviceManager.getConnectionHandler().process(request, new IConnectionHandler.OnCompletion() {
            @Override
            public void onCompletion(IConnectionHandler.HttpResponse response) {
                int responseCode = response.getResponseCode();
                String responseBody = response.getBody();

                //COMMENT THIS IN TO TEST RIDP WRONG ANSWERS RECOVERABLE
                //responseCode = 412;
                //responseBody = "{\n  \"verification_result\": {\n    \"response_code\": \"urn:openhbx:terms:v1:interactive_identity_verification#FAILURE\",\n    \"response_text\": \"You have not passed identity validation. To proceed please contact Experian at 1-866-578-5409, and provide them with reference number 2ffe-a5-1cbf.\",\n    \"transaction_id\": \"2ffe-a5-1cbf\"\n  },\n  \"session\": null,\n  \"ridp_verified\": false\n}";
                //COMMENT THIS IN TO TEST RIDP WRONG ANSWERS LOCKOUT
                //responseCode = 403;

                //COMMENT THIS IN TO TEST RIDP UNREACHABLE
                //responseCode = 503;

                handleVerificationResponse(responseCode, responseBody, eventParameters, account);

            }
        });
    }


    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void doThis(final Events.RidpCheckOverride checkOverride) throws Exception {
        Log.d(TAG, "in RidpService.checkOverride");
        UrlHandler urlHandler = serviceManager.getUrlHandler();
        final EventParameters eventParameters = checkOverride.getEventParameters();
        final Account account = (org.dchbx.coveragehq.models.account.Account) eventParameters.getObject(Account, Account.class);
        String transactionId = eventParameters.getString("transactionId");
        UrlHandler.HttpRequest request = urlHandler.getRidpOverrideRequest(transactionId);
        serviceManager.getConnectionHandler().process(request, new IConnectionHandler.OnCompletion() {
            @Override
            public void onCompletion(IConnectionHandler.HttpResponse response) {
                int responseCode = response.getResponseCode();
                handleVerificationResponse(responseCode, response.getBody(), eventParameters, account);
            }
        });
    }

    private void handleVerificationResponse(int responseCode, String responseBody,
                                            EventParameters eventParameters, Account account) {

        if (responseCode == 200){
            JsonParser parser = serviceManager.getParser();
            VerifyIdentityResponse verifiyIdentityResponse = parser.parseVerificationResponse(responseBody);
            eventParameters.add(VerifyIdentityResponse, verifiyIdentityResponse);
            switch (determineState(verifiyIdentityResponse)){
                case InRoster:
                    messages.appEvent(StateManager.AppEvents.UserVerifiedSsnWithEmployer, eventParameters);
                    break;
                case InEnroll:
                    messages.appEvent(StateManager.AppEvents.UserVerifiedFoundYou, eventParameters);
                    break;
                case OkToCreateAccount:
                    ConfigurationStorageHandler configurationStorageHandler = serviceManager.getConfigurationStorageHandler();
                    configurationStorageHandler.clearUqhpFamily();
                    configurationStorageHandler.store(account);
                    messages.appEvent(StateManager.AppEvents.UserVerifiedOkToCreate, eventParameters);
                    break;
            }
        } else if (responseCode == 503) {
            messages.appEvent(RidpConnectionFailure, eventParameters); //TODO “We're sorry. The third-party service used to confirm your identity is currently unavailable. Please try again later. If you continue to receive this message after trying several times, please call DC Health Link customer service for assistance at 1-855-532-5464.”
        } else if (responseCode == 412) {
            WrongAnswersResponse wrongAnswersResponse = serviceManager.getParser().parseWrongAnswersResponse(responseBody);
            messages.appEvent(RidpWrongAnswersRecoverable, eventParameters.add("transactionId", wrongAnswersResponse.verificationResult.transactionId)); //TODO “You have not passed identity validation. To proceed please contact Experian at 1-866-578-5409, and provide them with reference number {transaction_id}.”
        } else if (responseCode == 403 || responseCode == 422) {
            messages.appEvent(RidpWrongAnswersLockout, eventParameters); //TODO “Experian was unable to confirm your identity based on the information you provided. You will need to complete your application at the DC Health Benefit Exchange Authority office at 1225 Eye St NW. Please call (202)715-7576 to set up an appointment..”
        } else {
            messages.appEvent(StateManager.AppEvents.Error, EventParameters.build().add("error_msg", "Bad Http response processing RidpService.verificationRequest"));
        }
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
