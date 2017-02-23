package gov.dc.broker;

import android.util.Log;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import gov.dc.broker.models.brokeragency.BrokerAgency;
import gov.dc.broker.models.brokeragency.BrokerClient;
import gov.dc.broker.models.brokeragency.ContactInfo;
import gov.dc.broker.models.employer.Employer;
import gov.dc.broker.models.employer.PlanYear;
import gov.dc.broker.models.roster.Dependent;
import gov.dc.broker.models.roster.Enrollment;
import gov.dc.broker.models.roster.Roster;
import gov.dc.broker.models.roster.RosterEntry;

/**
 * Created by plast on 11/16/2016.
 */

public class BrokerUtilities {
    private static String TAG = "BrokerUtilities";

    static class PlanStatus {
        public int statusStringId;
        public int statusColorId;
    }

    /*public static long daysLeft(gov.dc.broker.models.employer.PlanYear planYear, LocalDate today) throws Exception {
        if (planYear.openEnrollmentEnds.compareTo(today) < 0){
            return Utilities.dateDifferenceDays(today, planYear.planYearBegins);
        }
        return Utilities.dateDifferenceDays(today, planYear.openEnrollmentEnds);
    }*/

    public static long daysLeftToApplicationDue(gov.dc.broker.models.brokeragency.PlanYear planYear, LocalDate now) {
        return Utilities.dateDifferenceDays(now, planYear.renewalApplicationDue);
    }

    public static long daysLeft(gov.dc.broker.models.employer.PlanYear planYear, LocalDate today) throws Exception {
        if (BrokerUtilities.isInOpenEnrollment(planYear, today)){
            return Utilities.dateDifferenceDays(today, planYear.openEnrollmentEnds);
        }
        return Utilities.dateDifferenceDays(today, planYear.renewalApplicationDue);
    }

    public static long daysLeft(gov.dc.broker.models.brokeragency.PlanYear planYear, LocalDate today) throws Exception {
        if (BrokerUtilities.isInOpenEnrollment(planYear, today)){
            return Utilities.dateDifferenceDays(today, planYear.openEnrollmentEnds);
        }
        return Utilities.dateDifferenceDays(today, planYear.renewalApplicationDue);
    }

    private static gov.dc.broker.models.brokeragency.PlanYear getPlanYear(BrokerClient brokerClient, LocalDate planYear) throws Exception {
        for (gov.dc.broker.models.brokeragency.PlanYear year : brokerClient.planYears) {
            if (year.planYearBegins == planYear){
                return year;
            }
        }
        throw new Exception("Plan year not found");
    }

    private static gov.dc.broker.models.employer.PlanYear getPlanYear(Employer employer, LocalDate planYear) {
        for (PlanYear year : employer.planYears) {
            if (year.planYearBegins == planYear){
                return year;
            }
        }
        return null;
    }

    public static boolean isInOpenEnrollment(PlanYear planYear, LocalDate date){
        if (planYear.openEnrollmentBegins == null
                || planYear.openEnrollmentEnds == null){
            return false;
        }
        if (planYear.openEnrollmentBegins.compareTo(date) > 0){
            return false;
        }
        Log.d(TAG, "planYear.openEnrollmentEnds:" + planYear.openEnrollmentEnds);
        Log.d(TAG, "now: " + date);
        Log.d(TAG, "compare: " + planYear.openEnrollmentEnds.compareTo(date));
        return planYear.openEnrollmentEnds.compareTo(date) >= 0;

    }

    public static boolean isInOpenEnrollment(gov.dc.broker.models.brokeragency.PlanYear planYear, LocalDate date){
        if (planYear.openEnrollmentBegins == null
                || planYear.openEnrollmentEnds == null){
            return false;
        }
        if (planYear.openEnrollmentBegins.isAfter(date)){
            return false;
        }
        if (planYear.openEnrollmentEnds.isBefore(date)){
            return false;
        }
        return true;

    }

    public static boolean anyAddresses(BrokerClient brokerClient) {
        for (ContactInfo contactInfo : brokerClient.contactInfo) {
            if (contactInfo.address1 != null
                && contactInfo.address1.length() > 0){
                return true;
            }
        }
        return false;
    }

