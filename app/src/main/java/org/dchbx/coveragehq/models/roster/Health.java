package org.dchbx.coveragehq.models.roster;

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
    public Double employerContribution;
    @SerializedName("employee_cost")
    @Expose
    public Double employeeCost;
    @SerializedName("total_premium")
    @Expose
    public Double totalPremium;
    @SerializedName("deductible")
    @Expose
    public String deductible;
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
    @SerializedName("summary_of_benefits_url")
    @Expose
    public String summaryOfBenefitsUrl;
    @SerializedName("provider_directory_url")
    @Expose
    public String provider_directory_url;
    @SerializedName("rx_formulary_url")
    @Expose
    public String RxFormularyUrl;
    @SerializedName("services_rates_url")
    @Expose
    public String servicesRatesUrl;
    @SerializedName("carrier_name")
    @Expose
    public String carrierName;
    @SerializedName("applied_aptc_amount_in_cent")
    @Expose
    public Double applied_aptc_amount_in_cent;
    @SerializedName("elected_aptc_pct")
    @Expose
    public double electedAptcPct;
    @SerializedName("health_link_id")
    @Expose
    public String healthLinkId;
    @SerializedName("hbx_enrollment_id")
    @Expose
    public String hbxEnrollmentId;
}