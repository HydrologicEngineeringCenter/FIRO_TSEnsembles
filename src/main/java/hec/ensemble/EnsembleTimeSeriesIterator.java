package hec.ensemble;

import hec.TimeSeriesDatabase;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;

/**
 * EnsembleTimeSeriesIterator is used to iterate over ensembles in a TimeSeriesDatabase
 * EnsembleTimeSeriesReader is used to create an EnsembleTimeSeriesIterator
 * */
public class EnsembleTimeSeriesIterator implements Iterator<Ensemble> {

    TimeSeriesDatabase _db;
    TimeSeriesIdentifier tsid;
    List<ZonedDateTime> issueDates;
    Iterator<ZonedDateTime> _iterator;
    public EnsembleTimeSeriesIterator(TimeSeriesDatabase db, TimeSeriesIdentifier tsid)
    {
    _db = db;
    this.tsid = tsid;
    issueDates = _db.getEnsembleIssueDates(tsid);
    _iterator = issueDates.iterator();
    }

    @Override
    public boolean hasNext() {
        return _iterator.hasNext();
    }

    @Override
    public Ensemble next() {
       ZonedDateTime t = _iterator.next();
        return _db.getEnsemble(tsid,t);
    }
}
