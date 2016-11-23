
package gov.dc.broker.models.brokerclient;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class BrokerClientDetails {

    @SerializedName("employer_name")
    @Expose
    public String employerName;
    @SerializedName("open_enrollment_begins")
    @Expose
    public DateTime openEnrollmentBegins;
    @SerializedName("open_enrollment_ends")
    @Expose
    public DateTime openEnrollmentEnds;
    @SerializedName("plan_year_begins")
    @Expose
    public DateTime planYearBegins;
    @SerializedName("renewal_in_progress")
    @Expose
    public Boolean renewalInProgress;
    @SerializedName("renewal_application_available")
    @Expose
    public String renewalApplicationAvailable;
    @SerializedName("renewal_application_due")
    @Expose
    public String renewalApplicationDue;
    @SerializedName("binder_payment_due")
    @Expose
    public Object binderPaymentDue;
    @SerializedName("minimum_participation_required")
    @Expose
    public Integer minimumParticipationRequired;
    @SerializedName("billing_report_date")
    @Expose
    public DateTime billingReportDate;
    @SerializedName("active_general_agency")
    @Expose
    public String activeGeneralAgency;
    @SerializedName("plan_offerings")
    @Expose
    public PlanOfferings planOfferings;

}
