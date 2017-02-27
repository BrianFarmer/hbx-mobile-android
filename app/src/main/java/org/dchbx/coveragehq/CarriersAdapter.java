package org.dchbx.coveragehq;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by plast on 9/6/2016.
 */
public class CarriersAdapter extends BaseAdapter {
    private final CarriersActivity carriersActivity;
    private final Carriers carriers;
    private final ArrayList<Carrier> carriersArrayList;

    public CarriersAdapter(CarriersActivity carriersActivity, Carriers carriers){
        this.carriersActivity = carriersActivity;
        this.carriers = carriers;
        this.carriersArrayList = new ArrayList<Carrier>();

        for (Map.Entry<String, Carrier> entry:
             carriers.entrySet()) {
            carriersArrayList.add(entry.getValue());
        }
    }
    @Override
    public int getCount() {
        return carriersArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return carriersArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView != null){
            view = convertView;
        } else {
            LayoutInflater inflater = (LayoutInflater) carriersActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.carrier_item, parent, false);
        }
        ImageView carrierLogoImageView = (ImageView)view.findViewById(R.id.imageViewCarrierLogo);
        TextView phoneNumberTextView = (TextView)view.findViewById(R.id.textViewPhoneNumber);
        final Carrier item = (Carrier) getItem(position);
        final String phoneNumber = item.phone;
        phoneNumberTextView.setText(item.phone);
        Glide
            .with(carriersActivity)
            .load(item.img)
            .into(carrierLogoImageView);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                carriersActivity.callPhoneNumber(phoneNumber);
            }
        });
        return view;
    }
}
