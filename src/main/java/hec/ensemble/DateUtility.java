package hec.ensemble;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DateUtility has methods to format and parse ISO_DATE_TIME
 */
public class DateUtility {


   // static String DateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    private static DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Converts from ISO format String to ZonedDateTime
     * @param dt ISO date formatted string
     * @return returns ZonedDateTime
     */
    public static ZonedDateTime parseDateTime(String dt)
    {
        ZonedDateTime zdt = ZonedDateTime.parse(dt, formatter);
        return zdt;
    }

    /**
     * Formats ZonedDateTime into ISO_DATE_TIME String
     * @param t input ZonedDateTime
     * @return formatted string
     */
    public static String formatDate(ZonedDateTime t)
    {
        return t.format(formatter);
    }


}
