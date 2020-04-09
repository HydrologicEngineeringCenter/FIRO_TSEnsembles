package hec.timeseries;

import java.time.Duration;
import java.time.ZonedDateTime;
import hec.Identifier;
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
    public double valueAtTime(ZonedDateTime time );
    public double valueAt( int index );
    public double timeAt( int index );
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
    public ZonedDateTime firstTime();
    public ZonedDateTime lastTime();
    public TimeSeriesIdentifier identifier();
}