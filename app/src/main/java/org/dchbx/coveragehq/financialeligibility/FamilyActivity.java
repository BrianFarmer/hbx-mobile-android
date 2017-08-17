package org.dchbx.coveragehq.financialeligibility;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.dchbx.coveragehq.BaseActivity;
import org.dchbx.coveragehq.Events;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.models.fe.FinancialAssistanceApplication;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by plast on 5/4/2017.
 */

public class FamilyActivity extends BaseActivity {
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(FamilyActivity.class);

    private FinancialAssistanceApplication financialAssistanceApplication;
    private FamilyAdapter familyAdapter;

    public FamilyActivity(){
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.family);
        configToolbar();
        getMessages().getFinancialAssistanceApplication();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetFinancialAssistanceApplicationResponse getFinancialAssistanceResponse) throws Exception {
        financialAssistanceApplication = getFinancialAssistanceResponse.getFinancialAssistanceApplication();
        populate();
    }

    protected void populate() {
        familyAdapter = new org.dchbx.coveragehq.financialeligibility.FamilyAdapter(this, financialAssistanceApplication);
        ListView memberListView = (ListView) findViewById(R.id.memberList);
        memberListView.setAdapter(familyAdapter);

        TextView shouldInclude = (TextView)findViewById(R.id.shouldInclude);
        shouldInclude.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMessages().appEvent(StateManager.AppEvents.ShowGlossaryItem, EventParameters.build().add("term", "family"));
            }
        });
        ImageView shouldIncludeImage = (ImageView) findViewById(R.id.shouldIncludeImage);
        shouldIncludeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMessages().appEvent(StateManager.AppEvents.ShowGlossaryItem, EventParameters.build().add("term", "family"));
            }
        });
        Button addFamilyMember = (Button) findViewById(R.id.addFamilyMember);
        addFamilyMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMessages().appEvent(StateManager.AppEvents.AddFamilyMember);
            }
        });

        Button continueButton = (Button) findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMessages().appEvent(StateManager.AppEvents.AddFamilyMember);
            }
        });
    }
}
