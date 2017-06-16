package org.dchbx.coveragehq;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.dchbx.coveragehq.models.roster.Health;
import org.dchbx.coveragehq.models.services.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by plast on 4/19/2017.
 */

public class WalletSummaryAdapter extends SummaryAdapter {

    public WalletSummaryAdapter(SummaryOfBenefitsActivity activity, List<Service> servicesList, Health plan){
        super(activity);

        items = new ArrayList<>();
        items.add(new ResourcesHeaderWrapper());
        items.add(new ResourcesItemWrapper(plan.summaryOfBenefitsUrl, R.drawable.pdf_document, activity.getString(R.string.terms_conditions_pdf), activity));
        items.add(new ResourcesItemWrapper(plan.provider_directory_url, R.drawable.physicians, activity.getString(R.string.provider_directory), activity));
        items.add(new ResourcesItemWrapper(plan.RxFormularyUrl, R.drawable.prescription_formulary, activity.getString(R.string.rx_formulary),activity));
        items.add(new ContactWrapper(activity, plan));
        items.add(new SummaryHeaderWrapper());
        for (Service service : servicesList) {
            items.add(new SummaryItemWrapper(service, items.size(), this));
        }
    }
}



class ContactWrapper extends AdapterItemWrapperBase {
    private static String TAG = ContactWrapper.class.getSimpleName();
    private final Health plan;
    private final FragmentActivity activity;

    public ContactWrapper(FragmentActivity activity, Health plan){
        this.plan = plan;
        this.activity = activity;
    }

    @Override
    public int getLayout() {
        return R.layout.summary_resource_item;
    }

    @Override
    public void populate(View view) {
        Log.d(TAG, "in ResourcesItemWrapper.fillValues");
        TextView label = (TextView) view.findViewById(R.id.resourceName);
        label.setText(R.string.plan_contact_information);
        label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlanContactInfoDialog dialog = PlanContactInfoDialog.build(activity, plan.carrierName);
            }
        });
        ImageView resourceImage = (ImageView) activity.findViewById(R.id.resourceImage);
        resourceImage.setImageResource(R.drawable.phone);
    }

    @Override
    public String getViewType() {
        return null;
    }
}


