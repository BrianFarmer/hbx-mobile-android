package gov.dc.broker;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.wdullaer.swipeactionadapter.SwipeListArrayAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EmployerAdapter extends SwipeListArrayAdapter {
    private static final String TAG = "EmployerAdapter";

    private MainActivity mainActivity;
    private EmployerList employerList;
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<ItemWrapperBase> wrapped_items = new ArrayList<ItemWrapperBase>();

    private ArrayList<OpenEnrollmentAlertedWrapper> alertedItems = new ArrayList<OpenEnrollmentAlertedWrapper>();
    private ArrayList<OpenEnrollmentNotAlertedWrapper> notAlertedItems = new ArrayList<OpenEnrollmentNotAlertedWrapper>();
    private ArrayList<RenewalWrapper> renewalItems = new ArrayList<RenewalWrapper>();
    private ArrayList<OtherWrapper> otherItems = new ArrayList<OtherWrapper>();
    private OpenEnrollmentHeader openEnrollmentHeader;
    private OpenEnrollmentAlertHeader openEnrollmentAlertHeader;
    private OpenEnrollmentNotAlertedHeader openEnrollmentNotAlertedHeader;
    private RenewalHeader renewalHeader;
    private OtherItemsHeader otherItemsHeader;

    private boolean openEnrollmentState = true;
    private boolean renewalState = false;
    private boolean othersState = false;
    public View notAlerted;

    public EmployerAdapter(MainActivity mainActivity, Context context, EmployerList employerList){
        this.mainActivity = mainActivity;
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.employerList = employerList;

        openEnrollmentHeader = new OpenEnrollmentHeader(this);
        openEnrollmentAlertHeader = new OpenEnrollmentAlertHeader(this);
        openEnrollmentNotAlertedHeader = new OpenEnrollmentNotAlertedHeader(this);
        renewalHeader = new RenewalHeader(this);
        otherItemsHeader = new OtherItemsHeader(this);

        Date now = Calendar.getInstance().getTime();
        int i = 0;
        for (BrokerClient brokerclient : employerList.brokerClients) {
            if (brokerclient.isInOpenEnrollment(now)
                && brokerclient.isAlerted()){
                alertedItems.add(new OpenEnrollmentAlertedWrapper(brokerclient, i));
            } else if (brokerclient.isInOpenEnrollment(now)
                && !brokerclient.isAlerted()){
                notAlertedItems.add(new OpenEnrollmentNotAlertedWrapper(brokerclient, i));
            } else if (brokerclient.renewalInProgress){
                renewalItems.add(new RenewalWrapper(brokerclient, i));
            } else {
                otherItems.add(new OtherWrapper(brokerclient, i));
            }
            i ++;
        }

        if (alertedItems.size() == 0
                && notAlertedItems.size() == 0){
            openEnrollmentState = false;
        }

        updateWrappedItems();
    }

    private void updateWrappedItems(){
        wrapped_items.clear();
        wrapped_items.add(openEnrollmentHeader);
        if (openEnrollmentState
            && (alertedItems.size() > 0
                || notAlertedItems.size() > 0)) {
            wrapped_items.add(openEnrollmentAlertHeader);
            wrapped_items.addAll(alertedItems);
            wrapped_items.add(openEnrollmentNotAlertedHeader);
            wrapped_items.addAll(notAlertedItems);
        }
        wrapped_items.add(renewalHeader);
        if (renewalState && renewalItems.size() > 0) {
            wrapped_items.addAll(renewalItems);
        }
        wrapped_items.add(otherItemsHeader);
        if (othersState && otherItems.size() > 0) {
            wrapped_items.addAll(otherItems);
        }
    }

    @Override
    public int getViewTypeCount(){
        return  1;
    }

    @Override
    public int getItemViewType(int position) {
        return wrapped_items.get(position).getType();
    }

    @Override
    public int getCount() {
        return wrapped_items.size();
    }

    @Override
    public Object getItem(int position) {
        return wrapped_items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return wrapped_items.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemWrapperBase itemWrapperBase = wrapped_items.get(position);
        return itemWrapperBase.getView(convertView, parent, mainActivity, inflater);
    }

    public ArrayList<OpenEnrollmentAlertedWrapper> getAlertedItems() {
        return alertedItems;
    }

    public ArrayList<OpenEnrollmentNotAlertedWrapper> getNotAlertedItems() {
        return notAlertedItems;
    }

    public ArrayList<RenewalWrapper> getRenewalItems() {
        return renewalItems;
    }

    public ArrayList<OtherWrapper> getOtherItems() {
        return otherItems;
    }

    public boolean isOpenEnrollmentOpen() {
        return openEnrollmentState;
    }

    public boolean isRenewalOpen() {
        return renewalState;
    }

    public boolean isOthersOpen() {
        return othersState;
    }

    public void toggleOthers() {
        othersState = !othersState;
        updateWrappedItems();
        notifyDataSetChanged();
    }

    public void toggleRenewals() {
        renewalState = !renewalState;
        updateWrappedItems();
        notifyDataSetChanged();
    }

    public void toggleOpenEnrollments() {
        openEnrollmentState = !openEnrollmentState;
        updateWrappedItems();
        notifyDataSetChanged();
    }

    @Override
    public boolean isItemSwipeable(int position) {
        return wrapped_items.get(position).isSwipeable();
    }
}

