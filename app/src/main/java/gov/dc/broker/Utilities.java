package gov.dc.broker;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

/**
 * Created by plast on 10/28/2016.
 */

public class Utilities {

    public static DateTime calculateOneYearOut(DateTime startDate) {
        DateTime oneYearOut = new DateTime(startDate.getYear() + 1, startDate.getMonthOfYear(), startDate.getDayOfMonth(), startDate.getHourOfDay(), startDate.getMinuteOfHour());
        return oneYearOut.minus(new Duration(1000*60*60*24));
    }

    public static LocalDate calculateOneYearOut(LocalDate startDate) {
        LocalDate oneYearOut = startDate.plusYears(1);
        return oneYearOut.minusDays(1);
    }

    public static String DateAsString(DateTime date){
        return DateTimeFormat.forPattern("MM/dd/yyyy").print(date);
    }
    public static String DateAsString(LocalDate date){
        return DateTimeFormat.forPattern("MM/dd/yyyy").print(date);
    }

    public static String DateAsMonthYear(DateTime date){
        return DateTimeFormat.forPattern("MMM dd, yyyy").print(date);
    }

    public static String DateAsMonthDay(LocalDate date){
        return DateTimeFormat.forPattern("MMM dd").print(date);
    }

    public static String DateAsMonthDayYear(LocalDate date){
        return DateTimeFormat.forPattern("MMM dd, yyyy").print(date);
    }

    public static long dateDifferenceDays(LocalDate start, LocalDate end) {
        return Days.daysBetween(start, end).getDays();
    }

    public static int colorFromEmployeeStatus(String statusString) {
        if (statusString.compareToIgnoreCase("not enrolled") == 0){
            return R.color.not_enrolled_color;
        }
        if (statusString.compareToIgnoreCase("waived") == 0){
            return R.color.waived_color;
        }
        if (statusString.compareToIgnoreCase("terminated") == 0){
            return R.color.terminated_color;
        }
        //if (statusString.compareToIgnoreCase("enrolled") == 0){
        // Everything else is getting the enrolled color...Maybe this should throw?
        return R.color.enrolled_color;
    }
}
