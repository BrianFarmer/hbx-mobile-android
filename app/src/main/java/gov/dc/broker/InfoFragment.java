package gov.dc.broker;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by plast on 10/21/2016.
 */

public class InfoFragment extends BrokerFragment {
    private static String TAG = "BrokerFragment";

    private int brokerClientId;
    private BrokerClient brokerClient = null;
    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        init();

        // TODO Auto-generated method stub
        view = LayoutInflater.from(getActivity()).inflate(R.layout.info_fragment, null);

        if (brokerClient == null) {
            brokerClientId = getBrokerActivity().getIntent().getIntExtra(Intents.BROKER_CLIENT_ID, -1);
            if (brokerClientId == -1) {
                // If we get here the employer id in the intent wasn't initialized and
                // we are in a bad state.
                Log.e(TAG, "onCreate: no client id found in intent");
                return view;
            }
            getMessages().getEmployer(brokerClientId);
        }
        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.BrokerClient brokerClientEvent) {
        brokerClient = brokerClientEvent.getBrokerClient();
        populateField();
    }

    private void populateField() {
        TextView openEnrollmentBegins = (TextView) view.findViewById(R.id.textViewOpenEnrollmentBegins);
        openEnrollmentBegins.setText(Utilities.DateAsString(brokerClient.openEnrollmentBegins));
        TextView openEnrollmentEnds = (TextView) view.findViewById(R.id.textViewOpenEnrollmentEnds);
        openEnrollmentEnds.setText(Utilities.DateAsString(brokerClient.openEnrollmentEnds));
        TextView daysLeft = (TextView) view.findViewById(R.id.textViewDaysLeft);
        daysLeft.setText(Long.toString(Utilities.dateDifferenceDays(brokerClient.openEnrollmentBegins, brokerClient.openEnrollmentEnds)));
    }
}