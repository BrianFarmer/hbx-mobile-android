package org.dchbx.coveragehq;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class CarriersActivity extends BrokerActivity {

    private EventBus eventBus;
    private boolean inErrorState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMessages().getCarriers();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Error error) {
        if (inErrorState){
            return;
        }
        inErrorState = true;
        final CarriersActivity _this = this;
        NetworkErrorDialog.build(this, R.string.app_title, R.string.network_access_error, new NetworkErrorDialog.Handler(){
            @Override
            public void finished() {
                inErrorState = false;
                getMessages().getCarriers();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Carriers carriersEvent) {
        Carriers carriers = carriersEvent.getCarriers();
        setContentView(R.layout.activity_carriers);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setLogo(R.drawable.app_header);
        toolbar.setTitle("");

        ListView listViewCarriers = (ListView) findViewById(R.id.listViewCarriers);
        listViewCarriers.setAdapter(new CarriersAdapter(this, carriers));
    }

    public void callPhoneNumber(String phoneNumber){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }
}
