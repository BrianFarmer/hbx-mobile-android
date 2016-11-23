
package gov.dc.broker.models.roster;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Health {

    @SerializedName("status")
    @Expose
    public String status;
    @SerializedName("employer_contribution")
    @Expose
    public double employerContribution;
    @SerializedName("employee_cost")
    @Expose
    public double employeeCost;
    @SerializedName("total_premium")
    @Expose
    public double totalPremium;
    @SerializedName("plan_name")
    @Expose
    public String planName;
    @SerializedName("plan_type")
    @Expose
    public String planType;
    @SerializedName("metal_level")
    @Expose
    public String metalLevel;
    @SerializedName("benefit_group_name")
    @Expose
    public String benefitGroupName;
    @SerializedName("terminated_on")
    @Expose
    public String terminatedOn;
    @SerializedName("terminate_reason")
    @Expose
    public String terminateReason;

}
