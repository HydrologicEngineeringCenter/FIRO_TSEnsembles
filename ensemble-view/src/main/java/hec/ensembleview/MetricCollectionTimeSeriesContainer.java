package hec.ensembleview;

import hec.ensemble.stats.Statistics;
import hec.metrics.MetricCollectionTimeSeries;
import hec.metrics.MetricTypes;

public class MetricCollectionTimeSeriesContainer {
    private final MetricCollectionTimeSeries metricCollectionTimeSeries;
    private final MetricTypes metricTypes;
    private final Statistics statistics;

    public MetricCollectionTimeSeriesContainer(MetricCollectionTimeSeries metricCollectionTimeSeries, MetricTypes metricTypes, Statistics statistics) {
        this.metricCollectionTimeSeries = metricCollectionTimeSeries;
        this.metricTypes = metricTypes;
        this.statistics = statistics;
    }

    public MetricCollectionTimeSeries getMetricCollectionTimeSeries() {
        return metricCollectionTimeSeries;
    }

    public MetricTypes getMetricTypes() {
        return metricTypes;
    }

    public Statistics getStatistics() {
        return statistics;
    }
}
