
package gov.dc.broker.models.brokeragency;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.LocalDate;

import java.io.Serializable;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class PlanYear implements Serializable{

    @SerializedName("plan_year_begins")
    @Expose
    public LocalDate planYearBegins;
    @SerializedName("open_enrollment_begins")
    @Expose
    public LocalDate openEnrollmentBegins;
    @SerializedName("open_enrollment_ends")
    @Expose
    public LocalDate openEnrollmentEnds;
    @SerializedName("renewal_in_progress")
    @Expose
    public Boolean renewalInProgress;
    @SerializedName("renewal_application_available")
    @Expose
    public LocalDate renewalApplicationAvailable;
    @SerializedName("renewal_application_due")
    @Expose
    public LocalDate renewalApplicationDue;
    @SerializedName("state")
    @Expose
    public String state;
    @SerializedName("minimum_participation_required")
    @Expose
    public Integer minimumParticipationRequired;
    @SerializedName("employees_enrolled")
    @Expose
    public Integer employeesEnrolled;
    @SerializedName("employees_waived")
    @Expose
    public Integer employeesWaived;
    @SerializedName("employees_terminated")
    @Expose
    public Integer employeesTerminated;

}
