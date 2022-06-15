package hec.stats;
import java.time.Duration;
import java.time.ZonedDateTime;

public interface Configuration{
    public Duration getDuration();
    public ZonedDateTime getIssueDate();
    public ZonedDateTime getStartDate();
    public String getUnits();
}