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
 *  storage of data can be EnsembleTimeSeriesDatabase
 *  or in-memory (from csv files for example)
 *
 */
public class EnsembleTimeSeries
  {

    private TimeSeriesDatabase _db = null;

    private TimeSeriesIdentifier timeseriesID;
    private String units;
    private String dataType;
    private String version;
    //https://docs.oracle.com/javase/7/docs/api/java/util/TreeMap.html
    private TreeMap<ZonedDateTime,Ensemble> items;

    public int getCount()
    {
      if( _db == null)
      return items.size();
      else
        return _db.getCount(timeseriesID);

    }



    /**
     * EnsembleTimeSeries constructor for use with a EnsembleDatabase
     * @param db
     * @param timeseriesID
     * @param units
     * @param dataType
     * @param version
     */
    public EnsembleTimeSeries(TimeSeriesDatabase db, TimeSeriesIdentifier timeseriesID, String units, String dataType, String version)
    {
      this._db = db;
      init(timeseriesID, units, dataType, version);
      List<ZonedDateTime> times = db.getIssueDates(timeseriesID);
      for (ZonedDateTime t: times ) {
         items.put(t,null); // keep all Timestamps in memory, ensembles come from disk.
      }
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

    /**
     * gets nearest ensemble at or before time t
     *
     *   t1 --- t2 --- t3  (issue dates in TreeMap)
     *   t1 --- t2         (issue dates from floorKey )
     *             t       (requested date)
     *
     *  In the example above t2 will be returned
     *  if  t2 + toleranceHours >= t
     *
     *
     * @param t   request ensemble at date
     * @param toleranceHours allow toleranceHours back to match an issue date
     * @return
     */
    public Ensemble getEnsemble(ZonedDateTime t, int toleranceHours) {

      ZonedDateTime t2 = getNearestIssueDate(t,toleranceHours);
      if( t2 == null)
        return null;

      if (_db != null){
        return _db.getEnsemble(timeseriesID,t2); // from disk
      }
         

       return items.get(t2);
    }

    private ZonedDateTime getNearestIssueDate(ZonedDateTime t, int toleranceHours)
    {
      ZonedDateTime t2 =  items.floorKey(t); //https://docs.oracle.com/javase/7/docs/api/java/util/TreeMap.html#floorEntry(K)
      if( t2 != null && t2.plusHours(toleranceHours).compareTo(t) >=0)
        return t2;
      return null;
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

    /**
     *
     * @param issueDate
     * @param hoursTolerance hours before t to check
     * @return true if an ensemble exists at or before time t
     */
    protected boolean issueDateExists(ZonedDateTime issueDate, int hoursTolerance) {
      return getNearestIssueDate(issueDate,hoursTolerance)!= null;
     }
  }
