package hec.ensemble;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ZonedDateTimeTest {

    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()).withZone(ZoneId.of("GMT"));
    @Test
    public void dateTesting(){

        String dt = "2013-11-03 12:00:00";
        ZonedDateTime zdt = ZonedDateTime.parse(dt,fmt);
        assertEquals(3,zdt.getDayOfMonth());
        assertEquals(11,zdt.getMonthValue());
        assertEquals(2013,zdt.getYear());
        assertEquals(12,zdt.getHour());
        assertEquals(0,zdt.getMinute());
    }


    /**
     * Test  TreeMap.floorKey with Key of ZonedDateTime
     * @throws Exception
     */
    @Test
    public void testTimeTolerance()throws Exception
    {
        TreeMap<ZonedDateTime,Integer> items = new TreeMap<ZonedDateTime, Integer>();
        ZonedDateTime t = ZonedDateTime.of(2013, 11, 1, 12, 0, 0, 0, ZoneId.of("GMT"));

        ZonedDateTime nov1 = t;
        ZonedDateTime nov3 = t.plusDays(2);
        ZonedDateTime nov5 = t.plusDays(4);
        ZonedDateTime nov10 = t.plusDays(9);
        ZonedDateTime nov15 = t.plusDays(14);

        items.put(nov3,3);
        items.put(nov5,5);
        items.put(nov15,15);

        t =  items.floorKey(nov3);

        // lookup existing nov 3
        int toleranceHours = 24*5;
        int c =  t.plusHours(toleranceHours).compareTo(nov3);
        assertEquals(1,c);

        // lookup missing nov 1 (before beginning)
        t =  items.floorKey(nov1);
        assertNull(t);

        // lookup nov10 without enough tolerance
        toleranceHours=4;
        t = items.floorKey(nov10);
        c =  t.plusHours(toleranceHours).compareTo(nov10);
        assertEquals(-1,c);

        // lookup nov10 with just enough tolerance
        toleranceHours=24*5;
        t = items.floorKey(nov10);
        c =  t.plusHours(toleranceHours).compareTo(nov10);
        assertEquals(0,c);


    }
}
