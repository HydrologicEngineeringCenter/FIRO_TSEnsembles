package hec.firo;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtility {


    static DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    public static ZonedDateTime ParseDateTime(String dt)
    {
        //2013-11-03 12:00:00
        String gmt =dt.substring(0,10)+"T"+dt.substring(11)+"Z";

        ZonedDateTime zdt = ZonedDateTime.parse(gmt, formatter);
        return zdt;
    }


}
