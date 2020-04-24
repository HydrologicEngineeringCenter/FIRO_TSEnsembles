package hec.timeseries;

import hec.ensemble.TestingPaths;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import hec.*;
import hec.TestFixtures;
import hec.timeseries.*;

public class RegularIntervalTest {

    TestFixtures fixtures = new TestFixtures();

    

    @Test
    public void test_create_timeseries_from_resource() throws Exception {

        TimeSeries ts = fixtures.load_regular_time_series_data("/timeseries_data/regular_15minutes_1day_inclusive.csv");
        assertNotNull(ts);
        assertEquals(1, ts.valueAt(0));
        assertEquals(96, ts.valueAt(95));
    }


    @Test
    public void timeseries_data_is_inserted_and_can_be_read_back() throws Exception{
        String fileName= TestingPaths.instance.getTempDir()+"/"+"timeseriestest.db";

        File file = new File(fileName);
        file.delete();
        try (TimeSeriesDatabase db = new JdbcTimeSeriesDatabase(fileName,
                JdbcTimeSeriesDatabase.CREATION_MODE.CREATE_NEW);
                
            ) {            

            TimeSeries ts = fixtures.load_regular_time_series_data("/timeseries_data/regular_1hour_1month.csv");
            
            assertNotNull(ts);                        
            assertEquals(1,ts.valueAt(0));
            db.write(ts);

            List<Identifier> catalog = db.getTimeSeriesIDs2();
            assertNotNull( catalog );
            assertTrue( catalog.contains( ts.identifier() ) );

            TimeSeries from_db = db.getTimeSeries(ts.identifier(),
                                                  ts.firstTime(),
                                                  ts.lastTime() );
            assertEquals( ts.numberValues(), from_db.numberValues() );
            assertNotNull( from_db );
            assertEquals( 1, from_db.valueAt(0) );

        }
        
    }

    @Test
    public void daily_intervals_return_expected_times(){
        TimeSeriesIdentifier ts_id = new TimeSeriesIdentifier("TestTS", Duration.parse("P1D"), Duration.parse("PT0S"), "ac-ft");
        TimeSeries ts = new ReferenceRegularIntervalTimeSeries(ts_id);
        ts.addRow( ZonedDateTime.of(2020,3,7,0,0,0,0,ZoneId.of("GMT-08:00")), 1.0);
        ts.addRow( ZonedDateTime.of(2020,3,8,0,0,0,0,ZoneId.of("GMT-08:00")), 2.0);
        ts.addRow( ZonedDateTime.of(2020,3,9,0,0,0,0,ZoneId.of("GMT-08:00")), 3.0);

        ZonedDateTime midnight_original_8th = ts.timeAt(1);
        ZonedDateTime  midnight_local_8th = midnight_original_8th.withZoneSameInstant(ZoneId.of("UTC"));
        assertEquals(2.0, ts.valueAt(1));
        assertEquals(8, midnight_local_8th.getDayOfMonth() );
        assertEquals(8, midnight_local_8th.getHour() );

        ZonedDateTime midnight_local_9th = ts.timeAt(2).withZoneSameInstant(ZoneId.of("UTC"));
        assertEquals(3.0, ts.valueAt(2));
        assertEquals(9, midnight_local_9th.getDayOfMonth() );
        assertEquals(8, midnight_local_9th.getHour() );

        assertEquals(3.0, ts.valueAt(midnight_local_9th));
    }

    @Test
    public void missing_values_are_filled_in_between_the_current_last_and_new(){
        TimeSeriesIdentifier ts_id = new TimeSeriesIdentifier("TestMissingFill", Duration.parse("PT1H"), Duration.parse("PT0S"), "cfs");
        TimeSeries ts = new ReferenceRegularIntervalTimeSeries(ts_id);
        ZonedDateTime first_time = ZonedDateTime.of( 2020,4,23,0,0,0,0,ZoneId.of("UTC"));
        ZonedDateTime end_time = ZonedDateTime.of( 2020,4,23,23,0,0,0,ZoneId.of("UTC"));
        ZonedDateTime middle_time = first_time.plusHours(12);
        final double FIRST_VALUE= 1.0;
        final double SECOND_VALUE= 2.0;
        final double MIDDLE_VALUE = 6.0;
        final double LAST_VALUE = 3.0;
        final double MISSING_VALUE = Double.NEGATIVE_INFINITY;
        ts.addRow( first_time, FIRST_VALUE);
        ts.addRow( first_time.plusHours(1), SECOND_VALUE );
        ts.addRow( end_time, LAST_VALUE);

        ts.addRow( middle_time, MIDDLE_VALUE);

        ZonedDateTime second_value = ts.timeAt(1);
        assertEquals( 1, second_value.getHour() );
        assertEquals( SECOND_VALUE, ts.valueAt(1) );

        double value = ts.valueAt(3);
        assertEquals(MISSING_VALUE, value );

        double third_value = ts.valueAt(middle_time);
        assertEquals(MIDDLE_VALUE, third_value);

        value = ts.valueAt(end_time);
        assertEquals(LAST_VALUE, value );
    }

    @Test
    public void values_at_times_before_current_start_are_prevented(){
        Exception exception = 
            assertThrows(
                RuntimeException.class,
                () -> {
                TimeSeriesIdentifier ts_id = new TimeSeriesIdentifier("TestMissingFill", Duration.parse("PT1H"), Duration.parse("PT0S"), "cfs");
                TimeSeries ts = new ReferenceRegularIntervalTimeSeries(ts_id);
                ZonedDateTime first_time = ZonedDateTime.of( 2020,4,23,0,0,0,0,ZoneId.of("UTC"));
                ZonedDateTime time_before_first = first_time.minusHours(5);
                final double FIRST_VALUE= 1.0;
                final double SECOND_VALUE= 2.0;        
                ts.addRow( first_time, FIRST_VALUE);
                ts.addRow( time_before_first, SECOND_VALUE );
            });
        assertTrue(exception.getMessage().contains("inserting data before"));

    }

}