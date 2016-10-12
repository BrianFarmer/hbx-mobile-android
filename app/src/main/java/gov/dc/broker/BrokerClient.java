package gov.dc.broker;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BrokerClient implements Serializable {

    @SerializedName("employer_name")
    public String employerName;

    @SerializedName("employer_details_url")
    public String employerDetailsUrl;

    @SerializedName("open_enrollment_begins")
    public Date openEnrollmentBegins;

    @SerializedName("open_enrollment_ends")
    public Date openEnrollmentEnds;

    @SerializedName("plan_year_begins")
    public Date planYearBegins;

    @SerializedName("renewal_in_progress")
    public boolean renewalInProgress;

    @SerializedName("renewal_application_available")
    public Date renewalApplicationAvailable;

    @SerializedName("renewal_application_due")
    public Date renewalApplicationDue;

    @SerializedName("binder_payment_due")
    public String binderPaymentDue;

    @SerializedName("employees_total")
    public int employeesTotal;

    @SerializedName("employees_enrolled")
    public int employeesEnrolled;

    @SerializedName("employees_waived")
    public int employessWaived;

    @SerializedName("minimum_participation_required")
    public int minimumParticipationRequired;

    @SerializedName("contact_info")
    public List<ContactInfo> contactInfo;

    @SerializedName("active_general_agency")
    public String generalAgency;

    public boolean isInOpenEnrollment(Date date){
        if (openEnrollmentBegins == null
            || openEnrollmentEnds == null){
            return false;
        }
        if (openEnrollmentBegins.after(date)){
            return false;
        }
        if (openEnrollmentEnds.before(date)){
            return false;
        }
        return true;
    }

    public int getEmployessNeeded(){
        return minimumParticipationRequired - (employeesEnrolled + employessWaived);
    }

    public int getEmployessNotCompleted(){
        return employeesTotal - (employeesEnrolled + employessWaived);
    }

    private long getDaysDiff(Date early, Date late){
        long diffMilliSeconds = late.getTime() - early.getTime();
        return diffMilliSeconds / 1000/60/60/24;
    }

    public long getDaysLeft() {
        return getDaysDiff(Calendar.getInstance().getTime(), openEnrollmentEnds);
    }

    public boolean isAlerted(){
        return (employeesEnrolled + employessWaived < minimumParticipationRequired);
    }

    public boolean anyEmailAddresses() {
        if (contactInfo == null){
            return false;
        }

        for (ContactInfo cur: contactInfo) {
            if (cur != null
                    && cur.emails != null
                    && cur.emails.size() > 0){
                return true;
            }
        }
        return false;
    }

    public boolean anyPhoneNumbers (){
        if (contactInfo == null){
            return false;
        }

        for (ContactInfo cur: contactInfo) {
            if (cur != null
                && ((cur.mobile != null
                     && !cur.mobile.isEmpty())
                    || (cur.phone != null
                        && !cur.phone.isEmpty()))){
                return true;
            }
        }
        return false;
    }

    public boolean anyMobileNumbers() {
        if (contactInfo == null){
            return false;
        }

        for (ContactInfo cur: contactInfo) {
            if (cur != null
                && cur.mobile != null
                && !cur.mobile.isEmpty()){
                return true;
            }
        }
        return false;
    }

    public boolean anyAddresses(){
        if (contactInfo == null){
            return false;
        }

        for (ContactInfo cur: contactInfo){
            if (cur != null
                && !cur.isAddressEmpty()){
                return true;
            }
        }
        return false;
    }
}
