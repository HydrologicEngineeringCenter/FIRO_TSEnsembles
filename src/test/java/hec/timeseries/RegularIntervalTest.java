package hec.timeseries;

import hec.ensemble.TestingPaths;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import hec.*;
import hec.TestFixtures;

public class RegularIntervalTest {

    TestFixtures fixtures = new TestFixtures();

    private static Stream<Arguments> class_and_resource_list() throws Exception{
        TestFixtures fix = new TestFixtures();
        ArrayList<String> resource_files = fix.load_lines("/timeseries_data/list_of_files.txt");
        ArrayList<Class> classes = (ArrayList<Class>) TestFixtures.timeseries_class_list().collect(Collectors.toList());
        
        ArrayList<Arguments> args = new ArrayList<>();
        for( Class c: classes){
            for( String file: resource_files ){
                args.add(Arguments.of(c,file));
            }
        }
        return args.stream();
    }

    @ParameterizedTest
    @MethodSource("hec.TestFixtures#timeseries_class_list")
    public void test_create_timeseries_from_resource(Class ts_class_type) throws Exception {

        TimeSeries ts = fixtures.load_regular_time_series_data("/timeseries_data/regular_15minutes_1day_inclusive.csv",ts_class_type);
        assertNotNull(ts);
        assertEquals(1, ts.valueAt(0));
        assertEquals(96, ts.valueAt(95));
    }


    @ParameterizedTest
    @MethodSource("hec.TestFixtures#timeseries_class_list")
    public void timeseries_data_is_inserted_and_can_be_read_back(Class ts_class_type) throws Exception{
        String fileName= TestingPaths.instance.getTempDir()+"/"+"timeseriestest.db";

        File file = new File(fileName);
        file.delete();
        try (TimeSeriesDatabase db = new JdbcTimeSeriesDatabase(fileName,
                JdbcTimeSeriesDatabase.CREATION_MODE.CREATE_NEW);                
            ) {            

            TimeSeries ts = fixtures.load_regular_time_series_data("/timeseries_data/regular_1hour_1month.csv", ts_class_type);
            
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

    @ParameterizedTest
    @MethodSource("hec.TestFixtures#timeseries_class_list")
    public void daily_intervals_return_expected_times(Class ts_class_type) throws Exception{
        TimeSeriesIdentifier ts_id = new TimeSeriesIdentifier("TestTS", Duration.parse("P1D"), Duration.parse("PT0S"), "ac-ft");
        TimeSeries ts = TestFixtures.create_class_instance(ts_class_type, ts_id);
        ZonedDateTime origFirst = ZonedDateTime.of(2020,3,7,0,0,0,0,ZoneId.of("GMT-08:00"));
        ZonedDateTime origMiddle = ZonedDateTime.of(2020,3,8,0,0,0,0,ZoneId.of("GMT-08:00"));
        ZonedDateTime origLast = ZonedDateTime.of(2020,3,9,0,0,0,0,ZoneId.of("GMT-08:00"));
        ts.addRow( origFirst, 1.0);
        ts.addRow( origMiddle, 2.0);
        ts.addRow( origLast, 3.0);

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
        assertTrue( origFirst.isEqual(ts.firstTime()) );
        ZonedDateTime calculatedLast = ts.lastTime();
        assertTrue( origLast.isEqual(calculatedLast) );  
        assertEquals( 3.0, ts.valueAt(origLast) );      
    }

    @ParameterizedTest
    @MethodSource("hec.TestFixtures#timeseries_class_list")
    public void missing_values_are_filled_in_between_the_current_last_and_new(Class ts_class_type) throws Exception{
        TimeSeriesIdentifier ts_id = new TimeSeriesIdentifier("TestMissingFill", Duration.parse("PT1H"), Duration.parse("PT0S"), "cfs");
        TimeSeries ts = TestFixtures.create_class_instance(ts_class_type, ts_id);
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

    @ParameterizedTest
    @MethodSource("hec.TestFixtures#timeseries_class_list")
    public void values_at_times_before_current_start_are_prevented(Class ts_class_type) throws Exception{
        Exception exception = 
            assertThrows(
                RuntimeException.class,
                () -> {
                TimeSeriesIdentifier ts_id = new TimeSeriesIdentifier("TestMissingFill", Duration.parse("PT1H"), Duration.parse("PT0S"), "cfs");
                TimeSeries ts = TestFixtures.create_class_instance(ts_class_type, ts_id);
                ZonedDateTime first_time = ZonedDateTime.of( 2020,4,23,0,0,0,0,ZoneId.of("UTC"));
                ZonedDateTime time_before_first = first_time.minusHours(5);
                final double FIRST_VALUE= 1.0;
                final double SECOND_VALUE= 2.0;        
                ts.addRow( first_time, FIRST_VALUE);
                ts.addRow( time_before_first, SECOND_VALUE );
            });
        assertTrue(exception.getMessage().contains("inserting data before"));

    }


    @ParameterizedTest
    @MethodSource("class_and_resource_list")
    public void test_all_forms_of_data( Class ts_class_type, String resource ) throws Exception {
        String fileName= TestingPaths.instance.getTempDir()+"/"+"timeseriestest.db";

        File file = new File(fileName);
        file.delete();
        try (TimeSeriesDatabase db = new JdbcTimeSeriesDatabase(fileName,
                JdbcTimeSeriesDatabase.CREATION_MODE.CREATE_NEW);                
            ) {            

            TimeSeries ts = fixtures.load_regular_time_series_data(resource, ts_class_type);
            
            assertNotNull(ts);                        
            assertEquals(1,ts.valueAt(0));

            ZonedDateTime firstTime = ts.firstTime();
            double firstValue = ts.valueAt(firstTime);
            int middle = (int)ts.numberValues()/2;
            ZonedDateTime middleTime = ts.timeAt(middle);
            double middleValue = ts.valueAt(middle);
            ZonedDateTime lastTime = ts.lastTime();
            double lastValue = ts.valueAt(lastTime);

            db.write(ts);

            List<Identifier> catalog = db.getTimeSeriesIDs2();
            assertNotNull( catalog );
            assertTrue( catalog.contains( ts.identifier() ) );

            TimeSeries from_db = db.getTimeSeries(ts.identifier(),
                                                  ts.firstTime(),
                                                  ts.lastTime() );
            assertEquals( ts.numberValues(), from_db.numberValues() );
            assertNotNull( from_db );
            assertEquals( firstValue, from_db.valueAt(0) );
            assertEquals( middleValue, from_db.valueAt(middle));
            assertEquals( lastValue, from_db.valueAt(lastTime));
            
            assertTrue( firstTime.isEqual(from_db.firstTime()));
            assertTrue( middleTime.isEqual(from_db.timeAt(middle)));
            assertTrue( lastTime.isEqual( from_db.lastTime()));

        }
    }


}