package org.dchbx.coveragehq;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import org.dchbx.coveragehq.statemachine.StateInfoBase;
import org.dchbx.coveragehq.statemachine.StateMachine;
import org.dchbx.coveragehq.statemachine.StateManager;

import java.util.ArrayDeque;

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
public class StateInfo extends BaseActivity {

    private static boolean shown;

    public StateInfo(){
    }

    public static boolean getShown() {
        return shown;
    }

    public void onCreate(Bundle bundle){
        super.onCreate(bundle);

        shown = true;

        setContentView(R.layout.state_info);
        WebView webView = (WebView) findViewById(R.id.webView);
        Button closeButton = (Button) findViewById(R.id.closeButton);

        ServiceManager serviceManager = ServiceManager.getServiceManager();
        StateManager stateManager = serviceManager.getStateManager();
        StateMachine stateMachine = stateManager.getStateMachine();
        ArrayDeque<StateInfoBase> statesStack = stateMachine.getStatesStack();

        StringBuilder builder = new StringBuilder();
        builder.append("<html>\n" +
                "\n" +
                "   <head>\n" +
                "      <title>HTML Table Header</title>\n" +
                "   </head>\n" +
                "\t\n" +
                "   <body>\n" +
                "      <table>\n" +
                "         <tr>\n" +
                "            <th>State</th>\n" +
                "            <th>Event</th>\n" +
                "         </tr>\n");
        for (StateInfoBase stateInfoBase : statesStack) {
        builder.append("         <tr>\n" +
                "            <td>" + stateInfoBase.getState().name() + "</td>\n" +
                "            <td>" + stateInfoBase.getEvent().name() + "</td>\n" +
                "         </tr>\n");
        };

        builder.append("      </table>\n" +
                "   </body>\n" +
                "   ");

        String html = builder.toString();
        webView.loadDataWithBaseURL("", html, "text/html", "utf-8", "");

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    shown = false;
                    StateInfo.this.finish();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
    }

}
