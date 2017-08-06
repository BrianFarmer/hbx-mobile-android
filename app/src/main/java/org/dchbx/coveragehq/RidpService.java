package org.dchbx.coveragehq;

import android.util.Log;

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
import org.dchbx.coveragehq.models.ridp.SignUp.Person;
import org.dchbx.coveragehq.models.ridp.SignUp.SignUp;
import org.dchbx.coveragehq.models.ridp.SignUp.SignUpResponse;
import org.dchbx.coveragehq.models.ridp.VerifiyIdentityResponse;
import org.dchbx.coveragehq.models.ridp.VerifyIdentity;
import org.dchbx.coveragehq.models.ridp.VerifyIdentityPerson;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;

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
    private final ServiceManager serviceManager;
    private Messages messages;

    public RidpService(ServiceManager serviceManager) {
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
        address.address.addressLine2 = account.address.address2;
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
            UrlHandler urlHandler = serviceManager.getUrlHandler();
            ConnectionHandler connectionHandler = serviceManager.getConnectionHandler();
            ConfigurationStorageHandler storageHandler = serviceManager.getConfigurationStorageHandler();
            VerifiyIdentityResponse verifiyIdentityResponse = storageHandler.readVerifiyIdentityResponse();
            org.dchbx.coveragehq.models.account.Account account = storageHandler.readAccount();
            SignUp signUp = buildSignUp(verifiyIdentityResponse, account);

            UrlHandler.HttpRequest request = urlHandler.getCreateAccount(signUp);
            IConnectionHandler.HttpResponse response = connectionHandler.process(request);
            if (response.getResponseCode() == 201){
                JsonParser parser = serviceManager.getParser();
                SignUpResponse signUpResponse = parser.parseSignUpResponse(response.getBody());
                storageHandler.store(signUpResponse);
                if (signUpResponse.error != null){
                    if (signUpResponse.error.type.compareTo("userHasActiveMedicaid") == 0){
                        BrokerWorker.eventBus.post(new Events.AppEvent(StateManager.AppEvents.SignUpUserInAceds, signUpResponse.error.message));
                    } else {
                        BrokerWorker.eventBus.post(new Events.AppEvent(StateManager.AppEvents.Error, signUpResponse.error.message));
                    }
                } else {
                    BrokerWorker.eventBus.post(new Events.AppEvent(StateManager.AppEvents.SignUpSuccessful, null));
                }
            } else {
                BrokerWorker.eventBus.post(new Events.Error("Bad Http response processing RidpService.signUp", "Events.GetCreateAccountInfo"));
            }
        } catch (Exception e) {
            Log.e(TAG, "exception processing http request");
            BrokerWorker.eventBus.post(new Events.Error("Error processing RidpService.signUp", e.getMessage()));
        }
    }

    private SignUp buildSignUp(VerifiyIdentityResponse verifiyIdentityResponse, Account account) {
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
    public void doThis(Events.GetVerificationResponse verificationResponse){
        try {
            Log.d(TAG, "in RidpService.doThis(Events(GetVerificationResponse)");

            UrlHandler urlHandler = serviceManager.getUrlHandler();
            ConnectionHandler connectionHandler = serviceManager.getConnectionHandler();
            ConfigurationStorageHandler storageHandler = serviceManager.getConfigurationStorageHandler();
            org.dchbx.coveragehq.models.account.Account account = storageHandler.readAccount();
            VerifyIdentity verifyIdentity = veriftyIdentityFromAccount(account);

            UrlHandler.HttpRequest request = urlHandler.getRidpVerificationParameters(verifyIdentity);
            try {
                IConnectionHandler.HttpResponse response = connectionHandler.process(request);
                if (response.getResponseCode() == 200) {
                    JsonParser parser = serviceManager.getParser();
                    Questions questions = parser.parseRidpQuestions(response.getBody());
                    storageHandler.store(questions);
                    messages.appEvent(StateManager.AppEvents.GetQuestionsOperationComplete);
                }
            } catch (Exception e) {
                Log.e(TAG, "exception processing http request");
                messages.appEvent(StateManager.AppEvents.ServiceErrorHappened, "Error getting RIDP questions");
            }
        } catch (Exception e){
            messages.error("Exception getting Experian questions.", e.getMessage());
        }
    }

    public void updateAnswers(Answers answers) {
        ConfigurationStorageHandler storageHandler = serviceManager.getConfigurationStorageHandler();
        storageHandler.store(answers);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.VerifyUser verificationResponse){
        Log.d(TAG, "in RidpService.verificationRequest");

        UrlHandler urlHandler = serviceManager.getUrlHandler();
        ConnectionHandler connectionHandler = serviceManager.getConnectionHandler();
        ConfigurationStorageHandler storageHandler = serviceManager.getConfigurationStorageHandler();
        Answers answers = storageHandler.readAnswers();

        UrlHandler.HttpRequest request = urlHandler.getAnswersRequest(answers);
        try {
            IConnectionHandler.HttpResponse response = connectionHandler.process(request);
            if (response.getResponseCode() == 200){
                JsonParser parser = serviceManager.getParser();
                VerifiyIdentityResponse verifiyIdentityResponse = parser.parseVerificationResponse(response.getBody());
                storageHandler.store(verifiyIdentityResponse);
                switch (determineState(verifiyIdentityResponse)){
                    case InRoster:
                        messages.appEvent(StateManager.AppEvents.UserVerifiedSsnWithEmployer);
                        break;
                    case InEnroll:
                        messages.appEvent(StateManager.AppEvents.UserVerifiedFoundYou);
                        break;
                    case OkToCreateAccount:
                        messages.appEvent(StateManager.AppEvents.UserVerifiedOkToCreate);
                        break;
                }
            } else {
                BrokerWorker.eventBus.post(new Events.Error("Bad Http response processing RidpService.verificationRequest", "Events.GetCreateAccountInfo"));
            }
        } catch (Exception e) {
            Log.e(TAG, "exception processing http request");
            BrokerWorker.eventBus.post(new Events.Error("Error processing RidpService.verificationRequest", "Events.GetCreateAccountInfo"));
        }
    }

    private VerifiyIdentityResponseStates determineState(VerifiyIdentityResponse verifiyIdentityResponse) {
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

    public VerifiyIdentityResponse getVerificationResponse() {
        ConfigurationStorageHandler configurationStorageHandler = serviceManager.getConfigurationStorageHandler();
        return configurationStorageHandler.readVerifiyIdentityResponse();
    }

    public static class QuestionsAndAnswers {
        Questions questions;
        Answers answers;
    }

    public QuestionsAndAnswers getRidpQuestions() {
        ConfigurationStorageHandler storageHandler = serviceManager.getConfigurationStorageHandler();
        Questions questions = storageHandler.readQuestions();
        if (questions == null){
            return null;
        }
        Answers answers = storageHandler.readAnswers();
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
}
