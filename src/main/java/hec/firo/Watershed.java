package hec.firo;


import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;


public class Watershed
  {

    public Watershed(String name)
    {
      this.Name = name;
      Locations = new ArrayList<>(10);
    }
    public String Name;

    public ArrayList<Location> Locations;

    public Forecast AddForecast(String locName, ZonedDateTime issueDate, float[][] ensemble, ZonedDateTime startDate, Duration interval)
    {
      Location loc = GetOrCreateLocation(locName);

        Forecast rval = loc.AddForecast(issueDate, ensemble, startDate, interval);
      return rval;
    }

    private Location GetOrCreateLocation(String locName)
    {
      Location loc =  Locations.stream().filter( x -> x.Name.equals(locName)).findAny().orElse(null);

      if (loc == null) {
          loc = new Location(locName, this);
        Locations.add(loc);
      }


      return loc;
    }

  }
