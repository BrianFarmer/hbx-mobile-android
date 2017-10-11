package org.dchbx.coveragehq;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Iterator;
import java.util.List;

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

    public static LocalDate parseDate(String date){
        return DateTimeFormat.forPattern("MM/dd/yyyy").parseLocalDate(date);
    }

    public static String DateAsString(DateTime date){
        return DateTimeFormat.forPattern("MM/dd/yyyy").print(date);
    }
    public static String DateAsString(LocalDate date){
        return DateTimeFormat.forPattern("MM/dd/yyyy").print(date);
    }

    public static String DateTimeAsIso8601(DateTime date){
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        return fmt.print(date);
    }

    public static String DateAsIso8601(LocalDate date){
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        String dateString = fmt.print(date).substring(0,10);
        return dateString;
    }

    public static String DateAsMonthYear(DateTime date){
        return DateTimeFormat.forPattern("MMM dd, yyyy").print(date);
    }

    public static String DateAsMonthDay(LocalDate date){
        return DateTimeFormat.forPattern("MMM dd").print(date);
    }

    public static String DateAsMonthYear(LocalDate date){
        return DateTimeFormat.forPattern("MMM yy").print(date);
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

    //it's astounding that there's not a standard method for this before Java 8
    public static String join(List<String> strings, String delimeter) {
        StringBuffer buffer = new StringBuffer();
        for (Iterator<String> iterator = strings.iterator(); iterator.hasNext(); ) {
            String s = iterator.next();
            buffer.append(s);
            if (iterator.hasNext()) {
                buffer.append(delimeter);
            }
        }
        return buffer.toString();
    }

    public static Gson getGson(){
        final GsonBuilder builder = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
                .registerTypeAdapter(LocalTime.class, new LocalTimeSerializer());
        return builder.create();
    }

    public static String getJson(Object object){
        Gson gson = getGson();
        return gson.toJson(object);
    }
}
