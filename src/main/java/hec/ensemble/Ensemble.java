package hec.ensemble;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Ensemble is an array of time-series data
 *
 * each time-series in the Ensemble has the same timestamps
 *
 */
public class Ensemble
  {
    private final Duration interval;
    protected EnsembleTimeSeries parent = null;


    public Ensemble(ZonedDateTime issueDate, float[][] values, ZonedDateTime startDate, Duration interval)
    {
      this.issueDate = issueDate;
      this.values = values;
      this.startDateTime = startDate;
      this.interval = interval;
    }


    private ZonedDateTime issueDate;

    private ZonedDateTime startDateTime;

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

    private float[][] values;


    public ZonedDateTime getIssueDate() {
      return issueDate;
    }

    public Duration getInterval()
    {
      return this.interval;
    }

    public ZonedDateTime getStartDateTime() {
      return startDateTime;
    }

    /**
     * ensemble data
     * row represents ensemble members
     * columns are time steps
     * @return tow-dimensional zize float[][] (all rows are the same size)
     */
    public float[][] getValues() {
      return values;
    }

  }
