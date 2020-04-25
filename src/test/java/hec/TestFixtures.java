package hec;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.time.ZonedDateTime;
import java.time.Duration;

import hec.timeseries.*;
/**
 * Helper for tests to load data into time series object from CSV files
 * 
 * @author Michael Neilson <michael.a.neilson@usace.army.mil>
 */
public class TestFixtures {

    public TimeSeries load_regular_time_series_data(String resource, Class ts_class_name) throws Exception{

        try( BufferedReader test_data_stream = new BufferedReader( 
                                                    new InputStreamReader(
                                                        getClass().getResourceAsStream(resource))
                                                    );
            ){
                                
                String meta_data_line = test_data_stream.readLine();
                String meta_parts[] = meta_data_line.split(",");
                String interval = null;
                String units = null;
                String name = null;
                String duration = "PT0S";
                for( String part: meta_parts){
                    String key_val[] = part.split(":");
                    String key = key_val[0].trim();
                    String val = key_val[1].trim();
                    if( "interval".equalsIgnoreCase(key)){
                        interval = val;
                    } else if( "units".equalsIgnoreCase(key)){
                        units = val;
                    } else if( "name".equalsIgnoreCase(key)){
                        name = val;
                    } else if( "duration".equalsIgnoreCase(key)){
                        duration = val;
                    }
                }
                TimeSeriesIdentifier ts_id = new TimeSeriesIdentifier(name, Duration.parse(interval), Duration.parse(duration), units);
                Constructor<TimeSeries> ts_class_constructor = ts_class_name.getConstructor(TimeSeriesIdentifier.class);
                TimeSeries ts = ts_class_constructor.newInstance(ts_id);
                
                String line = null;
                while( (line = test_data_stream.readLine() ) != null ){
                    String parts[] = line.split(",");
                    ZonedDateTime time = ZonedDateTime.parse(parts[0]);
                    double value = Double.parseDouble(parts[1]);
                    ts.addRow(time, value);
                } 
                return ts;
        }         
    }
    
}