abstract class ItemWrapperBase {
    private static long nextId = 0;
    private final long id;
    public abstract int getType();

    protected ItemWrapperBase(){
        id = nextId ;
        nextId = nextId + 1;
    }

    public boolean isSwipeable(){
        return false;
    }

    public long getId() {
        return id;
    }

    public abstract View getView(View convertView, ViewGroup parent, MainActivity mainActivity, LayoutInflater inflater);
}

class OpenEnrollmentHeader extends ItemWrapperBase {

    private final EmployerAdapter employerAdapter;

    public OpenEnrollmentHeader(EmployerAdapter employerAdapter) {
        this.employerAdapter = employerAdapter;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public View getView(View convertView, ViewGroup parent, MainActivity mainActivity, LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.open_enrollment_header, parent, false);
        Button openButton = (Button)view.findViewById(R.id.buttonCount);
        ImageView circleImageView = (ImageView)view.findViewById(R.id.imageViewCircle);
        if (employerAdapter.isOpenEnrollmentOpen()){
            openButton.setText("");
            circleImageView.setImageResource(R.drawable.uparrow);
        } else {
            openButton.setText(String.valueOf(employerAdapter.getAlertedItems().size() + employerAdapter.getNotAlertedItems().size()));
            circleImageView.setImageResource(R.drawable.circle);
        }
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                employerAdapter.toggleOpenEnrollments();
            }
        });
        return view;
    }
}
class OpenEnrollmentAlertHeader extends ItemWrapperBase {
    private static final String VIEW_TYPE = "OpenEnrollmentAlertedHeaderView";
    private static final String TAG = "OpenEnrollmntAlertHdr";

    private final EmployerAdapter employerAdapter;

    public OpenEnrollmentAlertHeader(EmployerAdapter employerAdapter) {
        this.employerAdapter = employerAdapter;
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public View getView(View convertView, ViewGroup parent, MainActivity mainActivity, LayoutInflater inflater) {
        View view;
        Boolean createNewView = true;
        if (convertView != null){
            Object tagObject = convertView.getTag(R.id.view_type);
            if (tagObject != null){
                String tagString = (String) tagObject;
                if (tagString.equals(VIEW_TYPE)){
                    createNewView = false;
                }
            }
        }
        if (createNewView) {
            view = inflater.inflate(R.layout.open_enrollment_alerted_header, parent, false);
            view.setTag(R.id.view_type, VIEW_TYPE);
        } else {
            view = convertView;
        }
        return view;
    }
}

class OpenEnrollmentNotAlertedHeader extends ItemWrapperBase {
    private static final String VIEW_TYPE = "OpenEnrollmentNotAlertedHeaderView";

