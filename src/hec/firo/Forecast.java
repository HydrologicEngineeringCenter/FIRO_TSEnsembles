package hec.firo;

import java.time.LocalDateTime;

/**
 * Forecast contains an Ensemble with some associated information
 *
 */

public class Forecast
  {
    public Forecast(Location location, LocalDateTime issueDate, float[][] ensemble, LocalDateTime[] timeStamps)
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

    public LocalDateTime IssueDate;

    public LocalDateTime[] TimeStamps;

    public float[][] Ensemble;


  }
