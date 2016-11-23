package gov.dc.broker;

import java.util.List;

import gov.dc.broker.models.roster.Employee;

/**
 * Created by plast on 11/16/2016.
 */

public class BrokerUtilities {
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
