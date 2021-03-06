package gov.dc.broker;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import gov.dc.broker.models.brokeragency.BrokerClient;
import gov.dc.broker.models.brokeragency.PlanYear;

public class EmployerAdapter extends BaseSwipeAdapter {
    private static final String TAG = "EmployerAdapter";

    private MainActivity mainActivity;
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<ItemWrapperBase> wrapped_items = new ArrayList<ItemWrapperBase>();

    private final List<BrokerClient> employerList;
    private final LocalDate coverageYear;
    private ArrayList<OpenEnrollmentAlertedWrapper> alertedItems = new ArrayList<OpenEnrollmentAlertedWrapper>();
    private ArrayList<OpenEnrollmentNotAlertedWrapper> notAlertedItems = new ArrayList<OpenEnrollmentNotAlertedWrapper>();
    private ArrayList<RenewalWrapper> renewalItems = new ArrayList<RenewalWrapper>();
    private ArrayList<OtherWrapper> otherItems = new ArrayList<>();
    private OpenEnrollmentHeader openEnrollmentHeader;
    private OpenEnrollmentAlertHeader openEnrollmentAlertHeader;
    private OpenEnrollmentNotAlertedHeader openEnrollmentNotAlertedHeader;
    private RenewalHeader renewalHeader;
    private OtherItemsHeader otherItemsHeader;

    private static boolean openEnrollmentState = true;
    private static boolean renewalState = false;
    private static boolean othersState = false;
    public View notAlerted;

    public enum AllClientSort {
        CompanyDesc,
        CompanyAsc,
        PlanYearDesc,
        PlanYearAsc
    };

    private AllClientSort allClientSort = AllClientSort.PlanYearDesc;

