package hec.timeseries;

import java.time.ZonedDateTime;
import java.util.List;

import hec.exceptions.*;
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
     
     * @param time time for this value (will be converted to GMT for storage)
     * @param value the value (Double.NEGATIVE_INFINITY is used as a missing value)
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
     * @param time the value at a given time
     * @return value at the associated time
     * 
     */
    public double valueAt(ZonedDateTime time ) throws NoValue;
    public double valueAt( int index ) throws NoValue;

    /**
     * This function MAY return a valid time even if the timeseries
     * object does not contain a value. Such as a regular interval TS were 
     * it can be calculated.
     * 
     * @param index Number over intervals from the start time
     * @return ZonedDateTime object containing the appropriate time
     */
    public ZonedDateTime timeAt( int index ) throws NoValue;
    /**
     * 
     * @param row_function function applied to this row
     * @param newTs Identifier for a new timeseries that will be created
     * @return a new TimeSeries built from the output of the row function
     * @throws Exception if the user supplied function doesn't handle errors this library will bail
     */
    public TimeSeries applyFunction( TimeSliceFunction row_function, TimeSeriesIdentifier newTs ) throws Exception;

    /**
     * Applies a function over the whole timewindow without building a new timeseries
     * @param row_function function to call
     * @throws Exception if the user supplied function doesn't handle errors this library will bail
     */
    public void applyFunction( TimeSliceFunction row_function ) throws Exception;
    /**
     * 
     * @param row_function function to call
     * @param window object that controls moving through the timeseries
     * @return a new TimeSeries built from the outputs of row_function.end();
     * @throws Exception if the user supplied function doesn't handle errors this library will bail
     */
    public TimeSeries applyFunction( WindowFunction row_function, AggregateWindow window )throws Exception;   
    
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
     * @return Unique Identifier for the type of time series
     */
    public String subtype();
}