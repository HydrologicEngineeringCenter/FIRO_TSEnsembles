package hec.timeseries;

import hec.ensemble.TestingPaths;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;

import hec.*;
import hec.TestFixtures;

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


}