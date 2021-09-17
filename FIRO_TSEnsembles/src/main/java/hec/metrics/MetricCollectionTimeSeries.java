package hec.metrics;

import hec.MetricDatabase;
import hec.RecordIdentifier;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class MetricCollectionTimeSeries implements  Iterable<MetricCollection>, Metrics
{

    private RecordIdentifier timeseriesID;
    private String units;
    private MetricTypes metricType;
    private TreeMap<ZonedDateTime,MetricCollection> items;



    public MetricCollectionTimeSeries(RecordIdentifier timeseriesID, String units, String metricType)
    {
        init(timeseriesID,units,metricType);
    }

    private void init(RecordIdentifier timeseriesID, String units, String dataType) {
        this.timeseriesID = timeseriesID;
        this.units = units;
        this.metricType = MetricTypes.valueOf(dataType);
        items = new TreeMap<>();
    }

    /**
     * addMetricCollection adds a new MetricCollection to this in-memory collection
     * @param issueDate issueDate of MetricCollection
     * @param metrics  data values
     * @param startDate startingDate for this MetricCollection.
     * @param parameterNames  the names
     */
    public void addMetricCollection(ZonedDateTime issueDate, float[][] metrics, ZonedDateTime startDate, String[] parameterNames)
    {
        MetricCollection e = new MetricCollection(issueDate,startDate, metrics, parameterNames);
        addMetricCollection(e);
    }
    /**
     * addMetricCollection adds the MetricCollection to this in-memory collection
     * @param metricCollection metricCollection to add
     */
    public void addMetricCollection(MetricCollection metricCollection) {
        metricCollection.parent = this;
        items.put(metricCollection.getIssueDate(),metricCollection);
    }

    /**
     * getIssueDates computes a list of ZonedDateTimes in this collection
     * @return returns an array of ZonedDateTime
     */
    public List<ZonedDateTime> getIssueDates() {
        // convert from map to a List<>
        List<ZonedDateTime> rval = new ArrayList<>(items.size());
        rval.addAll(items.keySet());
        return rval;
    }

    /**
     * Returns an specified metric collection from this im-memory collection
     * @param issueDate date used to lookup metricColelction to return.
     * @return a MetricCollection
     */
    public MetricCollection getMetricCollection(ZonedDateTime issueDate) {
        return items.get(issueDate);
    }

    public String getUnits() {
        return units;
    }

    public MetricTypes type() {
        return metricType;
    }


    public RecordIdentifier getTimeSeriesIdentifier() {
        return timeseriesID;
    }

    @Override
    public Iterator<MetricCollection> iterator() {
        return new MetricTimeSeriesIterator(this);
    }
}
