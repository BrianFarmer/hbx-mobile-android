package gov.dc.broker;

import org.joda.time.LocalDate;

import java.util.List;

import gov.dc.broker.models.roster.Employee;
import gov.dc.broker.models.roster.Health;
import gov.dc.broker.models.roster.Roster;

/**
 * Created by plast on 11/16/2016.
 */

public class BrokerUtilities {
    public static long daysLeft(BrokerClient brokerClient) {
        LocalDate today = new LocalDate();
        if (BrokerUtilities.isInOpenEnrollment(brokerClient, today)){
            return Utilities.dateDifferenceDays(today, brokerClient.openEnrollmentEnds.toLocalDate());
        }
        return Utilities.dateDifferenceDays(today, brokerClient.renewalApplicationDue.toLocalDate());
    }

    public static boolean isInOpenEnrollment(BrokerClient brokerClient, LocalDate date){
        if (brokerClient.openEnrollmentBegins == null
                || brokerClient.openEnrollmentEnds == null){
            return false;
        }
        if (brokerClient.openEnrollmentBegins.toLocalDate().isAfter(date)){
            return false;
        }
        if (brokerClient.openEnrollmentEnds.toLocalDate().isBefore(date)){
            return false;
        }
        return true;
    }

    static class EmployeeCounts {
        public int Enrolled;
        public int Waived;
        public int NotEnrolled;
        public int Total;
    }

    public static EmployeeCounts getEmployeeCounts(Roster roster, String coverageYear) {
        EmployeeCounts employeeCounts = new EmployeeCounts();

        for (Employee employee : roster.roster) {
            Health health;
            if (coverageYear.compareToIgnoreCase("active") == 0){
                health = employee.enrollments.active.health;
            } else {
                health = employee.enrollments.renewal.health;
            }

            if (health != null) {
                if (health.status.compareToIgnoreCase("Enrolled") == 0) {
                    employeeCounts.Enrolled++;
                } else if (health.status.compareToIgnoreCase("Waived") == 0) {
                    employeeCounts.Waived++;
                } else if (health.status.compareToIgnoreCase("Not Enrolled") == 0) {
                    employeeCounts.NotEnrolled++;
                }

                employeeCounts.Total ++;
            }
        }

        return employeeCounts;
    }

    static class Totals {
        public double employerTotal = 0.0f;
        public double employeeTotal = 0.0f;
        public double total = 0.0f;
    }

    public static Totals calcTotals(List<Employee> employees, boolean active){
        Totals totals = new Totals();

        for (Employee employee : employees) {
            if (active){
                if (employee.enrollments != null
                    && employee.enrollments.active != null
                    && employee.enrollments.active.health != null) {
                    totals.employeeTotal += employee.enrollments.active.health.employeeCost;
                    totals.employerTotal += employee.enrollments.active.health.employerContribution;
                    totals.total += employee.enrollments.active.health.totalPremium;
                }
            } else {
                if (employee.enrollments != null
                    && employee.enrollments.renewal != null
                    && employee.enrollments.renewal.health != null) {
                    totals.employeeTotal += employee.enrollments.renewal.health.employeeCost;
                    totals.employerTotal += employee.enrollments.renewal.health.employerContribution;
                    totals.total += employee.enrollments.renewal.health.totalPremium;
                }
            }
        }
        return totals;
    }
}
