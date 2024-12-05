package hec.ensembleview;

import hec.EnsembleDatabase;
import hec.MetricDatabase;
import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.dss.ensemble.DssDatabase;
import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.stats.Statistics;
import hec.ensembleview.mappings.StatisticsMap;
import hec.metrics.MetricCollectionTimeSeries;
import hec.metrics.MetricTypes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.ZonedDateTime;
import java.util.*;

public class DatabaseHandlerService {  // handles ensemble and metric database and gets ensemble and metric time series
    private static final DatabaseHandlerService instance = new DatabaseHandlerService();
    private EnsembleDatabase ensembleDatabase;
    private MetricDatabase metricDatabase;
//    private MetricCollectionTimeSeries metricCollectionTimeSeries; - for viewing metrics in database
    private EnsembleTimeSeries cumulativeEnsembleTimeSeries;
    private final List<MetricCollectionTimeSeriesContainer> metricCollectionTimeSeriesContainers = new ArrayList<>();
    private MetricCollectionTimeSeries residentMetricCollectionTimeSeries;
    private final Map<String, Map<Float, Float>> ensembleProbabilityList = new HashMap<>();
    private RecordIdentifier rid;
    private ZonedDateTime zdt;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private DatabaseHandlerService() {
    }

    public static DatabaseHandlerService getInstance() {
        return instance;
    }

    public void addDatabaseChangeListener(PropertyChangeListener  listener) {
        support.addPropertyChangeListener(listener);
    }

    public void setDatabase(SqliteDatabase sqliteDatabase) {
        this.ensembleDatabase = sqliteDatabase;
        this.metricDatabase = sqliteDatabase;
    }

    public void setDatabase(DssDatabase dssDatabase) {
        this.ensembleDatabase = dssDatabase;
        this.metricDatabase = dssDatabase;
    }

    public void setDbHandlerRecordIdentifier(RecordIdentifier rid) {
        RecordIdentifier currentRid = this.rid;
        if(currentRid != rid) {
            this.rid = rid;
            support.firePropertyChange("dbChange", currentRid, rid);
        }
    }

    public void setDbHandlerZonedDateTime(ZonedDateTime zdt) {
        ZonedDateTime currentZdt = this.zdt;
        if(currentZdt != zdt) {
            this.zdt = zdt;
            support.firePropertyChange("dbChange", false, true);
        }
    }

    public EnsembleDatabase getEnsembleDatabase() {
        return ensembleDatabase;
    }

    public RecordIdentifier getDbHandlerRid() {
        return rid;
    }

    public ZonedDateTime getDbHandlerZdt() {
        return zdt;
    }

    public void setResidentMetricCollectionTimeSeries(MetricCollectionTimeSeries metricCollectionsTimeSeries) {
        this.residentMetricCollectionTimeSeries = metricCollectionsTimeSeries;
    }

    public List<MetricCollectionTimeSeriesContainer> getMetricCollectionTimeSeriesContainer() {
        return metricCollectionTimeSeriesContainers;
    }

    public void refreshMetricCollectionTimeSeriesMap(StatisticsMap map) {
        for (Map.Entry<String, Boolean> entry : map.getTimeSeriesMapList().entrySet()) {
            if (entry.getValue() == Boolean.FALSE) {
                this.metricCollectionTimeSeriesContainers.removeIf(container ->
                        container.getStatistics().equals(Statistics.getStatName(entry.getKey())) &&
                                container.getMetricTypes().equals(MetricTypes.TIMESERIES_OF_ARRAY));
            }
        }
    }

    public void refreshMetricCollectionEnsembleSeriesMap(StatisticsMap map) {
        for (Map.Entry<String, Boolean> entry : map.getEnsembleSeriesMapList().entrySet()) {
            if (entry.getValue() == Boolean.FALSE) {
                this.metricCollectionTimeSeriesContainers.removeIf(container ->
                        container.getStatistics().equals(Statistics.getStatName(entry.getKey())) &&
                                container.getMetricTypes().equals(MetricTypes.ARRAY_OF_ARRAY));
            }
        }
    }

    public void refreshEnsembleProbabilityList() {
        ensembleProbabilityList.clear();
    }

    public void setMetricCollectionTimeSeriesContainer(MetricCollectionTimeSeriesContainer container) {
        this.metricCollectionTimeSeriesContainers.add(container);
    }


    public void saveMctsToDatabase(MetricCollectionTimeSeries metricCollectionTimeSeries) throws Exception {
        if (metricCollectionTimeSeries.getMetricType().equals(MetricTypes.TIMESERIES_OF_ARRAY)) {
            this.metricDatabase.write(metricCollectionTimeSeries);
        }
        if (metricCollectionTimeSeries.getMetricType().equals(MetricTypes.ARRAY_OF_ARRAY)) {
            this.metricDatabase.write(metricCollectionTimeSeries.getMetricCollection(zdt));
        }
    }

    public void setCumulativeEnsembleTimeSeries(EnsembleTimeSeries ensembleTimeSeries) {
        this.cumulativeEnsembleTimeSeries = ensembleTimeSeries;
    }

    public void setEnsembleProbabilityMap(String stat, Map<Float, Float> ensembleProb) {
        this.ensembleProbabilityList.put(stat, ensembleProb);
    }

    public Map<String, Map<Float, Float>> getEnsembleProbabilityList() {
        return this.ensembleProbabilityList;
    }

    public EnsembleTimeSeries getCumulativeEnsembleTimeSeries() {
        return this.cumulativeEnsembleTimeSeries;
    }

    public String getResidentMetricStatisticsList() {
        return this.residentMetricCollectionTimeSeries.getMetricCollection(zdt).getMetricStatistics();
    }

    public float[][] getResidentMetricStatisticsValues() {
        return residentMetricCollectionTimeSeries.getMetricCollection(zdt).getValues();
    }

    public EnsembleTimeSeries getEnsembleTimeSeries() {
        return this.ensembleDatabase.getEnsembleTimeSeries(rid);
    }

    public Ensemble getEnsemble() {
        return getEnsembleTimeSeries().getEnsemble(zdt);
    }
}