    public EmployerAdapter(MainActivity mainActivity, Context context,
                           List<BrokerClient> employerList, LocalDate coverageYear) throws Exception {
        this.mainActivity = mainActivity;
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.employerList = employerList;
        this.coverageYear = coverageYear;

        openEnrollmentHeader = new OpenEnrollmentHeader(this);
        openEnrollmentAlertHeader = new OpenEnrollmentAlertHeader(this);
        openEnrollmentNotAlertedHeader = new OpenEnrollmentNotAlertedHeader(this);
        renewalHeader = new RenewalHeader(this);
        otherItemsHeader = new OtherItemsHeader(this);

        LocalDate today = LocalDate.now();
        int i = 0;

        // The code makes some assumptions about plan years:
        //   There can only be one plan year in open enrollment
        //   There can only be one plan year in renewal
        //   Being in open enrollment and renewal are mutually exclusive.

        for (BrokerClient brokerClient : employerList) {
            if (brokerClient.planYears != null
                && brokerClient.planYears.size() > 0) {
                if (brokerClient.employerName.indexOf("Grig") >= 0){
                    Log.d(TAG, "found grig");
                }
                PlanYear lastestPlanYear = BrokerUtilities.getLastestPlanYear(brokerClient.planYears);
                switch (BrokerUtilities.getBrokerClientStatus(lastestPlanYear, today)){
                    case InOpenEnrollmentAlerted:
                        alertedItems.add(new OpenEnrollmentAlertedWrapper(brokerClient, lastestPlanYear, i));
                        break;
                    case InOpenEnrollmentNotAlerted:
                        notAlertedItems.add(new OpenEnrollmentNotAlertedWrapper(brokerClient, lastestPlanYear, i));
                        break;
                    case InRenewal:
                        renewalItems.add(new RenewalWrapper(brokerClient, lastestPlanYear, i));
                        break;
                }
                otherItems.add(new OtherWrapper(brokerClient, i, lastestPlanYear));
            } else {
                otherItems.add(new OtherWrapper(brokerClient, i, null));
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
            switch (allClientSort){
                case CompanyAsc:
                    sortOtherItemsByCompanyAsc(otherItems);
                    break;
                case CompanyDesc:
                    sortOtherItemsByCompanyDesc(otherItems);
                    break;
                case PlanYearAsc:
                    sortOtherItemsByPlayYearAsc(otherItems);
                    break;
                case PlanYearDesc:
                    sortOtherItemsByPlanYearDesc(otherItems);
                    break;
            }
            wrapped_items.addAll(otherItems);
        }
    }

    private void sortOtherItemsByCompanyDesc(ArrayList<OtherWrapper> otherItems) {
        Collections.sort(otherItems, new Comparator<OtherWrapper>() {
            @Override
            public int compare(OtherWrapper otherWrapper, OtherWrapper t1) {
                return -1 * otherWrapper.brokerClient.employerName.compareTo(t1.brokerClient.employerName);
            }
        });
    }
    private void sortOtherItemsByCompanyAsc(ArrayList<OtherWrapper> otherItems) {
        Collections.sort(otherItems, new Comparator<OtherWrapper>() {
            @Override
            public int compare(OtherWrapper otherWrapper, OtherWrapper t1) {
                return otherWrapper.brokerClient.employerName.compareTo(t1.brokerClient.employerName);
            }
        });
    }

    private void sortOtherItemsByPlanYearDesc(ArrayList<OtherWrapper> otherItems) {
        Collections.sort(otherItems, new Comparator<OtherWrapper>() {
            @Override
            public int compare(OtherWrapper otherWrapper, OtherWrapper t1) {
                if (otherWrapper.planYear == null){
                    return -1;
                }
                return -1 * otherWrapper.planYear.planYearBegins.compareTo(t1.planYear.planYearBegins);
            }
        });
    }
    private void sortOtherItemsByPlayYearAsc(ArrayList<OtherWrapper> otherItems) {
        Collections.sort(otherItems, new Comparator<OtherWrapper>() {
            @Override
            public int compare(OtherWrapper otherWrapper, OtherWrapper t1) {
                return otherWrapper.planYear.planYearBegins.compareTo(t1.planYear.planYearBegins);
            }
        });
    }

    public void sortByCompanyName() {
        if (allClientSort == AllClientSort.CompanyAsc){
            allClientSort = AllClientSort.CompanyDesc;
        } else {
            if (allClientSort == AllClientSort.CompanyDesc){
                allClientSort = AllClientSort.CompanyAsc;
            } else {
                allClientSort = AllClientSort.CompanyAsc;
            }
        }
        updateWrappedItems();
        notifyDataSetChanged();
    }

    public void sortByPlanYear() {
        if (allClientSort == AllClientSort.PlanYearAsc){
            allClientSort = AllClientSort.PlanYearDesc;
        } else {
            if (allClientSort == AllClientSort.PlanYearDesc){
                allClientSort = AllClientSort.PlanYearAsc;
            } else {
                allClientSort = AllClientSort.PlanYearAsc;
            }
        }
        updateWrappedItems();
        notifyDataSetChanged();
    }


    @Override
    public int getViewTypeCount(){
        return  9;
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
    public int getSwipeLayoutResourceId(int position) {
        return ((ItemWrapperBase)getItem(position)).getSwipeLayoutResourceId();
    }

    @Override
    public View generateView(int position, ViewGroup parent) {
        return ((ItemWrapperBase)getItem(position)).generateView(inflater, parent);
    }

    @Override
    public void fillValues(int position, View convertView) {
        try {
            ((ItemWrapperBase)getItem(position)).fillValues(convertView, mainActivity);
        } catch (Exception e) {
            Log.e(TAG, "Exception in fillValues", e);
        }
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

    public boolean toggleOthers() {
        othersState = !othersState;
        updateWrappedItems();
        notifyDataSetChanged();
        return othersState;
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

    public boolean isItemSwipeable(int position) {
        return wrapped_items.get(position).isSwipeable();
    }

    public int getTagId() {
        return R.id.view_type;
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

    //public abstract View getView(View convertView, ViewGroup parent, MainActivity mainActivity, LayoutInflater inflater);



    //return the `SwipeLayout` resource id in your listview | gridview item layout.
    public abstract int getSwipeLayoutResourceId();

    //render a new item layout.
    public abstract View generateView(LayoutInflater inflater, ViewGroup parent);

    /*fill values to your item layout returned from `generateView`.
      The position param here is passed from the BaseAdapter's 'getView()*/
    public abstract void fillValues(View convertView, final MainActivity mainActivity) throws Exception;
}

class OpenEnrollmentHeader extends ItemWrapperBase {

    private final EmployerAdapter employerAdapter;

    OpenEnrollmentHeader(EmployerAdapter employerAdapter) {
        this.employerAdapter = employerAdapter;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public void fillValues(View convertView, MainActivity mainActivity) {
        Button openButton = (Button)convertView.findViewById(R.id.buttonCount);
        ImageView circleImageView = (ImageView)convertView.findViewById(R.id.imageViewCircle);
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
                if (employerAdapter.getAlertedItems().size() == 0
                    && employerAdapter.getNotAlertedItems().size() == 0) {
                    return;
                }
                employerAdapter.toggleOpenEnrollments();
            }
        });
    }

    @Override
    public int getSwipeLayoutResourceId() {
        return 0;
    }

    @Override
    public View generateView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.open_enrollment_header, parent, false);
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
    public int getSwipeLayoutResourceId() {
        return 0;
    }

    @Override
    public View generateView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.open_enrollment_alerted_header, parent, false);
    }

