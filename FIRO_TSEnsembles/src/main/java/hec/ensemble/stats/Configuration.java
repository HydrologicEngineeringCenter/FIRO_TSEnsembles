package hec.ensemble.stats;
import java.time.Duration;
import java.time.ZonedDateTime;

public interface Configuration{
    /**
     * getDuration Represents the time interval duration
     */
    public Duration getDuration();
    public ZonedDateTime getIssueDate();
    public ZonedDateTime getStartDate();
    public String getUnits();
}