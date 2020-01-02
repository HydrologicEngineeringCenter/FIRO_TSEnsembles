package hec.firo;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtility {

    //static DateTimeFormatter _formatter = DateTimeFormatter.ofPattern("MM/d/yyyy H:mm");
    static DateTimeFormatter _formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ZonedDateTime ParseDateTime(String dt)
    {
        // 11/3/2013 12:00
        return ZonedDateTime.parse(dt,_formatter);
    }


}
