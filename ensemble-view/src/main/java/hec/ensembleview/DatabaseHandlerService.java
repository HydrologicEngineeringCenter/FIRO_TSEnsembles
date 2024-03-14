package hec.ensembleview;

import hec.EnsembleDatabase;
import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.dss.ensemble.DssDatabase;
import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.stats.Statistics;
import hec.metrics.MetricCollectionTimeSeries;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class DatabaseHandlerService {  // handles ensemble and metric database and gets ensemble and metric time series
    private static final DatabaseHandlerService instance = new DatabaseHandlerService();
    private EnsembleDatabase ensembleDatabase;
//    private MetricDatabase metricDatabase;  - for viewing metrics in database
//    private MetricCollectionTimeSeries metricCollectionTimeSeries; - for viewing metrics in database
    private EnsembleTimeSeries cumulativeEnsembleTimeSeries;
    private final EnumMap<Statistics, MetricCollectionTimeSeries> metricCollectionTimeSeriesMap = new EnumMap<>(Statistics.class);
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
//        this.metricDatabase = sqliteDatabase; - for viewing metrics in database
    }

    public void setDatabase(DssDatabase dssDatabase) {
        this.ensembleDatabase = dssDatabase;
//        this.metricDatabase = dssDatabase; - for viewing metrics in database
    }

    public void setDbHandlerRecordIdentifier(RecordIdentifier rid) {
        RecordIdentifier currentRid = this.rid;
        if(currentRid != rid) {
            this.rid = rid;
            support.firePropertyChange("dbChange", false, true);
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

    public Map<Statistics, MetricCollectionTimeSeries> getMetricCollectionTimeSeriesMap() {
        return metricCollectionTimeSeriesMap;
    }

    public void refreshMetricCollectionTimeSeriesMap() {
        metricCollectionTimeSeriesMap.clear();
    }

    public void refreshEnsembleProbabilityList() {
        ensembleProbabilityList.clear();
    }

    public void setMetricCollectionTimeSeriesMap(Statistics stat, MetricCollectionTimeSeries metricCollectionTimeSeries) {
        this.metricCollectionTimeSeriesMap.put(stat, metricCollectionTimeSeries);
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
