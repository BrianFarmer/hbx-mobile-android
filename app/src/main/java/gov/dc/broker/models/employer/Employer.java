
package gov.dc.broker.models.employer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Employer {

    @SerializedName("employer_name")
    @Expose
    public String employerName;
    @SerializedName("binder_payment_due")
    @Expose
    public LocalDate binderPaymentDue;
    @SerializedName("billing_report_date")
    @Expose
    public LocalDate billingReportDate;
    @SerializedName("active_general_agency")
    @Expose
    public String activeGeneralAgency;
    @SerializedName("plan_years")
    @Expose
    public List<PlanYear> planYears = new ArrayList<PlanYear>();

}