    public static boolean anyPhoneNumbers(BrokerClient brokerClient) {
        for (ContactInfo contactInfo : brokerClient.contactInfo) {
            if (contactInfo.phone != null
                && contactInfo.phone.length() > 0){
                return true;
            }
            if (contactInfo.mobile != null
                && contactInfo.mobile.length() > 0){
                return true;
            }
        }
        return false;
    }

    public static boolean anyMobileNumbers(BrokerClient brokerClient) {
        for (ContactInfo contactInfo : brokerClient.contactInfo) {
            if (contactInfo.mobile != null
                    && contactInfo.mobile.length() > 0){
                return true;
            }
        }
        return false;
    }

    public static boolean anyEmailAddresses(BrokerClient brokerClient) {
        for (ContactInfo contactInfo : brokerClient.contactInfo) {
            if (contactInfo.emails != null
                && contactInfo.emails.size() > 0){
                return true;
            }
        }
        return false;
    }

    public static String getFullName(RosterEntry employee) {
        StringBuilder builder = new StringBuilder();
        if (employee.firstName != null
            && employee.firstName.length() > 0){
            builder.append(employee.firstName);
        }

        if (employee.middleName != null
            && employee.middleName.length() > 0){
            if (builder.length() > 0){
                builder.append(' ');
            }
            builder.append(employee.middleName);
        }

        if (employee.lastName != null
            && employee.lastName.length() > 0){
            if (builder.length() > 0){
                builder.append(' ');
            }
            builder.append(employee.lastName);
        }

        if (employee.nameSuffix != null
            && employee.nameSuffix.length() > 0){
            if (builder.length() > 0){
                builder.append(' ');
            }
            builder.append(employee.nameSuffix);
        }

        return builder.toString();
    }

    public static Enrollment getEnrollmentForCoverageYear(RosterEntry employee, LocalDate date) throws Exception {
        for (Enrollment enrollment : employee.enrollments) {
            if (enrollment.startOn.compareTo(date) == 0){
                return enrollment;
            }
        }
        throw new Exception("bad date, no enrollment found with the date");
    }

    public static boolean isAddressEmpty(ContactInfo contactInfo) {
        return contactInfo.address1 == null || contactInfo.address1.length() < 1;
    }

    public static boolean isPrimaryOffice(ContactInfo contactInfo) {
        return contactInfo.first.equalsIgnoreCase("primary")
                && contactInfo.last.equalsIgnoreCase("office");
    }

    public static boolean isAlerted(gov.dc.broker.models.brokeragency.PlanYear planYear) {
        return false;
    }

    public static boolean isAlerted(PlanYear planYear) {
        return false;
    }

    public static gov.dc.broker.models.brokeragency.PlanYear getPlanYearForCoverageYear(BrokerClient brokerclient, LocalDate coverageYear) throws Exception {
        for (gov.dc.broker.models.brokeragency.PlanYear planYear : brokerclient.planYears) {
            if (planYear.planYearBegins == coverageYear){
                return planYear;
            }
        }
        throw new Exception("coverageYear not found in plan years");
    }

    public static PlanYear getPlanYearForCoverageYear(Employer employer, LocalDate coverageYear) throws Exception {
        for (PlanYear planYear : employer.planYears) {
            if (planYear.planYearBegins.compareTo(coverageYear) == 0){
                return planYear;
            }
        }
        return null;
    }

    public static int getEmployeesNeeded(gov.dc.broker.models.brokeragency.PlanYear planYearForCoverageYear) {
        if (planYearForCoverageYear.minimumParticipationRequired == null) {
            return -1;
        }

        if (planYearForCoverageYear.employeesEnrolled == null){
            if (planYearForCoverageYear.employeesWaived == null){
                return planYearForCoverageYear.minimumParticipationRequired;
            }
            return planYearForCoverageYear.minimumParticipationRequired - planYearForCoverageYear.employeesWaived;
        }

        if (planYearForCoverageYear.employeesWaived == null){
            return planYearForCoverageYear.minimumParticipationRequired - planYearForCoverageYear.employeesEnrolled;
        }

        return planYearForCoverageYear.minimumParticipationRequired - (planYearForCoverageYear.employeesEnrolled + planYearForCoverageYear.employeesWaived);
    }

