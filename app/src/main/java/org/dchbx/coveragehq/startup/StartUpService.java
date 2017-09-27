package org.dchbx.coveragehq.startup;

import org.dchbx.coveragehq.BrokerApplication;
import org.dchbx.coveragehq.ConnectionHandler;
import org.dchbx.coveragehq.CoverageConnection;
import org.dchbx.coveragehq.Events;
import org.dchbx.coveragehq.IConnectionHandler;
import org.dchbx.coveragehq.IServiceManager;
import org.dchbx.coveragehq.JsonParser;
import org.dchbx.coveragehq.Messages;
import org.dchbx.coveragehq.ServerConfiguration;
import org.dchbx.coveragehq.UrlHandler;
import org.dchbx.coveragehq.models.startup.Login;
import org.dchbx.coveragehq.models.startup.ResumeParameters;
import org.dchbx.coveragehq.models.startup.Status;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    private final IServiceManager serviceManager;
    Messages messages;

    public StartUpService(IServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        Messages messages = BrokerApplication.getBrokerApplication().getMessages(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.IvlLoginRequest loginRequest) throws Exception {
        EventParameters eventParameters = loginRequest.getEventParameters();

        ResumeParameters resumeParamters  = (ResumeParameters) eventParameters.getObject("LoginParameters", ResumeParameters.class);
        Login login = new Login();
        login.username = resumeParamters.email;
        login.password = resumeParamters.password;

        final UrlHandler urlHandler = serviceManager.getUrlHandler();
        UrlHandler.HttpRequest httpRequest = urlHandler.getLoginRequest(login);
        CoverageConnection coverageConnection = serviceManager.getCoverageConnection();
        ConnectionHandler connectionHandler = serviceManager.getConnectionHandler();
        connectionHandler.process(httpRequest, new IConnectionHandler.OnCompletion() {
            @Override
            public void onCompletion(IConnectionHandler.HttpResponse response) {
                if (response.getResponseCode() >= 200
                        && response.getResponseCode() < 300){
                    JsonParser parser = serviceManager.getParser();

                    ServerConfiguration serverConfiguration = serviceManager.getServerConfiguration();
                    org.dchbx.coveragehq.models.startup.LoginResponse loginResponse = parser.parseResumeLogin(response.getBody());
                    urlHandler.populateLinks(loginResponse._links);
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.ResumeApplication resumeApplication) throws Exception {
        EventParameters eventParameters = resumeApplication.getEventParameters();

        UrlHandler urlHandler = serviceManager.getUrlHandler();
        UrlHandler.HttpRequest httpRequest = urlHandler.getResumeRequest();
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
                            messages.appEvent(StateManager.AppEvents.StatusAppliedUqhp);
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

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.CheckOpenEnrollment checkOpenEnrollment) throws Exception {
        UrlHandler urlHandler = serviceManager.getUrlHandler();
        UrlHandler.HttpRequest openEnrollmentRequest = urlHandler.getOpenEnrollmentRequest();

        CoverageConnection coverageConnection = serviceManager.getCoverageConnection();
        ConnectionHandler connectionHandler = serviceManager.getConnectionHandler();
        connectionHandler.process(openEnrollmentRequest, new IConnectionHandler.OnCompletion() {
            @Override
            public void onCompletion(IConnectionHandler.HttpResponse response) {
                if (response.getResponseCode() >= 200
                    && response.getResponseCode() < 300){

                }
            }
        });
    }
}
