package org.dchbx.coveragehq.startup;

import android.content.Intent;
import android.util.Log;

import com.google.gson.GsonBuilder;

import org.dchbx.coveragehq.BrokerApplication;
import org.dchbx.coveragehq.ConfigurationStorageHandler;
import org.dchbx.coveragehq.ConnectionHandler;
import org.dchbx.coveragehq.CoverageConnection;
import org.dchbx.coveragehq.DateTimeDeserializer;
import org.dchbx.coveragehq.Events;
import org.dchbx.coveragehq.IConnectionHandler;
import org.dchbx.coveragehq.IServiceManager;
import org.dchbx.coveragehq.JsonParser;
import org.dchbx.coveragehq.LocalDateSerializer;
import org.dchbx.coveragehq.LocalTimeDeserializer;
import org.dchbx.coveragehq.Messages;
import org.dchbx.coveragehq.UrlHandler;
import org.dchbx.coveragehq.financialeligibility.FinancialEligibilityService;
import org.dchbx.coveragehq.models.Errors.ServerError;
import org.dchbx.coveragehq.models.account.Account;
import org.dchbx.coveragehq.models.ridp.SignUp.SignUpResponse;
import org.dchbx.coveragehq.models.startup.EffectiveDate;
import org.dchbx.coveragehq.models.startup.Login;
import org.dchbx.coveragehq.models.startup.OpenEnrollmentStatus;
import org.dchbx.coveragehq.models.startup.ResumeParameters;
import org.dchbx.coveragehq.models.startup.Status;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

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
public class StartUpService {
    private static String TAG = "MobilePasswordActivity";
    public static String EffectiveDate = "EffectiveDate";

    private final IServiceManager serviceManager;
    Messages messages;

