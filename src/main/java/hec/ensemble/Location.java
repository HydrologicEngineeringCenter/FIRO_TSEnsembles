package hec.ensemble;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class Location
  {
    public Location(String name, Watershed watershed)
    {
      this.Name = name;
      Forecasts = new ArrayList<>();
      this.Watershed = watershed;
    }
    public String Name;

    /// <summary>
    /// Parent Watershed
    /// </summary>
    public Watershed Watershed;
    
    /// <summary>
    /// List of forecasts 
    /// </summary>
    public ArrayList<Forecast> Forecasts;

     Forecast AddForecast(ZonedDateTime issueDate, float[][] ensemble, ZonedDateTime  startDate, Duration interval)
    {
      Forecast f = new Forecast(this, issueDate,ensemble,startDate,interval);
      Forecasts.add(f);
      return f;
    }
  }
