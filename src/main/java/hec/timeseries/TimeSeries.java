package hec.timeseries;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import hec.exceptions.*;
/**
 * A function that will peform some computation 
 * on the provided value
 * 
 * @param time The current time for this row
 * @param value The for this row
 * @return double the result of this function
 */
interface TimeSliceFunction{
    public double apply( ZonedDateTime time, double value );    
}

/**
 * A set of function that allow organizing computations 
 * over a static or moving window
 */
interface WindowFunction{
    public void start(ZonedDateTime start_of_window_time);
    public void apply_slice(ZonedDateTime time, double value);
    public double end( ZonedDateTime end_of_window_time);
}

/**
 * 
 */
interface AggregateWindow{
    public boolean isStart(ZonedDateTime time);
    public boolean isEnd(ZonedDateTime time);
    public boolean running();
    public Duration interval();
}


/**
 * Object to access data in a time series
 * At this level the distriction between regular and irregular is ignored.
 * 
 * This class is mutable
 * @author Michael Neilson &lt;michael.a.neilson@usace.army.mil&gt;
 * @version 20200409
 * 
 */
public interface TimeSeries {
    /**
     * Generic Interface into all time series
     
     * @param time
     * @param value
     * @return The Time Series itself
     */
    public TimeSeries addRow( ZonedDateTime time, double value);

    /**
     * 
     * @return the number of values contained in this time series object
     */
    public int numberValues();

    /**
     * 
     * @param time
     * @return value at the associated time
     */
    public double valueAt(ZonedDateTime time ) throws NoValue;
    public double valueAt( int index ) throws NoValue;
    public ZonedDateTime timeAt( int index ) throws NoValue;
    /**
     * 
     * @param row_function function applied to this row
     * @return a new TimeSeries built from the output of the row function
     */
    public TimeSeries applyFunction( TimeSliceFunction row_function );
    /**
     * 
     * @param row_function
     * @param window
     * @return a new TimeSeries built from the outputs of row_function.end();
     */
    public TimeSeries applyFunction( WindowFunction row_function, AggregateWindow window );   
    
    /**
     * 
     * @return ZoneDateTime object representing the first value in the retrieved time window
     */
    public ZonedDateTime firstTime();

    /**
     * @return ZonedDateDate object representing the last value in the retrieved time window
     */
    public ZonedDateTime lastTime();

    /**
     * 
     * @return TimeSeriesIdentifier object that can uniquely identify this TimeSeries in the catalog
     */
    public TimeSeriesIdentifier identifier();

    /**
     * 
     * @return A list of the particular type of column this timeseries requires
     */
    public List<String> columns();
}