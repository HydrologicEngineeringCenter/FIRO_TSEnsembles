package hec;


import hec.metrics.MetricCollectionTimeSeries;
import hec.metrics.MetricCollection;

import java.time.ZonedDateTime;
import java.util.List;

public interface MetricDatabase extends AutoCloseable {

    MetricCollection getMetricCollection(RecordIdentifier timeseriesID, ZonedDateTime issue_time);
    MetricCollectionTimeSeries getMetricCollectionTimeSeries(RecordIdentifier timeseriesID);
    List<ZonedDateTime> getMetricCollectionIssueDates(RecordIdentifier timeseriesID);
    void write(MetricCollectionTimeSeries[] metricsArray) throws Exception;
    void write(MetricCollectionTimeSeries metrics) throws Exception;
    List<RecordIdentifier> getMetricTimeSeriesIDs();


}
