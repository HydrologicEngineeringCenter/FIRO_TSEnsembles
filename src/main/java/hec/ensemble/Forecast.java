package hec.ensemble;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Forecast contains an Ensemble with some associated information
 *
 */

public class Forecast
  {
    private final Duration interval;

    public Forecast(Location location, ZonedDateTime issueDate, float[][] ensemble, ZonedDateTime startDate, Duration interval)
    {
      this.Location = location;
      this.IssueDate = issueDate;
      this.Ensemble = ensemble;
      this.startDateTime = startDate;
      this.interval = interval;
    }

    /// <summary>
    /// Location of this forecast
    /// </summary>
    public Location Location;

    public ZonedDateTime IssueDate;

    public ZonedDateTime startDateTime;

    public int getTimeCount(){
      return Ensemble[0].length;
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
    public float[][] Ensemble;


  }
