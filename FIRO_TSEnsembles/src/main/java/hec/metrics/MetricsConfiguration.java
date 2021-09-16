package hec.metrics;

import hec.stats.Configuration;

import java.time.Duration;
import java.time.ZonedDateTime;

public class MetricsConfiguration  implements Configuration {
    private final Duration _interval;
    private final ZonedDateTime _issueDate;
    private final ZonedDateTime _startDateTime;
    private final String _units;

    private MetricsConfiguration(ZonedDateTime issueDate, ZonedDateTime startDate, Duration interval, String units)
    {
        this._issueDate = issueDate;
        this._startDateTime = startDate;
        this._interval = interval;//this doesnt make sense
        this._units = units;//this doesnt make sense
    }
    public MetricsConfiguration(ZonedDateTime issueDate, ZonedDateTime startDate)
    {
        this(issueDate,startDate,null,"");
    }
    public Duration getDuration(){
        return _interval;
    }
    public ZonedDateTime getIssueDate(){
        return _issueDate;
    }
    public ZonedDateTime getStartDate(){
        return _startDateTime;
    }
    public String getUnits(){return  _units;}
}
