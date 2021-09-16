package hec;


import hec.metrics.MetricCollectionTimeSeries;
import hec.metrics.MetricCollection;
import hec.ensemble.TimeSeriesIdentifier;

import java.time.ZonedDateTime;
import java.util.List;

public interface MetricDatabase extends AutoCloseable {

    MetricCollection getMetricCollection(TimeSeriesIdentifier timeseriesID, ZonedDateTime issue_time);
    MetricCollectionTimeSeries getMetricCollectionTimeSeries(TimeSeriesIdentifier timeseriesID);
    List<ZonedDateTime> getMetricCollectionIssueDates(TimeSeriesIdentifier timeseriesID);
    void write(MetricCollectionTimeSeries[] metricsArray) throws Exception;
    void write(MetricCollectionTimeSeries metrics) throws Exception;
    List<TimeSeriesIdentifier> getMetricTimeSeriesIDs();


}