    @Override
    public void fillValues(View convertView, MainActivity mainActivity) {

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
    public void fillValues(View convertView, MainActivity mainActivity) {
        employerAdapter.notAlerted = convertView;
    }

    @Override
    public int getSwipeLayoutResourceId() {
        return 0;
    }

    @Override
    public View generateView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.open_enrollment_not_alerted_header, parent, false);
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

    public void fillValues(View convertView, MainActivity mainActivity) {
        Button openButton = (Button)convertView.findViewById(R.id.buttonCount);
        ImageView circleImageView = (ImageView)convertView.findViewById(R.id.imageViewCircle);
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
                if (employerAdapter.getRenewalItems().size() == 0){
                    return;
                }
                employerAdapter.toggleRenewals();
            }
        });
        if (!employerAdapter.isRenewalOpen()){
            View columnHeaders = convertView.findViewById(R.id.relativeLayoutColumnHeaders);
            columnHeaders.setVisibility(View.GONE);
        }
    }

    @Override
    public int getSwipeLayoutResourceId() {
        return 0;
    }

    @Override
    public View generateView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.renewals_in_progress_header, parent, false);
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
    public void fillValues(View convertView, final MainActivity mainActivity) {
        Button openButton = (Button)convertView.findViewById(R.id.buttonCount);
        ImageView circleImageView = (ImageView)convertView.findViewById(R.id.imageViewCircle);
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
                if (employerAdapter.getOtherItems().size() == 0){
                    return;
                }

                View parent = (View) v.getParent();
                RelativeLayout relativeLayoutColumnHeaders = (RelativeLayout) (parent.findViewById(R.id.relativeLayoutColumnHeaders));

                if (employerAdapter.toggleOthers()){
                    relativeLayoutColumnHeaders.setVisibility(View.VISIBLE);
                } else {
                    relativeLayoutColumnHeaders.setVisibility(View.GONE);
                }
            }
        });
        if (!employerAdapter.isOthersOpen()) {
            View columnHeaders = convertView.findViewById(R.id.relativeLayoutColumnHeaders);
            columnHeaders.setVisibility(View.GONE);
        }

        TextView textViewCompanyName = (TextView) convertView.findViewById(R.id.textViewCompanyNameLabel);
        textViewCompanyName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                employerAdapter.sortByCompanyName();
            }
        });
        ImageView imageViewCompanyNameSortIndicator = (ImageView)convertView.findViewById(R.id.imageViewCompanyNameSortIndicator);
        imageViewCompanyNameSortIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                employerAdapter.sortByCompanyName();
            }
        });
        TextView textViewPlanYearLabel = (TextView) convertView.findViewById(R.id.textViewPlanYearLabel);
        textViewPlanYearLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                employerAdapter.sortByPlanYear();
            }
        });
        ImageView imageViewPlanYearSortIndicator = (ImageView)convertView.findViewById(R.id.imageViewPlanYearSortIndicator);
        imageViewPlanYearSortIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                employerAdapter.sortByPlanYear();
            }
        });
    }

    @Override
    public int getSwipeLayoutResourceId() {
        return 0;
    }

    @Override
    public View generateView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.all_other_clients_header, parent, false);
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

    public void fillSwipeButtons(View view, final MainActivity mainActivity, final BrokerClient brokerClient){
        ImageButton emailButton = (ImageButton)view.findViewById(R.id.imageButtonEmail);

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactDialog contactDialog = ContactDialog.build(brokerClient, ContactListAdapter.ListType.Email);
                contactDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
                contactDialog.show(mainActivity.getSupportFragmentManager(), "ContactDialog");
            }
        });
        ImageButton chatButton = (ImageButton)view.findViewById(R.id.imageButtonChat);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactDialog contactDialog = ContactDialog.build(brokerClient, ContactListAdapter.ListType.Chat);
                contactDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
                contactDialog.show(mainActivity.getSupportFragmentManager(), "ContactDialog");
            }
        });
        ImageButton phoneButton = (ImageButton)view.findViewById(R.id.imageButtonPhone);
        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactDialog contactDialog = ContactDialog.build(brokerClient, ContactListAdapter.ListType.Phone);
                contactDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
                contactDialog.show(mainActivity.getSupportFragmentManager(), "ContactDialog");
            }
        });
        ImageButton locationButton = (ImageButton)view.findViewById(R.id.imageButtonLocation);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactDialog contactDialog = ContactDialog.build(brokerClient, ContactListAdapter.ListType.Directions);
                contactDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
                contactDialog.show(mainActivity.getSupportFragmentManager(), "ContactDialog");
            }
        });
    }
}