    private final EmployerAdapter employerAdapter;

    public OpenEnrollmentNotAlertedHeader(EmployerAdapter employerAdapter) {
        this.employerAdapter = employerAdapter;
    }

    @Override
    public int getType() {
        return 2;
    }

    @Override
    public View getView(View convertView, ViewGroup parent, MainActivity mainActivity, LayoutInflater inflater) {
        View view;
        Boolean createNewView = true;
        if (convertView != null){
            Object tagObject = convertView.getTag(R.id.view_type);
            if (tagObject != null){
                String tagString = (String) tagObject;
                if (tagString.equals(VIEW_TYPE)){
                    createNewView = false;
                }
            }
        }
        if (createNewView) {
            view = inflater.inflate(R.layout.open_enrollment_not_alerted_header, parent, false);
            view.setTag(R.id.view_type, VIEW_TYPE);
        } else {
            view = convertView;
        }

        employerAdapter.notAlerted = view;
        return view;
    }
}

class RenewalHeader extends ItemWrapperBase {

    private final EmployerAdapter employerAdapter;

    public RenewalHeader(EmployerAdapter employerAdapter) {
        this.employerAdapter = employerAdapter;
    }

    @Override
    public int getType() {
        return 3;
    }

    @Override
    public View getView(View convertView, ViewGroup parent, MainActivity mainActivity, LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.renewals_in_progress_header, parent, false);
        Button openButton = (Button)view.findViewById(R.id.buttonCount);
        ImageView circleImageView = (ImageView)view.findViewById(R.id.imageViewCircle);
        if (employerAdapter.isRenewalOpen()){
            openButton.setText("");
            circleImageView.setImageResource(R.drawable.uparrow);
        } else {
            openButton.setText(String.valueOf(employerAdapter.getRenewalItems().size()));
            circleImageView.setImageResource(R.drawable.circle);
        }
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                employerAdapter.toggleRenewals();
            }
        });
        if (!employerAdapter.isRenewalOpen()){
            View columnHeaders = view.findViewById(R.id.relativeLayoutColumnHeaders);
            columnHeaders.setVisibility(View.GONE);
        }
        return view;
    }
}

class OtherItemsHeader extends ItemWrapperBase {

    private final EmployerAdapter employerAdapter;

    public OtherItemsHeader(EmployerAdapter employerAdapter) {
        this.employerAdapter = employerAdapter;
    }

    @Override
    public int getType() {
        return 4;
    }

    @Override
    public View getView(View convertView, ViewGroup parent, final MainActivity mainActivity, LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.all_other_clients_header, parent, false);
        Button openButton = (Button)view.findViewById(R.id.buttonCount);
        ImageView circleImageView = (ImageView)view.findViewById(R.id.imageViewCircle);
        if (employerAdapter.isOthersOpen()){
            openButton.setText("");
            circleImageView.setImageResource(R.drawable.uparrow);
        } else {
            openButton.setText(String.valueOf(employerAdapter.getOtherItems().size()));
            circleImageView.setImageResource(R.drawable.circle);
        }
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                employerAdapter.toggleOthers();
            }
        });
        if (!employerAdapter.isOthersOpen()) {
            View columnHeaders = view.findViewById(R.id.relativeLayoutColumnHeaders);
            columnHeaders.setVisibility(View.GONE);
        }
        return view;
    }
}

abstract class BrokerClientWrapper extends ItemWrapperBase {

    protected final BrokerClient brokerClient;
    private final int objectIndex;

    public BrokerClientWrapper(BrokerClient brokerClient, int objectIndex) {
        this.brokerClient = brokerClient;
        this.objectIndex = objectIndex;
    }

    public int getObjectIndex() {
        return objectIndex;
    }
}

class OpenEnrollmentAlertedWrapper extends BrokerClientWrapper {
    private static final String TAG = "OpenEnrlmentAlertedWrpr";
    private static final String VIEW_TYPE = "OpenEnrollmentAlertedView";

    public OpenEnrollmentAlertedWrapper(BrokerClient brokerclient, int i) {
        super(brokerclient, i);
    }