    public StartUpService(IServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        messages = BrokerApplication.getBrokerApplication().getMessages(this);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void doThis(Events.ClearPIIRequest clearPIIRequest) throws Exception {
        messages.appEvent(StateManager.AppEvents.ClearedPII);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void doThis(final Events.IvlLoginRequest loginRequest) throws Exception {
        final EventParameters eventParameters = loginRequest.getEventParameters();

        final ResumeParameters resumeParamters  = (ResumeParameters) eventParameters.getObject("LoginParameters", ResumeParameters.class);
        final Login login = new Login();
        login.username = resumeParamters.email;
        login.password = resumeParamters.password;

        final UrlHandler urlHandler = serviceManager.getUrlHandler();
        UrlHandler.HttpRequest httpRequest = urlHandler.getLoginRequest(login);
        ConnectionHandler connectionHandler = serviceManager.getConnectionHandler();
        connectionHandler.process(httpRequest, new IConnectionHandler.OnCompletion() {
            @Override
            public void onCompletion(IConnectionHandler.HttpResponse response) throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException {
                if (response.getResponseCode() >= 200
                        && response.getResponseCode() < 300){
                    JsonParser parser = serviceManager.getParser();
                    SignUpResponse signUpResponse = parser.parseSignUpResponse(response.getBody());
                    urlHandler.populateLinks(signUpResponse.links);
                    ConfigurationStorageHandler configurationStorageHandler = serviceManager.getConfigurationStorageHandler();
                    configurationStorageHandler.setAccountName(signUpResponse.uuid);
                    Account account = configurationStorageHandler.readAccount();
                    if (account == null
                            || account.getEmailAddress() == null
                            || !account.getEmailAddress().toLowerCase().equals(resumeParamters.email.toLowerCase())){
                        account = null;
                        configurationStorageHandler.clear();
                    }
                    messages.appEvent(StateManager.AppEvents.IndividualLoggedIn);
                } else {
                    JsonParser parser = serviceManager.getParser();
                    ServerError error = parser.parseError(response.getBody());
                    messages.appEvent(StateManager.AppEvents.ServerError, eventParameters.add(FinancialEligibilityService.ServerError, error));
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void doThis(Events.GetEffectiveDate getEffectiveDate) throws Exception {
        try {
            final UrlHandler urlHandler = serviceManager.getUrlHandler();
            UrlHandler.HttpRequest httpRequest = urlHandler.getEffectiveDateRequest();
            CoverageConnection coverageConnection = serviceManager.getCoverageConnection();
            ConnectionHandler connectionHandler = serviceManager.getConnectionHandler();
            connectionHandler.process(httpRequest, new IConnectionHandler.OnCompletion() {
                @Override
                public void onCompletion(IConnectionHandler.HttpResponse response) throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeyException, InvalidKeySpecException {
                    if (response.getResponseCode() >= 200
                            && response.getResponseCode() < 300) {
                        JsonParser parser = serviceManager.getParser();
                        EffectiveDate effectiveDate = parser.parseEffectiveDate(response.getBody());
                        serviceManager.getConfigurationStorageHandler().storeEffectiveDate(effectiveDate);
                        messages.appEvent(StateManager.AppEvents.ReceivedEffectiveDate, EventParameters.build().add(EffectiveDate, effectiveDate));
                    } else {
                        messages.appEvent(StateManager.AppEvents.Error, EventParameters.build()
                                .add("ResponseCode", response.getResponseCode())
                                .add("Body", response.getBody())
                                .add("EventType", "Events.GetEffectiveDate"));
                    }
                }
            });
        } catch (Throwable t){
            Log.e(TAG, "throwable: " + t.getMessage());
            throw  t;
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void doThis(Events.ResumeApplication resumeApplication) throws Exception {
        final EventParameters eventParameters = resumeApplication.getEventParameters();

        UrlHandler urlHandler = serviceManager.getUrlHandler();
        UrlHandler.HttpRequest httpRequest = urlHandler.getResumeRequest(serviceManager.getConfigurationStorageHandler().readEffectiveDate().effectiveDate);
        CoverageConnection coverageConnection = serviceManager.getCoverageConnection();
        ConnectionHandler connectionHandler = serviceManager.getConnectionHandler();
        connectionHandler.process(httpRequest, new IConnectionHandler.OnCompletion() {
            @Override
            public void onCompletion(IConnectionHandler.HttpResponse response) {
                if (response.getResponseCode() >= 200
                    && response.getResponseCode() < 300){
                    JsonParser parser = serviceManager.getParser();
                    Status status = parser.parseStatus(response.getBody());

                    switch (status.status){
                        case "applied_uqhp":
                                messages.appEvent(StateManager.AppEvents.StatusAppliedUqhp, eventParameters.add("Status", status));
                            break;
                        case "enrolled":
                            messages.appEvent(StateManager.AppEvents.StatusEnrolled);
                            break;
                        case "applying":
                            messages.appEvent(StateManager.AppEvents.StatusApplying);
                            break;
                        case "enrolling_uqhp":
                            messages.appEvent(StateManager.AppEvents.StatusEnrollingUqhp);
                            break;
                    }
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void doThis(Events.CheckOpenEnrollment checkOpenEnrollment) throws Exception {
        UrlHandler urlHandler = serviceManager.getUrlHandler();
        UrlHandler.HttpRequest openEnrollmentRequest = urlHandler.getOpenEnrollmentRequest();

        CoverageConnection coverageConnection = serviceManager.getCoverageConnection();
        ConnectionHandler connectionHandler = serviceManager.getConnectionHandler();
        connectionHandler.process(openEnrollmentRequest, new IConnectionHandler.OnCompletion() {
            @Override
            public void onCompletion(IConnectionHandler.HttpResponse response) throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeyException, InvalidKeySpecException {
                if (response.getResponseCode() >= 200
                    && response.getResponseCode() < 300){
                    JsonParser parser = serviceManager.getParser();
                    OpenEnrollmentStatus openEnrollmentStatus = parser.parseOpenEnrollment(response.getBody());
                    serviceManager.getConfigurationStorageHandler().storeOpenEnrollmentStatus(openEnrollmentStatus);
                    if (openEnrollmentStatus.status.equals("enrollment_closed")){
                        messages.appEvent(StateManager.AppEvents.OpenEnrollmentClosed);
                    } else {
                        messages.appEvent(StateManager.AppEvents.InOpenEnrollment);
                    }
                }
            }
        });
    }

    public static EffectiveDate getEffectiveDate(Intent intent) {
        String jsonString = intent.getStringExtra(EffectiveDate);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateSerializer());
        gsonBuilder.registerTypeAdapter(LocalTime.class, new LocalTimeDeserializer());
        return gsonBuilder.create().fromJson(jsonString, EffectiveDate.class);
    }
}
