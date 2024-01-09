package hec.dss.ensemble;

import hec.heclib.util.HecTime;
import hec.heclib.util.HecTimeArray;
import hec.io.TimeSeriesCollectionContainer;
import hec.io.TimeSeriesContainer;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HecTimeToZonedDateTimeTest {
    @Test
    void test24HourStartTime() {
        ZonedDateTime zdt = ZonedDateTime.of(2023, 1, 2, 0,
                0, 0, 0,
                TimeZone.getTimeZone("").toZoneId());

        TimeSeriesCollectionContainer tscc = new TimeSeriesCollectionContainer();
        TimeSeriesContainer tsc = new TimeSeriesContainer();

        HecTime t = new HecTime("01Jan2023","24:00");
        HecTime t2 = new HecTime("01Jan2024","24:00");
        HecTimeArray times = new HecTimeArray();
        times.set(new int[]{t.value(),t2.value()});
        tsc.set(new double[]{1,2},times);

        tscc.add(tsc);
        DssDatabase db = new DssDatabase("test");

        assertEquals(zdt, db.hecTimeToZonedDateTime(tscc.get(0).getStartTime()));
    }

    @Test
    void test24HourStartTimeMonthAddition() {
        ZonedDateTime zdt = ZonedDateTime.of(2024, 2, 1, 0,
                0, 0, 0,
                TimeZone.getTimeZone("").toZoneId());

        TimeSeriesCollectionContainer tscc = new TimeSeriesCollectionContainer();
        TimeSeriesContainer tsc = new TimeSeriesContainer();

        HecTime t = new HecTime("31Jan2024","24:00");
        HecTime t2 = new HecTime("01Feb2024","24:00");
        HecTimeArray times = new HecTimeArray();
        times.set(new int[]{t.value(),t2.value()});
        tsc.set(new double[]{1,2},times);

        tscc.add(tsc);
        DssDatabase db = new DssDatabase("test");

        assertEquals(zdt, db.hecTimeToZonedDateTime(tscc.get(0).getStartTime()));
    }

    @Test
    void test24HourStartTimeLeapYear() {
        ZonedDateTime zdtStart = ZonedDateTime.of(2024, 2, 29, 0,
                0, 0, 0,
                TimeZone.getTimeZone("").toZoneId());

        ZonedDateTime zdtEnd = ZonedDateTime.of(2024, 3, 1, 0,
                0, 0, 0,
                TimeZone.getTimeZone("").toZoneId());

        TimeSeriesCollectionContainer tscc = new TimeSeriesCollectionContainer();
        TimeSeriesContainer tsc = new TimeSeriesContainer();

        HecTime t = new HecTime("28Feb2024","24:00");
        HecTime t2 = new HecTime("29Feb2024","24:00");
        HecTimeArray times = new HecTimeArray();
        times.set(new int[]{t.value(),t2.value()});
        tsc.set(new double[]{1,2},times);

        tscc.add(tsc);
        DssDatabase db = new DssDatabase("test");

        assertEquals(zdtStart, db.hecTimeToZonedDateTime(tscc.get(0).getStartTime()));
        assertEquals(zdtEnd, db.hecTimeToZonedDateTime(tscc.get(0).getEndTime()));
    }
}
