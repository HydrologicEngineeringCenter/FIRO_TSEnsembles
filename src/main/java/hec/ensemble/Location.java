package hec.ensemble;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class Location
  {
    public Location(String name, EnsembleTimeSeries watershed)
    {
      this.Name = name;
      Forecasts = new ArrayList<>();
      this.Watershed = watershed;
    }
    public String Name;

    /// <summary>
    /// Parent EnsembleTimeSeries
    /// </summary>
    public EnsembleTimeSeries Watershed;
    
    /// <summary>
    /// List of forecasts 
    /// </summary>
    public ArrayList<Ensemble> Forecasts;

     Ensemble AddForecast(ZonedDateTime issueDate, float[][] ensemble, ZonedDateTime  startDate, Duration interval)
    {
      Ensemble f = new Ensemble(this, issueDate,ensemble,startDate,interval);
      Forecasts.add(f);
      return f;
    }
  }
