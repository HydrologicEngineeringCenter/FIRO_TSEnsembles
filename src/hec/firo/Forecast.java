package hec.firo;

import java.util.Date;

/**
 * Forecast contains an Ensemble with some associated information
 *
 */

public class Forecast
  {
    public Forecast(Location location, Date issueDate, float[][] ensemble, Date[] timeStamps)
    {
      this.Location = location;
      this.IssueDate = issueDate;
      this.Ensemble = ensemble;
      this.TimeStamps = timeStamps;
    }

    /// <summary>
    /// Location of this forecast
    /// </summary>
    public Location Location;

    public Date IssueDate;

    public Date[] TimeStamps;

    public float[][] Ensemble;


  }
