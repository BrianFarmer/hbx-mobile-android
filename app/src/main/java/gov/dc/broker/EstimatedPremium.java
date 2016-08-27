package gov.dc.broker;

import com.google.gson.annotations.SerializedName;

/**
 * Created by plast on 7/28/2016.
 */
public class EstimatedPremium {
    @SerializedName("billing_report_date")
    public String billingReportDate;

    @SerializedName("total_premium")
    public double totalPremium;

    @SerializedName("employee_contribution")
    public double employeeContribution;

    @SerializedName("employer_contribution")
    public double employerContribution;
}
