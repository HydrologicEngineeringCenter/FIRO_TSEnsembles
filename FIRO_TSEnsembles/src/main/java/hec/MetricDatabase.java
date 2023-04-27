package hec;


import hec.metrics.MetricCollectionTimeSeries;
import hec.metrics.MetricCollection;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public interface MetricDatabase extends AutoCloseable {

    MetricCollection getMetricCollection(RecordIdentifier timeseriesID, ZonedDateTime issue_time);
    Map<RecordIdentifier,List<String>> getMetricStatistics();
    List<String> getMetricStatistics(RecordIdentifier timeseriesID);
    MetricCollectionTimeSeries getMetricCollectionTimeSeries(RecordIdentifier timeseriesID, String statistics);
    List<MetricCollectionTimeSeries> getMetricCollectionTimeSeries(RecordIdentifier timeseriesID);
    List<ZonedDateTime> getMetricCollectionIssueDates(RecordIdentifier timeseriesID);
    void write(MetricCollectionTimeSeries[] metricsArray) throws Exception;
    void write(MetricCollectionTimeSeries metrics) throws Exception;
    void write(MetricCollection metrics) throws Exception;
    List<RecordIdentifier> getMetricTimeSeriesIDs();
    List<RecordIdentifier> getMectricPairedDataIDs();

}
