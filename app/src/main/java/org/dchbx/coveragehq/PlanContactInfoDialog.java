package org.dchbx.coveragehq;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class PlanContactInfoDialog extends BrokerAppCompatDialogFragment {
    View view;
    PlanContactInfoDialog dialog;
    private Context context;
    private String carrierName;

    public static PlanContactInfoDialog build(FragmentActivity activity, String carrierName) {
        PlanContactInfoDialog dialog = new PlanContactInfoDialog();

        Bundle bundle = new Bundle();
        bundle.putCharSequence("carrier", carrierName);
        dialog.setArguments(bundle);
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
        dialog.show(activity.getSupportFragmentManager(), activity.getString(R.string.plan_contact_info));
        return dialog;
    }

    public PlanContactInfoDialog(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        carrierName = arguments.getCharSequence("carrier").toString();
        view = inflater.inflate(R.layout.carrier_contact_dialog, container, false);
        setCancelable(false);
        getMessages().getCarriers();
        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Carriers carriers){
        final Carrier carrier = carriers.getCarriers().get(carrierName.toLowerCase());
        TextView carrierPhoneNumber = (TextView) view.findViewById(R.id.carrierPhoneNumber);
        carrierPhoneNumber.setText(carrier.phone);
        carrierPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + carrier.phone));
                startActivity(intent);
                dismiss();
            }
        });
        TextView carrierWebSite = (TextView) view.findViewById(R.id.carrierWebSite);
        carrierWebSite.setText(carrier.url);
        carrierWebSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(carrier.url));
                startActivity(intent);
                dismiss();
            }
        });
        Button buttonExit = (Button) view.findViewById(R.id.buttonCancel);
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        init();
        this.context = context;
    }
}
