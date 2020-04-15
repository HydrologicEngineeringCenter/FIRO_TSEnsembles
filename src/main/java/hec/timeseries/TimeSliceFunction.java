package hec.timeseries;

import java.time.ZonedDateTime;

/**
 * @author Michael Neilson
 * @since 2020-04-09
 */
public interface TimeSliceFunction{
    /**
 * A function that will peform some computation 
 * on the provided value
 * 
 * @param time The current time for this row
 * @param value The for this row
 * @return double the result of this function
 * @throws Exception if the user supplied function doesn't handle errors this library will bail
 */
    public double apply( ZonedDateTime time, double value ) throws Exception;    
}