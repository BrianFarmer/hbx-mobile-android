
package gov.dc.broker.models.brokerclient;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class PlanOfferings {

    @SerializedName("active")
    @Expose
    public List<Active> active = new ArrayList<>();
    @SerializedName("renewal")
    @Expose
    public List<Active> renewal = new ArrayList<>();

}
