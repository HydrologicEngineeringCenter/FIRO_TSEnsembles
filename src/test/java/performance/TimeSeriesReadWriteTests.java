package performance;

import hec.ensemble.TestingPaths;
import hec.timeseries.BlockedRegularIntervalTimeSeries;
import hec.timeseries.ReferenceRegularIntervalTimeSeries;
import hec.timeseries.TimeSeries;
import hec.timeseries.TimeSeriesIdentifier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.stream.Stream;
import java.time.ZoneId;

import hec.*;

class Measure{
    public String name;    
    public double write_time;
    public double read_time;
    public double file_size;
    
    public Measure(String name ){
        this.name = name;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" Took:").append(System.lineSeparator());
        sb.append("Write: ").append(write_time).append(" milliseconds").append(System.lineSeparator());
        sb.append("Read:  ").append(read_time).append(" milliseconds").append(System.lineSeparator());
        sb.append("Size:  ").append(String.format("%.04f",file_size/1024/1024)).append(System.lineSeparator());
        return sb.toString();
    }
}

public class TimeSeriesReadWriteTests {

    @Test
    public void measure_and_compare_regular_interval_time_series() throws Exception{        
        // generate test data  
        long gen_start = System.currentTimeMillis();
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
        TimeSeries blocked_ts = new BlockedRegularIntervalTimeSeries(
                                ts_id
                            );
                          
        java.util.SplittableRandom rng = new java.util.SplittableRandom();
        ZonedDateTime current = start;
        while( current.isBefore(end)){
            double value = rng.nextDouble()*1000;
            reference_ts.addRow(current, value);
            blocked_ts.addRow(current, value);            
            current = current.plusDays(interval.toDays());
        }
        
        //System.out.println(reference_ts.numberValues());
        long gen_end = System.currentTimeMillis();
        System.out.println("Data Generation took " + (gen_end - gen_start) + " milliseconds");
        Measure reference_measure = measure_write_and_read_performance(reference_ts, "Reference Regular",start,end);
        Measure blocked_measure = measure_write_and_read_performance(blocked_ts,"Blocked Regular",start,end);

        System.out.println(reference_measure);
        System.out.println(blocked_measure);
    }
    
    public Measure measure_write_and_read_performance(TimeSeries theData, String setname, ZonedDateTime start, ZonedDateTime end) throws Exception{     
        Measure measure = new Measure(setname);
        int halfway_point = (int)theData.numberValues()/2;
        double halfway_value = theData.valueAt(halfway_point);
        ZonedDateTime half_way_time = theData.timeAt((halfway_point));
        
        String fileName= TestingPaths.instance.getTempDir()+"/"+"timeseriesperftest.db";

        File file = new File(fileName);
        file.delete();        
        try (TimeSeriesDatabase db = new JdbcTimeSeriesDatabase(fileName,
                JdbcTimeSeriesDatabase.CREATION_MODE.CREATE_NEW);
                
            ) {                            
                long write_start = System.currentTimeMillis();
                db.write(theData);
                long write_end = System.currentTimeMillis();
                measure.file_size = file.length();                
                measure.write_time = write_end - write_start;
                System.out.println("Writing took " + measure.write_time + " ms");
                //theData = null; // free up memory;
                System.gc();
                System.out.println("Reading Data");
                long read_start = System.currentTimeMillis();
                TimeSeries read_ts = db.getTimeSeries(theData.identifier(),start,end);

                double read_halfway_value = read_ts.valueAt(halfway_point);
                ZonedDateTime read_halfway_time = read_ts.timeAt(halfway_point);

                assertEquals( halfway_value, read_halfway_value, .01);
                assertTrue( read_halfway_time.isEqual(half_way_time));

                long read_end = System.currentTimeMillis();
                measure.read_time = read_end - read_start;
                System.out.println("Reading took " + measure.read_time + " ms");
                System.out.println("Done Reading Data");
                System.out.println("File was " + measure.file_size/1024/1024  + " MB");
                return measure;
            }
        catch( Exception err )
        {
            err.printStackTrace();            
        }                

        return null;
    }
    

}