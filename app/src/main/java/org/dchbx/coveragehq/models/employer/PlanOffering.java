
package org.dchbx.coveragehq.models.employer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PlanOffering {

    @SerializedName("benefit_group_name")
    @Expose
    public String benefitGroupName;
    @SerializedName("eligibility_rule")
    @Expose
    public String eligibilityRule;
    @SerializedName("health")
    @Expose
    public Health health;
    @SerializedName("dental")
    @Expose
    public Dental dental;

}
