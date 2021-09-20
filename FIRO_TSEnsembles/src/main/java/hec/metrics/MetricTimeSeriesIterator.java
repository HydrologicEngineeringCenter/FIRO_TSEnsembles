package hec.metrics;

import hec.metrics.MetricCollection;
import hec.metrics.MetricCollectionTimeSeries;

import java.time.ZonedDateTime;
import java.util.Iterator;

public class MetricTimeSeriesIterator implements Iterator<MetricCollection> {
    private Iterator<ZonedDateTime> _iterator;
    private MetricCollectionTimeSeries _mcts;
    public MetricTimeSeriesIterator(MetricCollectionTimeSeries metricCollections) {
        _mcts =metricCollections;
        _iterator = _mcts.getIssueDates().iterator();
    }

    @Override
    public boolean hasNext() {
        return _iterator.hasNext();
    }

    @Override
    public MetricCollection next() {
        return _mcts.getMetricCollection(_iterator.next());
    }
}
