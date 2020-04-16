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
import java.time.ZoneId;
import java.util.List;

import hec.*;
import hec.TestFixtures;


public class TimeSeriesReadWriteTests {

    @Test
    public void measure_write_and_read_performance_5000_years() throws Exception{     
        // generate test data  
        ZonedDateTime gen_start = ZonedDateTime.now();      
        ZonedDateTime start = ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime end = start.plusYears(40000);
        Duration interval = Duration.parse("P1D");
        TimeSeriesIdentifier ts_id = new TimeSeriesIdentifier(
                                            "TestTS",
                                            interval,                                                      
                                            Duration.parse("PT0S"),
                                            "ac-ft"
                                            );

        TimeSeries reference_ts = new ReferenceRegularIntervalTimeSeries(
                                ts_id
                            );                            
        ZonedDateTime current = start;
        while( current.isBefore(end)){
            reference_ts.addRow(current, Math.random()*1000);
            current = current.plusDays(interval.toDays());
        }
        int halfway_point = (int)reference_ts.numberValues()/2;
        double halfway_value = reference_ts.valueAt(halfway_point);
        ZonedDateTime half_way_time = reference_ts.timeAt((halfway_point));
        System.out.println(reference_ts.numberValues());
        ZonedDateTime gen_end = ZonedDateTime.now();
        long time_to_gen = gen_end.toEpochSecond() - gen_start.toEpochSecond();
        System.out.println("Generation to " + time_to_gen + " seconds");
        // write test data

        
        String fileName= TestingPaths.instance.getTempDir()+"/"+"timeseriestest.db";

        File file = new File(fileName);
        file.delete();
        long time_to_write_reference = Long.MIN_VALUE;
        long time_to_read_reference = Long.MIN_VALUE;
        try (TimeSeriesDatabase db = new JdbcTimeSeriesDatabase(fileName,
                JdbcTimeSeriesDatabase.CREATION_MODE.CREATE_NEW);
                
            ) {            
                System.out.println("Writing Data");
                ZonedDateTime write_start = ZonedDateTime.now();
                db.write(reference_ts);
                ZonedDateTime write_end = ZonedDateTime.now();
                System.out.println("Done Writing Data");
                time_to_write_reference = write_end.toEpochSecond() - write_start.toEpochSecond();
                System.out.println("Writing took " + time_to_write_reference + " seconds");
                reference_ts = null; // free up memory;
                System.gc();
                System.out.println("Reading Data");
                ZonedDateTime read_start =ZonedDateTime.now();
                TimeSeries read_ts = db.getTimeSeries(ts_id,start,end);

                double read_halfway_value = read_ts.valueAt(halfway_point);
                ZonedDateTime read_halfway_time = read_ts.timeAt(halfway_point);

                assertEquals( halfway_value, read_halfway_value, .01);
                assertTrue( read_halfway_time.isEqual(half_way_time));

                ZonedDateTime read_end = ZonedDateTime.now();
                time_to_read_reference = read_end.toEpochSecond()  - read_start.toEpochSecond();
                System.out.println("Reading took " + time_to_read_reference + " seconds");
                System.out.println("Done Reading Data");
            }
        catch( Exception err )
        {
            err.printStackTrace();            
        }                

        
    }
    

}