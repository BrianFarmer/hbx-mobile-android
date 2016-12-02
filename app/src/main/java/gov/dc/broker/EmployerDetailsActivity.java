package gov.dc.broker;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

import java.util.ArrayList;

import gov.dc.broker.models.brokeragency.BrokerClient;
import gov.dc.broker.models.brokeragency.ContactInfo;
import gov.dc.broker.models.brokeragency.PlanYear;

/**
 * Created by plast on 10/21/2016.
 */

public class EmployerDetailsActivity extends BrokerActivity {
    private static final String TAG = "EmployerDetailsActivity";
    public static final String BROKER_CLIENT_ID = "BrokerClientId";

    private final String INFO_TAB = "info_tab";
    private final String ROSTER_TAB = "roster_tab";
    private final String COSTS_TAB = "costs_tab";
    private final String PLANS_TAB = "plans_tab";


    private Toolbar toolbar;
    private int clientId;
    BrokerClient brokerClient;
    private FragmentTabHost tabHost;
    private LocalDate coverageYear;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.BrokerClient  brokerClientEvent) {
        brokerClient = brokerClientEvent.getBrokerClient();

        TextView textViewCompanyName = (TextView) findViewById(R.id.textViewCompanyName);
        textViewCompanyName.setText(brokerClient.employerName);

        Spinner spinnerCoverageYear = (Spinner) findViewById(R.id.spinnerCoverageYear);

        ArrayList<String> list = new ArrayList<>();

        // set coverage year to the lowsest playYearBegins in the planYears
        LocalDate initialCoverageYear = new LocalDate(2100, 1, 1);
        for (PlanYear planYear : brokerClient.planYears) {
            if (planYear.planYearBegins != null
                && planYear.planYearBegins.compareTo(initialCoverageYear) < 0){
                initialCoverageYear = planYear.planYearBegins;
            }
            list.add(String.format("%s - %s", Utilities.DateAsMonthYear(planYear.planYearBegins), Utilities.DateAsMonthYear(Utilities.calculateOneYearOut(planYear.planYearBegins))));
        }
        coverageYear = initialCoverageYear;


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        spinnerCoverageYear.setAdapter(dataAdapter);
        spinnerCoverageYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                coverageYear = brokerClient.planYears.get(pos).planYearBegins;
                getMessages().coverageYearChanged(coverageYear);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        TextView textViewEnrollmentStatus = (TextView) findViewById(R.id.textViewEnrollmentStatus);
        PlanYear planYear = brokerClient.planYears.get(0);
        LocalDate today = new LocalDate();
        if (BrokerUtilities.isInOpenEnrollment(planYear, today)) {
            if (BrokerUtilities.isAlerted(planYear)){
                textViewEnrollmentStatus.setText(R.string.minimum_not_met);
                textViewEnrollmentStatus.setTextColor(ContextCompat.getColor(this, R.color.alertColor));
            } else {
                textViewEnrollmentStatus.setText(R.string.minimum_met);
                textViewEnrollmentStatus.setTextColor(ContextCompat.getColor(this, R.color.open_enrollment_minimum_met));
            }
        } else {
            if (planYear.renewalInProgress){
                textViewEnrollmentStatus.setText(R.string.renewal_in_progress);
                textViewEnrollmentStatus.setTextColor(ContextCompat.getColor(this, R.color.in_renewal));
            } else {
                textViewEnrollmentStatus.setText(R.string.all_other_clients);
                textViewEnrollmentStatus.setTextColor(ContextCompat.getColor(this, R.color.textgray));
            }
        }
        configButtons();
        configToolbar();
    }

    private void configToolbar() {
        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.app_header);
        toolbar.setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void configButtons(){
        if (brokerClient == null){
            return;
        }

        ContactInfo curContactInfo = brokerClient.contactInfo.get(0);

        ImageButton emailButton = (ImageButton)findViewById(R.id.imageButtonEmail);
        if (BrokerUtilities.anyEmailAddresses(brokerClient)) {
            emailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactDialog contactDialog = ContactDialog.build(brokerClient, ContactListAdapter.ListType.Email);
                    contactDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
                    contactDialog.show(getSupportFragmentManager(), "ContactDialog");
                }
            });
        } else {
            emailButton.setEnabled(false);
        }
        ImageButton chatButton = (ImageButton) findViewById(R.id.imageButtonChat);
        if (BrokerUtilities.anyMobileNumbers(brokerClient)) {
            chatButton.setEnabled(true);
            chatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactDialog contactDialog = ContactDialog.build(brokerClient, ContactListAdapter.ListType.Chat);
                    contactDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
                    contactDialog.show(getSupportFragmentManager(), "ContactDialog");
                }
            });
        } else {
            chatButton.setEnabled(false);
        }

        ImageButton phoneButton = (ImageButton) findViewById(R.id.imageButtonPhone);
        if (BrokerUtilities.anyPhoneNumbers(brokerClient)) {
            phoneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactDialog contactDialog = ContactDialog.build(brokerClient, ContactListAdapter.ListType.Phone);
                    contactDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
                    contactDialog.show(getSupportFragmentManager(), "ContactDialog");
                }
            });
        } else {
            phoneButton.setEnabled(false);
        }
        ImageButton locationButton = (ImageButton) findViewById(R.id.imageButtonLocation);
        if (BrokerUtilities.anyAddresses(brokerClient)) {
            locationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactDialog contactDialog = ContactDialog.build(brokerClient, ContactListAdapter.ListType.Directions);
                    contactDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
                    contactDialog.show(getSupportFragmentManager(), "ContactDialog");
                }
            });
        } else {
            locationButton.setEnabled(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (brokerClient == null) {
            Intent intent = getIntent();
            clientId = intent.getIntExtra(BROKER_CLIENT_ID, -1);
            if (clientId == -1) {
                Log.e(TAG, "onCreate: no client id found in intent");
                return;
            }

            getMessages().getEmployer(clientId);
        }

        LayoutInflater inflater = getLayoutInflater();
        setContentView(R.layout.employer_details);
        tabHost = (FragmentTabHost)findViewById(R.id.tabhost);
        tabHost.setup(this, getSupportFragmentManager(), R.id.fragment_content);

        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.info_tab_normal);
        tabHost.addTab(
                tabHost.newTabSpec(INFO_TAB)
                        .setIndicator(createTabIndicator(inflater, tabHost, R.string.info_tab_name, R.drawable.info_tab_states, true)),
                        InfoFragment.class, null);
        tabHost.addTab(
                tabHost.newTabSpec(ROSTER_TAB)
                .setIndicator(createTabIndicator(inflater, tabHost, R.string.roster_tab_name, R.drawable.roster_tab_states, false)),
                RosterFragment.class, null);
        tabHost.addTab(
                tabHost.newTabSpec(COSTS_TAB)
                    .setIndicator(createTabIndicator(inflater, tabHost, R.string.costs_tab_name, R.drawable.costs_tab_states, false)),
                CostsFragment.class, null);
        tabHost.addTab(
                tabHost.newTabSpec(PLANS_TAB)
                    .setIndicator(createTabIndicator(inflater, tabHost, R.string.plans_tab_name, R.drawable.plans_tab_states, false)),
                PlansFragment.class, null);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                selectedTabChanged(tabId);
            }
        });
    }

    private void selectedTabChanged(String tabId) {
        int currentTab = tabHost.getCurrentTab();
        TabWidget tabWidget = tabHost.getTabWidget();
        int unselectedTabColor = ContextCompat.getColor(this, R.color.unselected_tab_color);
        int selectedTabColor = ContextCompat.getColor(this, R.color.selected_tab_color);
        for(int i = 0; i < 4; i ++){
            View tab = tabWidget.getChildTabViewAt(i);
            TextView tabTitle = (TextView) tab.findViewById(R.id.tabtitle);
            if (i != currentTab) {
                tab.setBackgroundColor(unselectedTabColor);
                tabTitle.setTextColor(selectedTabColor);
            } else {
                tab.setBackgroundColor(selectedTabColor);
                tabTitle.setTextColor(unselectedTabColor);
            }
        }
    }

    public View createTabIndicator(LayoutInflater inflater, FragmentTabHost tabHost, int textResource, int iconResource, boolean selected) {
        View tabIndicator = inflater.inflate(R.layout.tab_indicator, tabHost.getTabWidget(), false);
        TextView tabTitle = (TextView) tabIndicator.findViewById(R.id.tabtitle);
        tabTitle.setText(textResource);
        ImageView tabImage = (ImageView) tabIndicator.findViewById(R.id.tabicon);
        tabImage.setImageResource(iconResource);

        if (selected){
            tabIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.selected_tab_color));
            tabTitle.setTextColor(ContextCompat.getColor(this, R.color.unselected_tab_color));
        }else {
            tabIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.unselected_tab_color));
            tabTitle.setTextColor(ContextCompat.getColor(this, R.color.selected_tab_color));
        }
        return tabIndicator;
    }

    public LocalDate getCoverageYear() {
        return coverageYear;
    }
}
