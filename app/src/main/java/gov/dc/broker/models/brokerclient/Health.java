
package gov.dc.broker.models.brokerclient;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Health {

    @SerializedName("reference_plan_name")
    @Expose
    public String referencePlanName;
    @SerializedName("reference_plan_HIOS_id")
    @Expose
    public String referencePlanHIOSId;
    @SerializedName("carrier_name")
    @Expose
    public String carrierName;
    @SerializedName("plan_type")
    @Expose
    public String planType;
    @SerializedName("metal_level")
    @Expose
    public String metalLevel;
    @SerializedName("plan_option_kind")
    @Expose
    public String planOptionKind;
    @SerializedName("plans_by")
    @Expose
    public String plansBy;
    @SerializedName("plans_by_summary_text")
    @Expose
    public String plansBySummaryText;
    @SerializedName("employer_contribution_by_relationship")
    @Expose
    public EmployerContributionByRelationship employerContributionByRelationship;
    @SerializedName("estimated_employer_max_monthly_cost")
    @Expose
    public Integer estimatedEmployerMaxMonthlyCost;
    @SerializedName("estimated_plan_participant_min_monthly_cost")
    @Expose
    public Integer estimatedPlanParticipantMinMonthlyCost;
    @SerializedName("estimated_plan_participant_max_monthly_cost")
    @Expose
    public Integer estimatedPlanParticipantMaxMonthlyCost;

}