    @Override
    public int getType() {
        return 5;
    }

    protected void fillOpenEnrollmentItem(View view, MainActivity mainActivity) {
        final MainActivity mainActivityFinal = mainActivity;
        TextView companyName = (TextView)view.findViewById(R.id.textViewCompanyName);
        companyName.setText(brokerClient.employerName);
        TextView employeesNeeded = (TextView)view.findViewById(R.id.textViewEmployessNeeded);
        employeesNeeded.setText(String.valueOf(brokerClient.getEmployessNeeded()));
        TextView daysLeft = (TextView)view.findViewById(R.id.textViewDaysLeft);
        daysLeft.setText(String.valueOf(brokerClient.getDaysLeft()));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainActivityFinal, ClientDetailsActivity.class);
                Log.d(TAG, "onClick: launching");
                intent.putExtra(ClientDetailsActivity.BROKER_CLIENT_ID, getObjectIndex());
                mainActivityFinal.startActivity(intent);
            }
        });
    }

    @Override
    public boolean isSwipeable() {
        return true;
    }

    @Override
    public View getView(View convertView, ViewGroup parent, MainActivity mainActivity, LayoutInflater inflater) {
        View view;
        Boolean createNewView = true;
        if (convertView != null){
            Object tagObject = convertView.getTag(R.id.view_type);
            if (tagObject != null){
                String tagString = (String) tagObject;
                if (tagString.equals(VIEW_TYPE)){
                    createNewView = false;
                }
            }
        }
        if (createNewView) {
            view = inflater.inflate(R.layout.open_enrollment_item, parent, false);
            view.setTag(R.id.view_type, VIEW_TYPE);
        } else {
            view = convertView;
        }
        fillOpenEnrollmentItem(view, mainActivity);
        return view;
    }
}

class OpenEnrollmentNotAlertedWrapper extends BrokerClientWrapper {
    private static final String TAG = "OpnEnrlmntNotAlertdWrpr";
    private static final String VIEW_TYPE = "OpenEnrollmentNotAlertedView";

    public OpenEnrollmentNotAlertedWrapper(BrokerClient brokerclient, int i) {
        super(brokerclient, i);
    }

    @Override
    public int getType() {
        return 6;
    }

    @Override
    public boolean isSwipeable() {
        return true;
    }

    protected void fillOpenEnrollmentItem(View view, MainActivity mainActivity) {
        final MainActivity mainActivityFinal = mainActivity;
        TextView companyName = (TextView)view.findViewById(R.id.textViewCompanyName);
        companyName.setText(brokerClient.employerName);
        TextView employeesNeeded = (TextView)view.findViewById(R.id.textViewEmployessNeeded);
        employeesNeeded.setText(String.valueOf(brokerClient.employeesEnrolled));
        TextView daysLeft = (TextView)view.findViewById(R.id.textViewDaysLeft);
        daysLeft.setText(String.valueOf(brokerClient.getDaysLeft()));
        View alertBar = view.findViewById(R.id.imageViewAlertBar);
        alertBar.setVisibility(View.GONE);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainActivityFinal, ClientDetailsActivity.class);
                Log.d(TAG, "onClick: launching");
                intent.putExtra(ClientDetailsActivity.BROKER_CLIENT_ID, getObjectIndex());
                mainActivityFinal.startActivity(intent);
            }
        });
    }

    @Override
    public View getView(View convertView, ViewGroup parent, MainActivity mainActivity, LayoutInflater inflater) {
        View view;
        Boolean createNewView = true;
        if (convertView != null){
            Object tagObject = convertView.getTag(R.id.view_type);
            if (tagObject != null){
                String tagString = (String) tagObject;
                if (tagString.equals(VIEW_TYPE)){
                    createNewView = false;
                }
            }
        }
        if (createNewView) {
            view = inflater.inflate(R.layout.open_enrollment_item, parent, false);
            view.setTag(R.id.view_type, VIEW_TYPE);
        } else {
            view = convertView;
        }
        fillOpenEnrollmentItem(view, mainActivity);
        return view;

    }
}

