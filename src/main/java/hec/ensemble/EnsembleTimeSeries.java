package hec.ensemble;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import hec.*;

/**
 *  EnsembleTimeSeries is a collection of Ensembles over time
 *
 */
public class EnsembleTimeSeries
  {

    private TimeSeriesIdentifier timeseriesID;
    private String units;
    private String dataType;
    private String version;
    //https://docs.oracle.com/javase/7/docs/api/java/util/TreeMap.html
    private TreeMap<ZonedDateTime,Ensemble> items;

    public int getCount()
    {
      return items.size();
    }

    public EnsembleTimeSeries(TimeSeriesIdentifier timeseriesID, String units, String dataType, String version)
    {
      init(timeseriesID,units,dataType,version);
    }

    private void init(TimeSeriesIdentifier timeseriesID, String units, String dataType, String version) {
      this.timeseriesID = timeseriesID;
      this.units = units;
      this.dataType = dataType;
      this.version = version;
      items = new TreeMap<>();
    }

    public void addEnsemble(ZonedDateTime issueDate, float[][] ensemble, ZonedDateTime startDate, Duration interval)
    {
      Ensemble e = new Ensemble(issueDate,ensemble,startDate, interval);
      addEnsemble(e);
    }

    public void addEnsemble(Ensemble ensemble) {
      ensemble.parent = this;
      items.put(ensemble.getIssueDate(),ensemble);
    }

    public List<ZonedDateTime> getIssueDates() {
      // convert from map to a List<>
      List<ZonedDateTime> rval = new ArrayList<>(items.size());
      rval.addAll(items.keySet());
      return rval;
    }

    public Ensemble getEnsemble(ZonedDateTime t) {
      return items.get(t);
    }

    public String getUnits() {
      return units;
    }

    public String getDataType() {
      return dataType;
    }

    public String getVersion() {
      return version;
    }

    public TimeSeriesIdentifier getTimeSeriesIdentifier() {
      return timeseriesID;
    }

  }
