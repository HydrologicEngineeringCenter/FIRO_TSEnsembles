package hec.ensemble;
import java.time.Duration;
import java.time.ZonedDateTime;
import hec.ensemble.stats.Configuration;


public class EnsembleConfiguration implements Configuration{
    private final Duration _interval;
    private final ZonedDateTime _issueDate;
    private final ZonedDateTime _startDateTime;
    private final String _units;

    public EnsembleConfiguration(ZonedDateTime issueDate, ZonedDateTime startDate, Duration interval, String units)
    {
        this._issueDate = issueDate;
        this._startDateTime = startDate;
        this._interval = interval;
        this._units = units;
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