    public static List<BrokerClient> getBrokerClientsInOpenEnrollment(BrokerAgency brokerAgency, LocalDate date) {
        ArrayList<BrokerClient> brokerClientsInOpenEnrollment = new ArrayList<>();
        for (BrokerClient brokerClient : brokerAgency.brokerClients) {
            if (isInOpenEnrollment(brokerClient, date)){
                brokerClientsInOpenEnrollment.add(brokerClient);
            }
        }
        return brokerClientsInOpenEnrollment;
    }

    private static boolean isInOpenEnrollment(BrokerClient brokerClient, LocalDate date) {
        for (gov.dc.broker.models.brokeragency.PlanYear planYear : brokerClient.planYears) {
            if (isInOpenEnrollment(planYear, date)){
                return true;
            }
        }
        return false;
    }

    public static gov.dc.broker.models.brokeragency.PlanYear getPendingRenewalPlanYear(BrokerClient brokerClient) throws Exception {
        for (gov.dc.broker.models.brokeragency.PlanYear planYear : brokerClient.planYears) {
            if (planYear.renewalInProgress){
                return planYear;
            }
        }
        throw new Exception("No plan year in renewal");
    }

    public static int daysLeftToRenewal(gov.dc.broker.models.brokeragency.PlanYear planYear, LocalDate now) {
        return Days.daysBetween(planYear.openEnrollmentBegins, now).getDays();
    }

    public static boolean isPlanYearAlerted(gov.dc.broker.models.brokeragency.PlanYear planYear) {
        return (planYear.employeesEnrolled + planYear.employeesWaived < planYear.minimumParticipationRequired);
    }

    public static boolean isInRenewal(gov.dc.broker.models.brokeragency.PlanYear planYear) {
        return planYear.renewalInProgress;
    }

    public static gov.dc.broker.models.brokeragency.PlanYear getLastestPlanYear(List<gov.dc.broker.models.brokeragency.PlanYear> planYears) {
        if (planYears == null) {
            return null;
        }
        LocalDate date = new LocalDate(2000, 1, 1);
        gov.dc.broker.models.brokeragency.PlanYear foundPlanYear = null;
        for (gov.dc.broker.models.brokeragency.PlanYear planYear : planYears) {
            if (planYear.planYearBegins.compareTo(date) > 0){
                date = planYear.planYearBegins;
                foundPlanYear = planYear;
            }
        }
        return foundPlanYear;
    }

    public static String getBrokerClientId(BrokerAgency brokerAgency, int index){
        BrokerClient brokerClient = brokerAgency.brokerClients.get(index);
        return getBrokerClientId(brokerClient);
    }

    public static String getBrokerClientId(BrokerClient brokerClient){
        return brokerClient.employerDetailsUrl;
    }

    public static BrokerClient getBrokerClient(BrokerAgency brokerAgency, String employerId) throws Exception {
        for (BrokerClient brokerClient : brokerAgency.brokerClients) {
            if (brokerClient.employerDetailsUrl.compareTo(employerId) == 0){
                return brokerClient;
            }
        }
        throw new CoverageException("broker client not found");
    }

    public static String getEmployeeId(BrokerAgency brokerAgency, String employerId, String rosterId) {
        return null;
    }

    public static String getRosterUrl(BrokerAgency brokerAgency, String employerId) throws Exception {
        BrokerClient brokerClient = getBrokerClient(brokerAgency, employerId);
        return brokerClient.employeeRosterUrl;
    }

    public static RosterEntry getRosterEntry(Roster roster, String employeeId) throws CoverageException {
        for (RosterEntry rosterEntry : roster.roster) {
            if (rosterEntry.id.compareTo(employeeId) == 0) {
                return rosterEntry;
            }
        }
        throw new CoverageException("Roster entry not found");
    }

