package org.dchbx.coveragehq.financialeligibility;

import android.util.Log;

import org.dchbx.coveragehq.ApplicationUtilities;
import org.dchbx.coveragehq.BrokerApplication;
import org.dchbx.coveragehq.ConnectionHandler;
import org.dchbx.coveragehq.Events;
import org.dchbx.coveragehq.IConnectionHandler;
import org.dchbx.coveragehq.JsonParser;
import org.dchbx.coveragehq.Messages;
import org.dchbx.coveragehq.ServiceManager;
import org.dchbx.coveragehq.UrlHandler;
import org.dchbx.coveragehq.models.fe.FinancialAssistanceApplication;
import org.dchbx.coveragehq.models.fe.Schema;
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
public class FinancialEligibilityService {
    private static String TAG = "FinancialEligibility";

    private final Messages messages;
    private final ServiceManager serviceManager;
    private Schema schema;

    public FinancialEligibilityService(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        messages = BrokerApplication.getBrokerApplication().getMessages(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetApplicationPerson getApplicationPerson){
        String eaPersonId = getApplicationPerson.getEaPersonId();
        if (eaPersonId == null){
            messages.getFinancialApplicationPersonResponse(ApplicationUtilities.getNewPerson());
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetFinancialAssistanceApplication getFinancialAssistanceApplication){
        messages.getFinancialAssistanceApplicationResponse(new FinancialAssistanceApplication());
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.GetFinancialEligibilityJson getFinancialEligibilityJson) throws Exception {
        Log.d(TAG, "in FinancialEligibilityService.doThis(Events.GetFinancialEligibilityJson)");
        if (schema == null){
            UrlHandler urlHandler = serviceManager.getUrlHandler();
            ConnectionHandler connectionHandler = serviceManager.getConnectionHandler();

            UrlHandler.HttpRequest request = urlHandler.getFinancialEligibilityJson();
            IConnectionHandler.HttpResponse response = connectionHandler.process(request);
            connectionHandler.process(request, new IConnectionHandler.OnCompletion(){
                public void onCompletion(IConnectionHandler.HttpResponse response){
                    if (response.getResponseCode() == 200){
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
}
