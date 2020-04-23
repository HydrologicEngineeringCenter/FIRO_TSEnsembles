package hec.timeseries;

import hec.ensemble.TestingPaths;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.time.*;
import java.util.List;

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

            assertNotNull( from_db );
            assertEquals( 1, from_db.valueAt(0) );

        }

        //fail("test not yet implemented");
    }

    @Test
    public void daily_intervals_return_expected_times(){
        TimeSeriesIdentifier ts_id = new TimeSeriesIdentifier("TestTS", Duration.parse("P1D"), Duration.parse("PT0S"), "ac-ft");
        TimeSeries ts = new ReferenceRegularIntervalTimeSeries(ts_id);
        ts.addRow( ZonedDateTime.of(2020,3,7,0,0,0,0,ZoneId.of("GMT-08:00")), 1.0);
        ts.addRow( ZonedDateTime.of(2020,3,8,0,0,0,0,ZoneId.of("GMT-08:00")), 2.0);
        ts.addRow( ZonedDateTime.of(2020,3,9,0,0,0,0,ZoneId.of("GMT-08:00")), 3.0);

        ZonedDateTime midnight_local_8th = ts.timeAt(1).withZoneSameInstant(ZoneId.of("UTC"));
        assertEquals(2.0, ts.valueAt(1));
        assertEquals(9, midnight_local_8th.getDayOfMonth() );
        assertEquals(8, midnight_local_8th.getHour() );

        ZonedDateTime midnight_local_9th = ts.timeAt(2).withZoneSameInstant(ZoneId.of("UTC"));
        assertEquals(3.0, ts.valueAt(2));
        assertEquals(10, midnight_local_9th.getDayOfMonth() );
        assertEquals(8, midnight_local_9th.getHour() );
    }

}