    public static gov.dc.broker.models.brokeragency.PlanYear getMostRecentPlanYear(BrokerClient brokerClient) {
        gov.dc.broker.models.brokeragency.PlanYear mostRecentPlanYear = null;
        for (gov.dc.broker.models.brokeragency.PlanYear planYear : brokerClient.planYears) {
            if (mostRecentPlanYear == null){
                mostRecentPlanYear = planYear;
            } else {
                if (planYear.planYearBegins.compareTo(mostRecentPlanYear.planYearBegins) > 0){
                    mostRecentPlanYear = planYear;
                }
            }
        }
        return mostRecentPlanYear;
    }

    public static PlanYear getMostRecentPlanYear(Employer employer) {
        PlanYear mostRecentPlanYear = null;
        for (PlanYear planYear : employer.planYears) {
            if (mostRecentPlanYear == null){
                mostRecentPlanYear = planYear;
            } else {
                if (planYear.planYearBegins.compareTo(mostRecentPlanYear.planYearBegins) > 0){
                    mostRecentPlanYear = planYear;
                }
            }
        }
        return mostRecentPlanYear;
    }

    public static Enrollment getPlanYearForCoverageYear(RosterEntry employee, LocalDate coverageYear) {
        for (Enrollment enrollment : employee.enrollments) {
            if (enrollment.startOn.compareTo(coverageYear) == 0){
                return enrollment;
            }
        }
        return null;
    }

    public static PlanStatus getPlanStatus(PlanYear planYear, LocalDate today) {
        PlanStatus planStatus = new PlanStatus();

        if (planYear !=  null) {
            boolean inOpenEnrollment = BrokerUtilities.isInOpenEnrollment(planYear, today);
            if (inOpenEnrollment) {
                if (BrokerUtilities.isAlerted(planYear)) {
                    planStatus.statusStringId = R.string.minimum_not_met;
                    planStatus.statusColorId = R.color.alertColor;
                } else {
                    planStatus.statusStringId = R.string.minimum_met;
                    planStatus.statusColorId = R.color.open_enrollment_minimum_met;
                }
            } else {
                if (planYear.renewalInProgress) {
                    planStatus.statusStringId = R.string.renewal_in_progress;
                    planStatus.statusColorId = R.color.in_renewal;
                } else {
                    if (planYear.planYearBegins.plusYears(1).compareTo(today) < 0){
                        planStatus.statusStringId = R.string.coverage_expired;
                        planStatus.statusColorId = R.color.textgray;
                    } else {
                        planStatus.statusStringId = R.string.active;
                        planStatus.statusColorId = R.color.textgray;
                    }
                }
            }
        }

        return planStatus;
    }

    public enum BrokerClientStatus {
        InOpenEnrollmentAlerted,
        InOpenEnrollmentNotAlerted,
        InRenewal,
        Other
    };

    public static BrokerClientStatus getBrokerClientStatus(gov.dc.broker.models.employer.PlanYear planYear, LocalDate date) {
        if (planYear.planYearBegins == null){
            return BrokerClientStatus.Other;
        }

        if (planYear.planYearBegins.compareTo(date) < 0){
            return BrokerClientStatus.Other;
        }


        if (planYear.openEnrollmentBegins != null
                && planYear.openEnrollmentEnds != null
                && planYear.openEnrollmentBegins.compareTo(date) <=0
                && planYear.openEnrollmentEnds.compareTo(date) >= 0) {
            if (planYear.employeesEnrolled == null
                    && (planYear.employeesWaived == null
                    || planYear.employeesWaived < planYear.minimumParticipationRequired)){
                return BrokerClientStatus.InOpenEnrollmentAlerted;
            }
            if (planYear.employeesWaived == null
                    && planYear.employeesEnrolled < planYear.minimumParticipationRequired) {
                return BrokerClientStatus.InOpenEnrollmentAlerted;
            }
            if (planYear.employeesEnrolled + planYear.employeesWaived < planYear.minimumParticipationRequired) {
                return BrokerClientStatus.InOpenEnrollmentAlerted;
            }
            return BrokerClientStatus.InOpenEnrollmentNotAlerted;
        }
        if (planYear.renewalInProgress){
            return BrokerClientStatus.InRenewal;
        }
        return BrokerClientStatus.Other;
    }

