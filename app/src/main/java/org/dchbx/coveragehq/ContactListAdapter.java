package org.dchbx.coveragehq;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dchbx.coveragehq.models.brokeragency.ContactInfo;

/**
 * Created by plast on 8/31/2016.
 */
public class ContactListAdapter extends BaseAdapter {

    private final Context context;
    private List<ContactInfo> contactInfos;
    private final ListType listType;
    private final LayoutInflater inflater;
    private final ArrayList<ContactItemWrapperBase> listItems;

    enum ListType {
        Email,
        Phone,
        Directions,
        Chat;

        static HashMap<Integer, ListType> map = null;

        static ListType fromInt(int i){
            if (map == null){
                int j = 0;
                map = new HashMap<Integer, ListType>();
                for (ListType l: ListType.values()) {
                    map.put(j, l);
                    j++;
                }
            }
            return map.get(i);
        }

    }

    public ContactListAdapter(Context context, List<ContactInfo> contactInfos,
                              ListType listType, ContactDialog contactDialog){
        this.context = context;
        this.contactInfos = contactInfos;
        this.listType = listType;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listItems = new ArrayList<ContactItemWrapperBase>();

        for (ContactInfo contactInfo:
             contactInfos) {
            switch (listType){
                case Email:
                    if (!contactInfo.emails.isEmpty()){
                        listItems.add(new NameHeader(contactInfo));
                        for (String email:contactInfo.emails) {
                            listItems.add(new EmailWrapper(contactInfo, email, contactDialog));
                        }
                    }
                    break;
                case Phone:
                    PhoneWrapper phoneWrapper = null;
                    PhoneWrapper mobileWrapper = null;
                    if (contactInfo.phone != null && !contactInfo.phone.isEmpty()) {
                        phoneWrapper = new PhoneWrapper(contactInfo, context.getString(R.string.PhoneLabel), contactInfo.phone, contactDialog);
                    }
                    if (contactInfo.mobile != null && !contactInfo.mobile.isEmpty()) {
                        mobileWrapper = new PhoneWrapper(contactInfo, context.getString(R.string.MobileLabel), contactInfo.mobile, contactDialog);
                    }
                    if (phoneWrapper != null || mobileWrapper != null) {
                        listItems.add(new NameHeader(contactInfo));
                        if (phoneWrapper != null) {
                            listItems.add(phoneWrapper);
                        }
                        if (mobileWrapper != null) {
                            listItems.add(mobileWrapper);
                        }
                    }
                    break;
                case Directions:
                    if (contactInfo.address1 != null
                        && contactInfo.address1.length() > 0) {
                        listItems.add(new NameHeader(contactInfo));
                        listItems.add(new AddressWrapper(contactInfo, contactDialog));
                    }
                    break;
                case Chat:
                    if (!BrokerUtilities.isPrimaryOffice(contactInfo)
                        && contactInfo.mobile != null
                        && !contactInfo.mobile.isEmpty()) {
                        listItems.add(new NameHeader(contactInfo));
                        listItems.add(new ChatWrapper(contactInfo, contactInfo.mobile, contactDialog));
                    }
                    break;
            }
        }
        if (listItems.isEmpty()){
            listItems.add(new EmptyListWrapper(listType));
        }
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return listItems.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ContactItemWrapperBase item = listItems.get(position);
        String  viewType = item.getViewType();
        View view;
        boolean createNewView = true;

        if (convertView != null){
            Object tagObject = convertView.getTag(R.id.view_type);
            if (tagObject != null){
                String tagString = (String) tagObject;
                if (tagString.equals(viewType)){
                    createNewView = false;
                }
            }
        }
        if (createNewView) {
            view = inflater.inflate(item.getLayout(), parent, false);
            view.setTag(R.id.view_type, viewType);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (item.clicked()) {

                    }
                }
            });
        } else {
            view = convertView;
        }
        item.fill(view);
        return view;
    }
}

abstract class ContactItemWrapperBase {
    private static long nextId = 0;
    private final long id;

    protected ContactItemWrapperBase(){
        id = nextId ;
        nextId = nextId + 1;
    }

    public boolean isSwipeable(){
        return false;
    }

    public long getId() {
        return id;
    }
    public abstract int getLayout();
    public abstract void fill(View view);
    public abstract String getViewType();

    public abstract boolean clicked();
}

class EmptyListWrapper extends ContactItemWrapperBase{

    private final ContactListAdapter.ListType listType;

    public EmptyListWrapper(ContactListAdapter.ListType listType){
        this.listType = listType;
    }
    @Override
    public int getLayout() {
        return R.layout.client_info_empty_item;
    }

