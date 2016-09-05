package gov.dc.broker;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ContactDialog extends AppCompatDialogFragment {
    private BrokerClient brokerClient;
    private ContactListAdapter.ListType listType;
    private View view;
    private Context context;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.contact_action_selection,container, false);

        Bundle arguments = getArguments();
        brokerClient = (BrokerClient) arguments.getSerializable("BrokerClient");
        listType = ContactListAdapter.ListType.fromInt(arguments.getInt("ListType"));

        final ContactDialog contactDialog = this;

        ListView listView = (ListView) view.findViewById(R.id.listView);
        /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View arg1,int position, long arg3)
            {

                Toast.makeText(getActivity(), "" + position, Toast.LENGTH_SHORT).show();
            }
        });*/
        ContactListAdapter listAdapter = new ContactListAdapter(context, brokerClient, listType, this);
        listView.setAdapter(listAdapter);
        TextView titleBarTextView = (TextView) view.findViewById(R.id.textViewTitleBar);
        titleBarTextView.setText(brokerClient.employerName);

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

    public void launchEmail(ContactInfo contactInfo, String email) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:myemail@gmail.com"));
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
