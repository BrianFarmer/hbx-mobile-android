
package gov.dc.broker.models.brokeragency;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.LocalDate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")

public class BrokerClient implements Serializable {

    @SerializedName("employer_name")
    @Expose
    public String employerName;
    @SerializedName("binder_payment_due")
    @Expose
    public LocalDate binderPaymentDue;
    @SerializedName("plan_year_begins")
    @Expose
    public LocalDate planYearBegins;
    @SerializedName("billing_report_date")
    @Expose
    public LocalDate billingReportDate;
    @SerializedName("active_general_agency")
    @Expose
    public Object activeGeneralAgency;
    @SerializedName("plan_years")
    @Expose
    public List<PlanYear> planYears = new ArrayList<PlanYear>();
    @SerializedName("employees_total")
    @Expose
    public Integer employeesTotal;
    @SerializedName("employer_details_url")
    @Expose
    public String employerDetailsUrl;
    @SerializedName("employee_roster_url")
    @Expose
    public String employeeRosterUrl;
    @SerializedName("contact_info")
    @Expose
    public List<ContactInfo> contactInfo = new ArrayList<ContactInfo>();

}
