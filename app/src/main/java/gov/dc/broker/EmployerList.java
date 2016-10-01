package gov.dc.broker;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by plast on 7/27/2016.
 */
public class EmployerList {
    @SerializedName("broker_agency")
    public String brokerAgency;

    @SerializedName("broker_name")
    public String brokerName;

    @SerializedName("broker_agency_id")
    public String brokerAgencyId;

    @SerializedName("broker_clients")
    public List<BrokerClient> brokerClients;

    private ArrayList<BrokerClient> toArrayList(){
        ArrayList<BrokerClient> arrayList = new ArrayList<BrokerClient>();
        for (BrokerClient brokerClient : brokerClients) {
            arrayList.add(brokerClient);
        }
        return arrayList;
    }
    
    public ArrayList<BrokerClient> getAllClients(){
        return toArrayList();
    }

    public ArrayList<BrokerClient> getOpenEnrollmentsAlerted() {
        ArrayList<BrokerClient> employers_in_open_enrollment = new ArrayList<BrokerClient>();
        for (BrokerClient brokerClient : brokerClients) {
            if (brokerClient.isInOpenEnrollment(Calendar.getInstance().getTime())
                && brokerClient.isAlerted()){
                employers_in_open_enrollment.add(brokerClient);
            }
        }
        return employers_in_open_enrollment;
    }

    public ArrayList<BrokerClient> getOpenEnrollmentsNotAlerted() {
        ArrayList<BrokerClient> employers_in_open_enrollment = new ArrayList<BrokerClient>();
        for (BrokerClient brokerClient : brokerClients) {
            if (brokerClient.isInOpenEnrollment(Calendar.getInstance().getTime())
                && !brokerClient.isAlerted()){
                employers_in_open_enrollment.add(brokerClient);
            }
        }
        return employers_in_open_enrollment;
    }

    public ArrayList<BrokerClient> getRenewalsInProgress(){
        ArrayList<BrokerClient> inRenewal = new ArrayList<BrokerClient>();
        for (BrokerClient brokerClient : brokerClients) {
            if (brokerClient.renewalInProgress){
                inRenewal.add(brokerClient);
            }
        }
        return inRenewal;
    }
}
