package hec.ensemble;


import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;


public class EnsembleTimeSeries
  {
    private final String watershedName; // name of basin/watershed
    private final String source; // where did this data come from i.e. 'rfc'
    private String locationName;  // name of location

    public ArrayList<Ensemble> ensembleList;

    public EnsembleTimeSeries(String locationName, String watershedName, String source)
    {
      this.locationName = locationName;
      this.watershedName = watershedName;
      this.source = source;
      ensembleList = new ArrayList<>(10);
    }

    ZonedDateTime getStartDateTime()
    {
      return ensembleList.get(0).startDateTime;
    }
    ZonedDateTime getEndDateTime()
    {
      return ensembleList.get(ensembleList.size()-1).startDateTime;
    }

    public void addEnsemble(ZonedDateTime issueDate, float[][] ensemble, ZonedDateTime startDate, Duration interval)
    {
      Ensemble e = new Ensemble(issueDate,ensemble,startDate, interval);
      ensembleList.add(e);
    }

    public void addEnsemble(Ensemble ensemble) {

    ensembleList.add(ensemble);
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
