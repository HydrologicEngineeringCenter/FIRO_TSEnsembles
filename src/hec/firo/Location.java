package hec.firo;

import java.util.ArrayList;
import java.util.Date;

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

     Forecast AddForecast(Date issueDate, float[][] ensemble, Date[] timeStamps)
    {
      Forecast f = new Forecast(this, issueDate,ensemble,timeStamps);
      Forecasts.add(f);
      return f;
    }
  }
