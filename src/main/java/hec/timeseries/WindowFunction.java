package hec.timeseries;

import java.time.ZonedDateTime;

/**
 * A set of function that allow organizing computations 
 * over a static or moving window
 */
public interface WindowFunction{
    public void start(ZonedDateTime start_of_window_time);
    public void apply_slice(ZonedDateTime time, double value);
    public double end( ZonedDateTime end_of_window_time);
}