package hec.timeseries;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * 
 */
public interface AggregateWindow{
    public boolean isStart(ZonedDateTime time);
    public boolean isEnd(ZonedDateTime time);
    public boolean running();
    public Duration interval();
}