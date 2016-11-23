
package gov.dc.broker.models.brokerclient;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Renewal {

    @SerializedName("benefit_group_name")
    @Expose
    public String benefitGroupName;
    @SerializedName("eligibility_rule")
    @Expose
    public String eligibilityRule;
    @SerializedName("health")
    @Expose
    public Health_ health;
    @SerializedName("dental")
    @Expose
    public Object dental;

}
