package org.dchbx.coveragehq;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.dchbx.coveragehq.models.brokeragency.BrokerClient;
import org.dchbx.coveragehq.models.brokeragency.ContactInfo;

public class ContactDialog extends AppCompatDialogFragment {
    private BrokerClient brokerClient;
    private ContactListAdapter.ListType listType;
    private View view;
    private Context context;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ContactDialog thisContactDialog = this;
        view = inflater.inflate(R.layout.contact_action_selection,container, false);

        Bundle arguments = getArguments();
        listType = ContactListAdapter.ListType.fromInt(arguments.getInt("ListType"));

        ContactListAdapter listAdapter;
        brokerClient = (BrokerClient) arguments.getSerializable("BrokerClient");
        if (brokerClient == null){
            ContactInfo contactInfo = (ContactInfo) arguments.getSerializable("ContactInfo");
            ArrayList<ContactInfo> contactInfos = new ArrayList<>();
            contactInfos.add(contactInfo);
            listAdapter = new ContactListAdapter(context, contactInfos, listType, this);
        } else {
            listAdapter = new ContactListAdapter(context, brokerClient.contactInfo, listType, this);

        }
        listType = ContactListAdapter.ListType.fromInt(arguments.getInt("ListType"));

        final ContactDialog contactDialog = this;
        ListView listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(listAdapter);
        TextView titleBarTextView = (TextView) view.findViewById(R.id.textViewTitleBar);
        titleBarTextView.setText(brokerClient.employerName);

        Button cancelButton = (Button) view.findViewById(R.id.buttonCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisContactDialog.dismiss();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public static ContactDialog build(BrokerClient brokerClient, ContactListAdapter.ListType dialogtype) {
        ContactDialog contactDialog = new ContactDialog();

        Bundle bundle = new Bundle();
        bundle.putSerializable("BrokerClient", brokerClient);
        bundle.putInt("ListType", dialogtype.ordinal());
        contactDialog.setArguments(bundle);
        return contactDialog;
    }

    public static ContactDialog build(ContactInfo contactInfo, ContactListAdapter.ListType dialogtype) {
        ContactDialog contactDialog = new ContactDialog();

        Bundle bundle = new Bundle();
        bundle.putSerializable("ContactInfo", contactInfo);
        bundle.putInt("ListType", dialogtype.ordinal());
        contactDialog.setArguments(bundle);
        return contactDialog;
    }

    public void launchEmail(ContactInfo contactInfo, String email) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email));
        startActivity(intent);
        dismiss();;
    }

    public void launchMap(ContactInfo contactInfo) {
        String address = contactInfo.address1 + " " + contactInfo.city + " " + contactInfo.state;
        try {
            address = URLEncoder.encode(address, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("geo:0,0?q="+ address));
        startActivity(intent);
        dismiss();;
    }

    public void launchPhoneCall(ContactInfo contactInfo, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
        dismiss();
    }

    public void launchChat(ContactInfo contactInfo, String chatPhoneNumber) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("sms:" + chatPhoneNumber));
        startActivity(intent);
        dismiss();;
    }
}
