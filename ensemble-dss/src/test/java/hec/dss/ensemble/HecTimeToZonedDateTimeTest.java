package hec.dss.ensemble;

import hec.heclib.util.HecTime;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HecTimeToZonedDateTimeTest {
    @Test
    void test24HourStartTime() {
        ZonedDateTime zdt = ZonedDateTime.of(2023, 1, 2, 0,
                0, 0, 0,
                TimeZone.getTimeZone("").toZoneId());

        HecTime t = new HecTime("01Jan2023","24:00");
        assertEquals(zdt, DssDatabase.getZonedDateTime(t));
    }

    @Test
    void test24HourStartTimeMonthAddition() {
        ZonedDateTime zdt = ZonedDateTime.of(2024, 2, 1, 0,
                0, 0, 0,
                TimeZone.getTimeZone("").toZoneId());

        HecTime t = new HecTime("31Jan2024","24:00");
        assertEquals(zdt, DssDatabase.getZonedDateTime(t));
    }

    @Test
    void test24HourStartTimeLeapYear() {
        ZonedDateTime zdtStart = ZonedDateTime.of(2024, 2, 29, 0,
                0, 0, 0,
                TimeZone.getTimeZone("").toZoneId());

        ZonedDateTime zdtEnd = ZonedDateTime.of(2024, 3, 1, 0,
                0, 0, 0,
                TimeZone.getTimeZone("").toZoneId());

        HecTime t = new HecTime("28Feb2024","24:00");
        HecTime t2 = new HecTime("29Feb2024","24:00");

        assertEquals(zdtStart, DssDatabase.getZonedDateTime(t));
        assertEquals(zdtEnd, DssDatabase.getZonedDateTime(t2));
    }
}
