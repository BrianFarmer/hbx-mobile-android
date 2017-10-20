package org.dchbx.coveragehq;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import org.dchbx.coveragehq.statemachine.StateManager;

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
public class GenericErrorHandlerActivity extends BaseActivity {
    private static String TAG = "DentalCoverageActivity";
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(GenericErrorHandlerActivity.class);

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Intent intent = getIntent();
        setContentView(R.layout.generic_error);
        TextView dentalCoverageContent = (TextView) findViewById(R.id.errorMessage);
        String errorContent = getString(R.string.generic_error_content);

        errorContent = errorContent.replace("{Body}", intent.getCharSequenceExtra("Body"));
        errorContent = errorContent.replace("{ResponseCode}", intent.getCharSequenceExtra("ResponseCode"));
        errorContent = errorContent.replace("{EventType}", intent.getCharSequenceExtra("EventType"));

        (dentalCoverageContent).setText(Html.fromHtml(errorContent));
        dentalCoverageContent.setMovementMethod(LinkMovementMethod.getInstance());
        configToolbar();
    }



    public void onClick(){

    }
}
