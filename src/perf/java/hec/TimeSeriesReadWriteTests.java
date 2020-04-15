package hec;
import hec.ensemble.TestingPaths;
import hec.timeseries.ReferenceRegularIntervalTimeSeries;
import hec.timeseries.TimeSeries;
import hec.timeseries.TimeSeriesIdentifier;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import hec.*;
import hec.TestFixtures;


public class TimeSeriesReadWriteTests {

    @Test
    public void measure_write_and_read_performance() throws Exception{     
        // generate test data        
        ZonedDateTime start = ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime end = start.plusYears(50000);
        Duration interval = Duration.parse("PT1D");
        TimeSeries ts = new ReferenceRegularIntervalTimeSeries(
                            new TimeSeriesIdentifier(
                                                    "TestTS",
                                                     interval,                                                      
                                                     Duration.parse("PT0S"),
                                                     "ac-ft"
                                                    )
                            );                            
        ZonedDateTime current = start;
        while( current.isBefore(start)){
            ts.addRow(current, Math.random()*1000);
            current = current.plusSeconds(interval.getSeconds()));
        }


        // write test data

        // read test data

        fail("not yet implemented");
    }
    

}