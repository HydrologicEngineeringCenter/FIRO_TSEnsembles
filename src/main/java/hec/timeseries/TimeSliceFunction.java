package hec.timeseries;

import java.time.ZonedDateTime;

/**
 * A function that will peform some computation 
 * on the provided value
 * 
 * @param time The current time for this row
 * @param value The for this row
 * @return double the result of this function
 */
public interface TimeSliceFunction{
    public double apply( ZonedDateTime time, double value ) throws Exception;    
}