package hec.ensemble;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;


/**
 *  EnsembleTimeSeries is a collection of Ensembles over time
 *
 *  backing can be database or in-memory (from csv)
 *
 */
public class EnsembleTimeSeries
  {

    private EnsembleTimeSeriesDatabase _db = null;

    private TimeSeriesIdentifier timeseriesID;
    private String units;
    private String dataType;
    private String version;

    public int getCount()
    {
      if( _db == null)
      return items.size();
      else
        return _db.getCount(timeseriesID);

    }

    private ArrayList<Ensemble> items;
    private ArrayList<ZonedDateTime> issueDates; // TO DO .need something sorted..


    /**
     * EnsembleTimeSeries constructor for use with a EnsembleDatabase
     * @param db
     * @param timeseriesID
     * @param units
     * @param dataType
     * @param version
     */
    protected EnsembleTimeSeries(EnsembleTimeSeriesDatabase db, TimeSeriesIdentifier timeseriesID, String units, String dataType, String version)
    {
      this._db = db;
      this.timeseriesID = timeseriesID;
      this.units = units;
      this.dataType = dataType;
      this.version = version;
      issueDates = new ArrayList<>();
      reLoadIssueDates();
    }

    public EnsembleTimeSeries(TimeSeriesIdentifier timeseriesID, String units, String dataType, String version)
    {
      this.timeseriesID = timeseriesID;
      this.units = units;
      this.dataType = dataType;
      this.version = version;
      items = new ArrayList<>();
      reLoadIssueDates();

    }

    public void addEnsemble(ZonedDateTime issueDate, float[][] ensemble, ZonedDateTime startDate, Duration interval)
    {
      Ensemble e = new Ensemble(issueDate,ensemble,startDate, interval);
      addEnsemble(e);

    }

    private void reLoadIssueDates() {
      issueDates = new ArrayList<>();
      if(_db != null)
      { // get issue dates from database/
        _db.getEnsembleTimeSeries(this.timeseriesID);
      }
      else
      {
        for (Ensemble e : items) {
          issueDates.add(e.getIssueDate());
        }
      }
    }

    public void addEnsemble(Ensemble ensemble) {
      ensemble.parent = this;
      items.add(ensemble);

      reLoadIssueDates();// SLOW>>>>  need sorted issueDate list
      // TO DO.. what if database backend?
    }


    public ZonedDateTime[] getIssueDates() {
    return (ZonedDateTime[])issueDates.toArray();
    }

    /**
     * gets nearest ensemble at or before time t
     * @param t
     * @param tolerance
     * @return
     */
    public Ensemble getEnsemble(ZonedDateTime t, int tolerance) {
    return null;
    }

    public Ensemble getEnsemble(ZonedDateTime t) {

      if( _db != null)
      {
        return null;
      //_db.getEnsemble()
      }
    return null;
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
    public boolean issueDateExists(ZonedDateTime issueDate, int hoursTolerance) {

      if( hoursTolerance == 0)
         return issueDates.contains(issueDate);
      else
      return false; // TODO... not implemented.
    }
  }