class OpenEnrollmentAlertedWrapper extends BrokerClientWrapper {
    private static final String TAG = "OpenEnrlmentAlertedWrpr";
    private static final String VIEW_TYPE = "OpenEnrollmentAlertedView";
    private final PlanYear planYear;

    public OpenEnrollmentAlertedWrapper(BrokerClient brokerclient, PlanYear planYear, int i) {
        super(brokerclient, i);
        this.planYear = planYear;
    }

    @Override
    public int getType() {
        return 5;
    }

    @Override
    public void fillValues(View convertView, final MainActivity mainActivity) throws Exception {
        TextView companyName = (TextView)convertView.findViewById(R.id.textViewCompanyName);
        companyName.setText(brokerClient.employerName);

        TextView employeesNeeded = (TextView) convertView.findViewById(R.id.textViewEmployessNeeded);
        int employeesNeeded1 = BrokerUtilities.getEmployeesNeeded(planYear);
        if (employeesNeeded1 >= 0){
            employeesNeeded.setText(String.valueOf(employeesNeeded1));
        }

        TextView daysLeft = (TextView)convertView.findViewById(R.id.textViewDaysLeft);
        daysLeft.setText(String.valueOf(BrokerUtilities.daysLeft(planYear, LocalDate.now())));
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intents.launchEmployerDetails(mainActivity, BrokerUtilities.getBrokerClientId(brokerClient));
            }
        });
        fillSwipeButtons(convertView, mainActivity, brokerClient);
    }

    @Override
    public boolean isSwipeable() {
        return true;
    }

    @Override
    public int getSwipeLayoutResourceId() {
        return 0;
    }

    @Override
    public View generateView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.open_enrollment_item, parent, false);

    }
}

class OpenEnrollmentNotAlertedWrapper extends BrokerClientWrapper {
    private static final String TAG = "OpnEnrlmntNotAlertdWrpr";
    private static final String VIEW_TYPE = "OpenEnrollmentNotAlertedView";
    private final PlanYear planYear;

    public OpenEnrollmentNotAlertedWrapper(BrokerClient brokerclient, PlanYear planYear, int i) {
        super(brokerclient, i);
        this.planYear = planYear;
    }

    @Override
    public int getType() {
        return 6;
    }

    @Override
    public boolean isSwipeable() {
        return true;
    }

