package org.dchbx.coveragehq;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.dchbx.coveragehq.models.Glossary;
import org.dchbx.coveragehq.statemachine.DialogBuilder;
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
public class GlossaryDialog extends BrokerAppCompatDialogFragment {
    private static final String TAG = "GlossaryDialog";
    public static StateManager.UiDialog uiDialog = new StateManager.UiDialog(GlossaryDialog.class, new DialogBuilder(){
        public BrokerAppCompatDialogFragment build(EventParameters eventParameters, BaseActivity activity) {
            GlossaryDialog glossaryDialog = new GlossaryDialog();
            Bundle bundle = new Bundle();
            eventParameters.initBundle(bundle);
            glossaryDialog.setArguments(bundle);
            try {
                glossaryDialog.show(activity.getSupportFragmentManager(), null);
            } catch (Throwable t){
                Log.e(TAG, t.getMessage());
                throw t;
            }
            return glossaryDialog;
        }
    });

    private View view;
    private Glossary.GlossaryItem glossaryItem;
    private Context context;

    public GlossaryDialog(){
        Log.d(TAG, "in GlossaryDialog ctor");
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetGlossaryItemResponse getGlossaryItemResponse){
        this.glossaryItem = getGlossaryItemResponse.getGlossaryItem();
        TextView glossaryItemText = (TextView) view.findViewById(R.id.glossaryItemText);
        glossaryItemText.setText(Html.fromHtml(glossaryItem.description));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        String term = arguments.getString("term");
        getMessages().getGlossaryItem(term);

        view = inflater.inflate(R.layout.glossary_item, container, false);
        //view.setBackgroundColor(Color.TRANSPARENT);
        ImageButton close = (ImageButton) view.findViewById(R.id.closeButton);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMessages().appEvent(StateManager.AppEvents.Close);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // initializes bases class, mainly for event bus.
        init();

        this.context = context;
    }

}
