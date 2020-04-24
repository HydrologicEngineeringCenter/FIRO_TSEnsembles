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



    @Test
    public void a_new_timeseries_can_be_generated_from_an_apply() throws Exception{
        TimeSeriesIdentifier ts_id = new TimeSeriesIdentifier("TestTS", Duration.parse("P1D"), Duration.parse("PT0S"), "ac-ft");
        TimeSeries source_ts = new ReferenceRegularIntervalTimeSeries(ts_id);
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

    @Test
    public void total_calculation_calculates_all_sums() throws Exception {
        TimeSeries source_ts = fixtures.load_regular_time_series_data("/timeseries_data/regular_1hour_1month.csv");
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