    public static BrokerClientStatus getBrokerClientStatus(gov.dc.broker.models.brokeragency.PlanYear planYear, LocalDate date) {
        if (planYear.planYearBegins == null){
            return BrokerClientStatus.Other;
        }

        if (planYear.planYearBegins.compareTo(date) < 0){
            return BrokerClientStatus.Other;
        }


        if (planYear.openEnrollmentBegins != null
                && planYear.openEnrollmentEnds != null
                && planYear.openEnrollmentBegins.compareTo(date) <=0
                && planYear.openEnrollmentEnds.compareTo(date) >= 0) {
            if (planYear.employeesEnrolled == null
                && (planYear.employeesWaived == null
                    || planYear.employeesWaived < planYear.minimumParticipationRequired)){
                return BrokerClientStatus.InOpenEnrollmentAlerted;
            }
            if (planYear.employeesWaived == null
                && planYear.employeesEnrolled < planYear.minimumParticipationRequired) {
                return BrokerClientStatus.InOpenEnrollmentAlerted;
            }
            if (planYear.employeesEnrolled + planYear.employeesWaived < planYear.minimumParticipationRequired) {
                return BrokerClientStatus.InOpenEnrollmentAlerted;
            }
            return BrokerClientStatus.InOpenEnrollmentNotAlerted;
        }

        if (planYear.openEnrollmentEnds != null
            && planYear.openEnrollmentEnds.compareTo(date) < 0
            && planYear.planYearBegins != null
            && date.compareTo(planYear.planYearBegins) < 0) {
            return BrokerClientStatus.Other;
        }

        if (planYear.renewalInProgress){
            return BrokerClientStatus.InRenewal;
        }
        return BrokerClientStatus.Other;
    }

    static class EmployeeCounts {
        public int Enrolled;
        public int Waived;
        public int NotEnrolled;
        public int Terminated;
        public int Total;
    }

    public static EmployeeCounts getEmployeeCounts(Roster roster, LocalDate coverageDate) {
        EmployeeCounts employeeCounts = new EmployeeCounts();

        for (RosterEntry entry : roster.roster) {
            for (Enrollment enrollment : entry.enrollments) {
                if (enrollment.startOn.compareTo(coverageDate) == 0
                        && enrollment.health != null) {
                    if (enrollment.health.status.compareToIgnoreCase("Enrolled") == 0) {
                        employeeCounts.Enrolled++;
                    } else if (enrollment.health.status.compareToIgnoreCase("Waived") == 0) {
                        employeeCounts.Waived++;
                    } else if (enrollment.health.status.compareToIgnoreCase("Not Enrolled") == 0) {
                        employeeCounts.NotEnrolled++;
                    } else if (enrollment.health.status.compareToIgnoreCase("Terminated") == 0){
                        employeeCounts.Terminated ++;
                    }
                }
            }
        }
        employeeCounts.Total = employeeCounts.Enrolled + employeeCounts.Waived + employeeCounts.NotEnrolled;
        return employeeCounts;
    }

    static class Totals {
        public double employerTotal = 0.0f;
        public double employeeTotal = 0.0f;
        public double total = 0.0f;
    }

    public static Totals calcTotals(Roster roster, LocalDate startOnDate){
        Totals totals = new Totals();

        for (RosterEntry rosterEntry: roster.roster) {
            for (Enrollment enrollment : rosterEntry.enrollments) {
                if (enrollment.startOn.compareTo(startOnDate) == 0
                    && enrollment.health != null
                    && enrollment.health.status.compareToIgnoreCase("not enrolled") != 0){
                    totals.employeeTotal += enrollment.health.employeeCost;
                    totals.employerTotal += enrollment.health.employerContribution;
                    totals.total += enrollment.health.totalPremium;
                }
            }
        }
        return totals;
    }

    public static String getFullName(Dependent dependent) {
        String fullName = "";
        if (dependent.firstName != null
                && !dependent.firstName.isEmpty()){
            fullName = dependent.firstName;
        }

        if (dependent.middleName != null
                && !dependent.middleName.isEmpty()){
            if (!fullName.isEmpty()){
                fullName += " ";
            }
            fullName += dependent.middleName;
        }

        if (dependent.lastName != null){
            if (!fullName.isEmpty()){
                fullName += " ";
            }
            fullName += dependent.lastName;
        }

        return fullName;
    }
}
