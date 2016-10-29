package gov.dc.broker;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;

/**
 * Created by plast on 10/28/2016.
 */

public class Utilities {
    public static CharSequence DateAsString(DateTime date){
        return DateTimeFormat.forPattern("MMM dd, yyyy").print(date);
    }

    public static CharSequence DateAsMonthYear(DateTime date){
        return DateTimeFormat.forPattern("MMM dd, yyyy").print(date);
    }

    public static long dateDifferenceDays(DateTime start, DateTime end) {
        return (new Duration(start, end)).getStandardDays();
    }

}