    @Override
    public void fill(View view) {
        int stringResourceId = 0;
        switch (listType){
            case Email:
                stringResourceId = R.string.no_emails;
                break;
            case Phone:
                stringResourceId = R.string.no_phone_numbers;
                break;
            case Directions:
                stringResourceId = R.string.no_addresses;
                break;
            case Chat:
                stringResourceId = R.string.no_mobile_phone_numbers;
                break;
        }
        TextView emptyItemLabel = (TextView) view.findViewById(R.id.textViewEmptyItem);
        emptyItemLabel.setText(stringResourceId);
    }

    @Override
    public String getViewType() {
        return null;
    }

    @Override
    public boolean clicked() {
        return false;
    }
}
class AddressWrapper extends ContactItemWrapperBase{

    private final ContactInfo contactInfo;
    private final ContactDialog dialog;

    public AddressWrapper(ContactInfo contactInfo, ContactDialog dialog){
        this.contactInfo = contactInfo;
        this.dialog = dialog;
    }

    @Override
    public String getViewType() {
        return "Address";
    }

    @Override
    public boolean clicked() {
        dialog.launchMap(contactInfo);
        return false;
    }

    @Override
    public int getLayout() {
        return R.layout.contact_info_address_item;
    }

    @Override
    public void fill(View view) {
        TextView addressLine1 = (TextView) view.findViewById(R.id.textViewAddressLine1);
        addressLine1.setText(contactInfo.address1);
        TextView addressLine2 = (TextView) view.findViewById(R.id.textViewAddressLine2);
        addressLine2.setText(contactInfo.city + ", " + contactInfo.state + " " + contactInfo.zip);
    }
}
class NameHeader extends ContactItemWrapperBase {

    private final ContactInfo contactInfo;

    public NameHeader(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    @Override
    public String getViewType() {
        return "Name";
    }

    @Override
    public boolean clicked() {
        return false;
    }

    @Override
    public int getLayout() {
        return R.layout.contact_info_header;
    }

    public void fill(View view) {
        TextView contactName = (TextView) view.findViewById(R.id.textViewContactName);
        contactName.setText(contactInfo.first + " " + contactInfo.last);
    }
}

class PhoneWrapper extends ContactItemWrapperBase {
    private final ContactInfo contactInfo;
    private final String label;
    private final String phoneNumber;
    private final ContactDialog dialog;

    public PhoneWrapper(ContactInfo contactInfo, String label, String phoneNumber, ContactDialog dialog){
        this.contactInfo = contactInfo;
        this.label = label;
        this.phoneNumber = phoneNumber;
        this.dialog = dialog;
    }

    @Override
    public String getViewType() {
        return "ContactInfo";
    }

    @Override
    public boolean clicked() {
        dialog.launchPhoneCall(contactInfo, phoneNumber);
        return false;
    }

    @Override
    public int getLayout() {
        return R.layout.contact_info_item;
    }

    public void fill(View view) {
        TextView itemType = (TextView) view.findViewById(R.id.textViewItemType);
        TextView itemValue = (TextView) view.findViewById(R.id.textViewItemValue);
        itemType.setText(label);
        itemValue.setText(phoneNumber);
    }
}

class EmailWrapper extends ContactItemWrapperBase {
    private final ContactInfo contactInfo;
    private final String email;
    private final ContactDialog dialog;

    public EmailWrapper(ContactInfo contactInfo, String email, ContactDialog dialog){
        this.contactInfo = contactInfo;
        this.email = email;
        this.dialog = dialog;
    }

    @Override
    public String getViewType() {
        return "Email";
    }

    @Override
    public boolean clicked() {
        dialog.launchEmail(contactInfo, email);
        return true;
    }

    @Override
    public int getLayout() {
        return R.layout.contact_info_item;
    }

    public void fill(View view) {
        TextView itemType = (TextView) view.findViewById(R.id.textViewItemType);
        TextView itemValue = (TextView) view.findViewById(R.id.textViewItemValue);
        itemType.setText("");
        itemValue.setText(email);
    }
}

class ChatWrapper extends ContactItemWrapperBase {
    private final ContactInfo contactInfo;
    private final String chatPhoneNumber;
    private final ContactDialog dialog;

    public ChatWrapper(ContactInfo contactInfo, String chatPhoneNumber, ContactDialog dialog){
        this.contactInfo = contactInfo;
        this.chatPhoneNumber = chatPhoneNumber;
        this.dialog = dialog;
    }

    @Override
    public String getViewType() {
        return "ContactInfo";
    }

    @Override
    public boolean clicked() {
        dialog.launchChat(contactInfo, chatPhoneNumber);
        return false;
    }

    @Override
    public int getLayout() {
        return R.layout.contact_info_item;
    }

    public void fill(View view) {
        TextView itemType = (TextView) view.findViewById(R.id.textViewItemType);
        itemType.setText(R.string.MobileLabel);
        TextView itemValue = (TextView) view.findViewById(R.id.textViewItemValue);
        itemValue.setText(chatPhoneNumber);
    }
}