    protected void fillOpenEnrollmentItem(View view, MainActivity mainActivity) throws Exception {
        final MainActivity mainActivityFinal = mainActivity;
        TextView companyName = (TextView)view.findViewById(R.id.textViewCompanyName);
        companyName.setText(brokerClient.employerName);
        TextView employeesNeeded = (TextView)view.findViewById(R.id.textViewEmployessNeeded);
        employeesNeeded.setText(String.valueOf(planYear.employeesEnrolled));
        TextView daysLeft = (TextView)view.findViewById(R.id.textViewDaysLeft);
        daysLeft.setText(String.valueOf(BrokerUtilities.daysLeft(planYear, LocalDate.now())));
        View alertBar = view.findViewById(R.id.imageViewAlertBar);
        alertBar.setVisibility(View.GONE);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intents.launchEmployerDetails(mainActivityFinal, BrokerUtilities.getBrokerClientId(brokerClient));
            }
        });
        fillSwipeButtons(view, mainActivity, brokerClient);
    }

    public View getView(View convertView, ViewGroup parent, MainActivity mainActivity, LayoutInflater inflater) throws Exception {
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

    @Override
    public int getSwipeLayoutResourceId() {
        return R.id.swipe;
    }

    @Override
    public View generateView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.open_enrollment_item, parent, false);
    }

    @Override
    public void fillValues(View convertView, MainActivity activity) throws Exception {
        fillOpenEnrollmentItem(convertView, activity);
    }
}

class RenewalWrapper extends BrokerClientWrapper {
    private static final String VIEW_TYPE = "RenewalView";
    private static final String TAG = "RenewalWrapper";
    private final PlanYear planYear;

    public RenewalWrapper(BrokerClient brokerClient, PlanYear planYear, int i) {
        super(brokerClient, i);
        this.planYear = planYear;
    }

    @Override
    public boolean isSwipeable() {
        return true;
    }

    @Override
    public int getType() {
        return 7;
    }

    public void fillValues(View view, MainActivity mainActivity) throws Exception {
        final MainActivity mainActivityFinal = mainActivity;

        PlanYear pendingRenewalPlanYear = BrokerUtilities.getPendingRenewalPlanYear(brokerClient);
        TextView companyName = (TextView)view.findViewById(R.id.textViewCompanyName);
        companyName.setText((brokerClient).employerName);
        TextView planYear = (TextView)view.findViewById(R.id.textViewPlanYear);
        CharSequence dateString = Utilities.DateAsMonthDayYear(pendingRenewalPlanYear.planYearBegins);
        planYear.setText(dateString);
        TextView daysLeft = (TextView)view.findViewById(R.id.textViewDaysLeft);
        daysLeft.setText(String.valueOf(BrokerUtilities.daysLeftToRenewal(pendingRenewalPlanYear, LocalDate.now())));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intents.launchEmployerDetails(mainActivityFinal, BrokerUtilities.getBrokerClientId(brokerClient));
            }
        });
        fillSwipeButtons(view, mainActivity, brokerClient);
    }

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
        return view;
    }

    @Override
    public int getSwipeLayoutResourceId() {
        return R.id.swipe;
    }

    @Override
    public View generateView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.renewals_in_progress_item, parent, false);
    }
}

class OtherWrapper extends BrokerClientWrapper {
    private static final String VIEW_TYPE = "OtherView";
    private static final String TAG = "OtherWrapper";

    public PlanYear planYear = null;

    public OtherWrapper(BrokerClient brokerclient, int i, PlanYear planYear) {
        super(brokerclient, i);
        this.planYear = planYear;
    }

    @Override
    public boolean isSwipeable() {
        return true;
    }

    @Override
    public int getType() {
        return 8;
    }

    @Override
    public void fillValues(View view, MainActivity mainActivity) throws Exception {
        final MainActivity mainActivityFinal = mainActivity;
        TextView companyName = (TextView)view.findViewById(R.id.textViewCompanyName);
        companyName.setText((brokerClient).employerName);

        LocalDate today = LocalDate.now();
        PlanYear curPlanYear = null;
        int daysLeft = -1;

        if (planYear != null
                && planYear.planYearBegins != null) {
            TextView textViewPlanYear = (TextView) view.findViewById(R.id.textViewPlanYear);
            textViewPlanYear.setText(Utilities.DateAsMonthDay(planYear.planYearBegins));
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intents.launchEmployerDetails(mainActivityFinal, BrokerUtilities.getBrokerClientId(brokerClient));
            }
        });
        fillSwipeButtons(view, mainActivity, brokerClient);
    }

    @Override
    public int getSwipeLayoutResourceId() {
        return R.id.swipe;
    }

    @Override
    public View generateView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.all_other_clients_item, parent, false);
    }

}


