package hec.metrics;

import hec.RecordIdentifier;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class MetricCollectionTimeSeries implements  Iterable<MetricCollection>
{

    private RecordIdentifier timeseriesID;
    private String units;
    private String dataType;
    private String version;
    //https://docs.oracle.com/javase/7/docs/api/java/util/TreeMap.html
    private TreeMap<ZonedDateTime,MetricCollection> items;

    public int getCount()
    {
        return items.size();
    }

    public MetricCollectionTimeSeries(RecordIdentifier timeseriesID, String units, String dataType, String version)
    {
        init(timeseriesID,units,dataType,version);
    }

    private void init(RecordIdentifier timeseriesID, String units, String dataType, String version) {
        this.timeseriesID = timeseriesID;
        this.units = units;
        this.dataType = dataType;
        this.version = version;
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

    public String getDataType() {
        return dataType;
    }

    public String getVersion() {
        return version;
    }

    public RecordIdentifier getTimeSeriesIdentifier() {
        return timeseriesID;
    }

    @Override
    public Iterator<MetricCollection> iterator() {
        return new MetricTimeSeriesIterator(this);
    }
}
