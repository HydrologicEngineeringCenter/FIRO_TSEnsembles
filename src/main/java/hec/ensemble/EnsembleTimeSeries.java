package hec.ensemble;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 *  EnsembleTimeSeries is a collection of Ensembles over time
 *
 */
public class EnsembleTimeSeries implements  Iterable<Ensemble>,IEnsembleTimeSeries
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

    /**
     * addEnsemble adds a new Ensemble to this in-memory collection
     * @param issueDate issueDate of Ensemble
     * @param ensemble  data values
     * @param startDate startingDate for this ensemble.
     * @param interval time-step (Duration) between values
     */
    public void addEnsemble(ZonedDateTime issueDate, float[][] ensemble, ZonedDateTime startDate, Duration interval)
    {
      Ensemble e = new Ensemble(issueDate,ensemble,startDate, interval);
      addEnsemble(e);
    }
    /**
     * addEnsemble adds a new Ensemble to this in-memory collection
     * @param ensemble ensemble to add
     */
    public void addEnsemble(Ensemble ensemble) {
      ensemble.parent = this;
      items.put(ensemble.getIssueDate(),ensemble);
    }

    /**
     * getIssueDates computes a list of ZonedDateTimes in this collection
     * @return returns an array of ZonedDateTime
     */
    public List<ZonedDateTime> getIssueDates() {
      // convert from map to a List<>
      List<ZonedDateTime> rval = new ArrayList<>(items.size());
      rval.addAll(items.keySet());
      return rval;
    }

    /**
     * Returns an specified ensemble from this im-memory collection
     * @param issueDate date used to lookup ensemble to reutrn.
     * @return an Ensemble
     */
    public Ensemble getEnsemble(ZonedDateTime issueDate) {
      return items.get(issueDate);
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

    @Override
    public Iterator<Ensemble> iterator() {
      return new EnsembleTimeSeriesIterator(this);
    }
  }
