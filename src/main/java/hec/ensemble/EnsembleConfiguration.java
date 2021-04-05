package hec.ensemble;
import java.time.Duration;
import java.time.ZonedDateTime;


public class EnsembleConfiguration implements Configuration{
    private final Duration _interval;
    private final ZonedDateTime _issueDate;
    private final ZonedDateTime _startDateTime;

    public EnsembleConfiguration(ZonedDateTime issueDate, ZonedDateTime startDate, Duration interval)
    {
        this._issueDate = issueDate;
        this._startDateTime = startDate;
        this._interval = interval;
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
}