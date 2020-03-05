package hec.ensemble;

import java.time.ZonedDateTime;
import java.util.Iterator;

/**
 * EnsembleTimeSeriesIterator is used to iterate over ensembles in a EnsembleTimeSeries
 * */
public class EnsembleTimeSeriesIterator implements Iterator<Ensemble> {

    private Iterator<ZonedDateTime> _iterator;
    private EnsembleTimeSeries ets;

    public EnsembleTimeSeriesIterator(EnsembleTimeSeries ets)
    {
      this.ets = ets;
      _iterator = ets.getIssueDates().iterator();
    }

    @Override
    public boolean hasNext() {
        return _iterator.hasNext();
    }

    @Override
    public Ensemble next() {
       ZonedDateTime t = _iterator.next();
        return ets.getEnsemble(t);
    }
}