class RenewalWrapper extends BrokerClientWrapper {
    private static final String VIEW_TYPE = "RenewalView";
    private static final String TAG = "RenewalWrapper";

    public RenewalWrapper(BrokerClient brokerclient, int i) {
        super(brokerclient, i);
    }

    @Override
    public boolean isSwipeable() {
        return true;
    }

    @Override
    public int getType() {
        return 7;
    }

    private void fillRenewalInProgressItem(View view, MainActivity mainActivity) {
        final MainActivity mainActivityFinal = mainActivity;
        TextView companyName = (TextView)view.findViewById(R.id.textViewCompanyName);
        companyName.setText((brokerClient).employerName);
        TextView planYear = (TextView)view.findViewById(R.id.textViewPlanYear);
        CharSequence dateString = DateFormat.format("MMM yy", brokerClient.planYearBegins);
        planYear.setText(dateString);
        TextView daysLeft = (TextView)view.findViewById(R.id.textViewDaysLeft);
        daysLeft.setText(String.valueOf(brokerClient.getDaysLeft()));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainActivityFinal, ClientDetailsActivity.class);
                Log.d(TAG, "onClick: launching");
                intent.putExtra(ClientDetailsActivity.BROKER_CLIENT_ID, getObjectIndex());
                mainActivityFinal.startActivity(intent);
            }
        });
    }

    @Override
    public View getView(View convertView, ViewGroup parent, MainActivity mainActivity, LayoutInflater inflater) {
        View view;
        Boolean createNewView = true;
        if (convertView != null){
            Object tagObject = convertView.getTag(R.id.view_type);
            if (tagObject != null){
                String tagString = (String) tagObject;
                if (tagString.equals(VIEW_TYPE)){
                    createNewView = false;
                }
            }
        }
        if (createNewView) {
            view = inflater.inflate(R.layout.renewals_in_progress_item, parent, false);
            view.setTag(R.id.view_type, VIEW_TYPE);
        } else {
            view = convertView;
        }
        fillRenewalInProgressItem(view, mainActivity);
        return view;
    }
}

class OtherWrapper extends BrokerClientWrapper {
    private static final String VIEW_TYPE = "OtherView";
    private static final String TAG = "OtherWrapper";

    public OtherWrapper(BrokerClient brokerclient, int i) {
        super(brokerclient, i);
    }

    @Override
    public boolean isSwipeable() {
        return true;
    }

    @Override
    public int getType() {
        return 8;
    }

    private void fillOtherItem(View view, MainActivity mainActivity) {
        final MainActivity mainActivityFinal = mainActivity;
        TextView companyName = (TextView)view.findViewById(R.id.textViewCompanyName);
        companyName.setText((brokerClient).employerName);
        TextView planYear = (TextView)view.findViewById(R.id.textViewPlanYear);
        CharSequence dateString = DateFormat.format("MMM yy", brokerClient.planYearBegins);
        planYear.setText(dateString);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainActivityFinal, ClientDetailsActivity.class);
                Log.d(TAG, "onClick: launching");
                intent.putExtra(ClientDetailsActivity.BROKER_CLIENT_ID, getObjectIndex());
                mainActivityFinal.startActivity(intent);
            }
        });
    }

    @Override
    public View getView(View convertView, ViewGroup parent, MainActivity mainActivity, LayoutInflater inflater) {
        View view;
        Boolean createNewView = true;
        if (convertView != null){
            Object tagObject = convertView.getTag(R.id.view_type);
            if (tagObject != null){
                String tagString = (String) tagObject;
                if (tagString.equals(VIEW_TYPE)){
                    createNewView = false;
                }
            }
        }
        if (createNewView) {
            view = inflater.inflate(R.layout.all_other_clients_item, parent, false);
            view.setTag(R.id.view_type, VIEW_TYPE);
        } else {
            view = convertView;
        }
        fillOtherItem(view, mainActivity);
        return view;
    }
}


