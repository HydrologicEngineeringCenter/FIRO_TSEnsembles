package hec.ensemble;

import hec.TimeSeriesDatabase;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 *  EnsembleTimeSeriesReader reads ensembles from a @TimeSeriesDatabase.
 *  Supports iterating through time at a location defined by @TimeSeriesIdentifier
 *
 */
public class EnsembleTimeSeriesReader
  {

    private TimeSeriesDatabase _db = null;
    private TimeSeriesIdentifier timeseriesID;
    private List<ZonedDateTime> items;

    /**
     * EnsembleTimeSeries constructor
     * @param db abstract TimeSeriesDatabase that stores the ensembles
     * @param timeseriesID TimeSeriesIdentifier
     */
    public EnsembleTimeSeriesReader(TimeSeriesDatabase db, TimeSeriesIdentifier timeseriesID)
    {
      this._db = db;
      this.timeseriesID = timeseriesID;
      items = _db.getEnsembleIssueDates(timeseriesID);
    }

    public List<ZonedDateTime> getIssueDates() {
      return items;
    }

    public Ensemble getEnsemble(ZonedDateTime t) {
      Ensemble rval = _db.getEnsemble(timeseriesID,t); // from disk
      if( rval == null)
         Logger.logError("Error: could not find ensemble at "+t.toString());
      return rval;
    }

    public TimeSeriesIdentifier getTimeSeriesIdentifier() {
      return timeseriesID;
    }

  }
