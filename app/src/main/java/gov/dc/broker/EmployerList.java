package gov.dc.broker;

import com.google.gson.annotations.SerializedName;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;


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
        LocalDate today = new LocalDate();
        ArrayList<BrokerClient> employers_in_open_enrollment = new ArrayList<BrokerClient>();
        for (BrokerClient brokerClient : brokerClients) {
            if (brokerClient.isInOpenEnrollment(today)
                && brokerClient.isAlerted()){
                employers_in_open_enrollment.add(brokerClient);
            }
        }
        return employers_in_open_enrollment;
    }

    public ArrayList<BrokerClient> getOpenEnrollmentsNotAlerted() {
        ArrayList<BrokerClient> employers_in_open_enrollment = new ArrayList<BrokerClient>();
        for (BrokerClient brokerClient : brokerClients) {
            if (brokerClient.isInOpenEnrollment(new LocalDate())
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
