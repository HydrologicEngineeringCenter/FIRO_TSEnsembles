package hec.ensemble;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Ensemble is an array of time-series data
 *
 * each time-series in the Ensemble has the same timestamps
 *
 *
 */

public class Ensemble
  {
    private final Duration interval;
    EnsembleTimeSeries parent = null;

    public Ensemble(ZonedDateTime issueDate, float[][] values, ZonedDateTime startDate, Duration interval)
    {
      this.IssueDate = issueDate;
      this.values = values;
      this.startDateTime = startDate;
      this.interval = interval;
    }


    private ZonedDateTime IssueDate;

    public ZonedDateTime startDateTime;

    public int getTimeCount(){
      return values[0].length;
  }

    public ZonedDateTime[] getTimeStamps() {

      ZonedDateTime[] rval = new ZonedDateTime[getTimeCount()];
      ZonedDateTime t = startDateTime;
      int size= getTimeCount();
      for (int i = 0; i <size ; i++) {
        rval[i] =t;
        t.plus(interval);
      }
      return rval;
    }

    /**
     * ensemble data
     * row represents ensemble members
     * columns are time steps
     */
    public float[][] values;


    public ZonedDateTime getIssueDate() {
      return IssueDate;
    }

    public Duration getInterval()
    {
      return this.interval;
    }
  }
