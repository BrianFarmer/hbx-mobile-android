
package org.dchbx.coveragehq.models.brokeragency;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class BrokerAgency implements Serializable {

    @SerializedName("broker_agency")
    @Expose
    public String brokerAgency;
    @SerializedName("broker_name")
    @Expose
    public String brokerName;
    @SerializedName("broker_clients")
    @Expose
    public List<BrokerClient> brokerClients = new ArrayList<BrokerClient>();

}
