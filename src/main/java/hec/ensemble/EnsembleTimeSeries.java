package hec.ensemble;


import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;


public class EnsembleTimeSeries
  {
    private final String watershedName; // name of basin/watershed
    private final String source; // where did this data come from i.e. 'rfc'
    private String locationName;  // name of location

    public int size()
    {
      return items.size();
    }
    public ArrayList<Ensemble> items;

    public EnsembleTimeSeries(String locationName, String watershedName, String source)
    {
      this.locationName = locationName;
      this.watershedName = watershedName;
      this.source = source;
      items = new ArrayList<>(10);
    }

    ZonedDateTime getStartDateTime()
    {
      return items.get(0).startDateTime;
    }
    ZonedDateTime getEndDateTime()
    {
      return items.get(items.size()-1).startDateTime;
    }

    public void addEnsemble(ZonedDateTime issueDate, float[][] ensemble, ZonedDateTime startDate, Duration interval)
    {
      Ensemble e = new Ensemble(issueDate,ensemble,startDate, interval);
      items.add(e);
    }

    public void addEnsemble(Ensemble ensemble) {
      ensemble.parent = this;
    items.add(ensemble);
    }

    public String getWatershedName() {
      return watershedName;
    }

    public String getSource() {
      return source;
    }

    public String getLocationName() {
      return locationName;
    }
  }
