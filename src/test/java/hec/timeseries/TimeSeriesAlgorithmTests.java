package hec.timeseries;

import hec.ensemble.TestingPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import hec.*;
import hec.TestFixtures;


public class TimeSeriesAlgorithmTests { 
    TestFixtures fixtures = new TestFixtures();
    
    private static Stream<Class> timeseries_class_list() {
        return Stream.of(ReferenceRegularIntervalTimeSeries.class, BlockedRegularIntervalTimeSeries.class);
    }

    private static TimeSeries create_class_instance(Class ts_class_type, TimeSeriesIdentifier ts_id)
            throws Exception {
        Constructor<TimeSeries> ts_class_constructor = ts_class_type.getConstructor(TimeSeriesIdentifier.class);
        return ts_class_constructor.newInstance(ts_id);
    }

    @ParameterizedTest
    @MethodSource("timeseries_class_list")
    public void a_new_timeseries_can_be_generated_from_an_apply(Class ts_class_type) throws Exception{
        TimeSeriesIdentifier ts_id = new TimeSeriesIdentifier("TestTS", Duration.parse("P1D"), Duration.parse("PT0S"), "ac-ft");
        TimeSeries source_ts = create_class_instance(ts_class_type, ts_id);
        source_ts.addRow( ZonedDateTime.of(2020,3,7,0,0,0,0,ZoneId.of("GMT-08:00")), 1.0);
        source_ts.addRow( ZonedDateTime.of(2020,3,8,0,0,0,0,ZoneId.of("GMT-08:00")), 2.0);
        source_ts.addRow( ZonedDateTime.of(2020,3,9,0,0,0,0,ZoneId.of("GMT-08:00")), 3.0);

        TimeSeriesIdentifier derived_ts_id = new TimeSeriesIdentifier("Derived TS", Duration.parse("P1D"), Duration.parse("PT0S"), "ac-ft");
        TimeSeries derived_ts = source_ts.applyFunction( (time, value ) -> {
            return value+1;
        },derived_ts_id);

        assertNotNull(derived_ts);
        assertEquals(derived_ts_id, derived_ts.identifier());
        assertEquals(source_ts.valueAt(0)+1, derived_ts.valueAt(0));
        assertEquals(source_ts.valueAt(1)+1, derived_ts.valueAt(1));
        assertEquals(source_ts.valueAt(2)+1, derived_ts.valueAt(2));

        
        TimeSeriesIdentifier difference_ts_id = new TimeSeriesIdentifier("Differences", Duration.parse("P1D"), Duration.parse("P1D"), "ac-ft");
        AtomicReference<Double> previous = new AtomicReference<>();
        previous.set( Double.NEGATIVE_INFINITY );
        TimeSeries difference_ts = source_ts.applyFunction( (time,value ) -> {
            if( previous.get() == Double.NEGATIVE_INFINITY ){
                previous.set(value);
                return Double.NEGATIVE_INFINITY;
            } else {
                double diff = value - previous.get();
                previous.set(value);
                return diff;
            }
        }, difference_ts_id);

        assertNotNull(difference_ts);
        assertEquals(source_ts.firstTime().plusDays(1), difference_ts.firstTime() );
        assertEquals(1, difference_ts.valueAt(0));

    }

    @ParameterizedTest
    @MethodSource("timeseries_class_list")
    public void total_calculation_calculates_all_sums(Class ts_class_type) throws Exception {
        TimeSeries source_ts = fixtures.load_regular_time_series_data("/timeseries_data/regular_1hour_1month.csv", ts_class_type);
        assertNotNull(source_ts);

        TimeSeriesIdentifier total_ts_id = new TimeSeriesIdentifier("Totals", Duration.parse("PT1H"), Duration.parse("PT1H"), "raw");
        WindowFunction row_function = new WindowFunction(){
            ArrayList<Double> values_in_window = new ArrayList<>();            
            @Override
            public void start(ZonedDateTime start_of_window_time) {                                
                values_in_window.clear();
            }
        
            @Override
            public double end(ZonedDateTime end_of_window_time) {
                if( values_in_window.size() > 14 ){
                    Double total = values_in_window.stream().reduce(0.0, (subtotal, current) -> subtotal + current );                
                    return total.doubleValue();
                } else {
                    return Double.NEGATIVE_INFINITY;
                }                
            }
        
            @Override
            public void apply_slice(ZonedDateTime time, double value) {                
                values_in_window.add(value);
            }
        };

        AggregateWindow window_function = new AggregateWindow(){
            ZonedDateTime start = null;
            @Override
            public boolean running() {                
                return false;
            }
        
            @Override
            public boolean isStart(ZonedDateTime time) {
                boolean the_return = start == null && time.getHour() == 0;                
                start = time;
                return the_return;
            }
        
            @Override
            public boolean isEnd(ZonedDateTime time) {                
                return start != null && !time.isEqual(start) && time.getHour() == 0;
            }
        
            @Override
            public Duration interval() {                
                return Duration.parse("P1D");
            }
        };
        TimeSeries total_ts = source_ts.applyFunction(
            row_function, 
            window_function,
            total_ts_id
        );
        assertNotNull(total_ts);
        assertTrue( total_ts.numberValues() > 0 );
        assertEquals(325, total_ts.valueAt(0));
        assertTrue(ZonedDateTime.parse("2020-01-02T00:00:00+00:00").isEqual(total_ts.firstTime()));
    }
}