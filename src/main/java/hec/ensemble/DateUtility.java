package hec.ensemble;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtility {


   // static String DateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    static DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    static ZonedDateTime parseDateTime(String dt)
    {
        ZonedDateTime zdt = ZonedDateTime.parse(dt, formatter);
        return zdt;
    }

    static String formatDate(ZonedDateTime t)
    {
        return t.format(formatter);
    }


}
