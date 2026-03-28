package hec.ensembleview;

import hec.EnsembleDatabase;
import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.dss.ensemble.DssDatabase;
import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.stats.Statistics;
import hec.metrics.MetricCollectionTimeSeries;
import hec.metrics.MetricTypes;
import hec.ensembleview.charts.ChartType;

import hec.MetricDatabase;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central service for managing database access, current selection state (rid/zdt),
 * and property change events. Delegates data caching to EnsembleDataCache and
 * MetricResultCache.
 */
public class DatabaseHandlerService {
    private static final Logger logger = Logger.getLogger(DatabaseHandlerService.class.getName());
    private static final DatabaseHandlerService instance = new DatabaseHandlerService();
    private EnsembleDatabase ensembleDatabase;
    private RecordIdentifier rid;
    private ZonedDateTime zdt;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private final EnsembleDataCache ensembleDataCache = new EnsembleDataCache();
    private final MetricResultCache metricResultCache = new MetricResultCache();

    private DatabaseHandlerService() {
    }

    public static DatabaseHandlerService getInstance() {
        return instance;
    }

    public void addDatabaseChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    // --- Database ---

    public void setDatabase(SqliteDatabase sqliteDatabase) {
        this.ensembleDatabase = sqliteDatabase;
        ensembleDataCache.invalidate();
        metricResultCache.invalidate();
    }

    public void setDatabase(DssDatabase dssDatabase) {
        this.ensembleDatabase = dssDatabase;
        ensembleDataCache.invalidate();
        metricResultCache.invalidate();
    }

    public EnsembleDatabase getEnsembleDatabase() {
        return ensembleDatabase;
    }

    // --- Selection state ---

    public void setDbHandlerRecordIdentifier(RecordIdentifier rid) {
        RecordIdentifier currentRid = this.rid;
        if (currentRid != rid) {
            this.rid = rid;
            ensembleDataCache.invalidate();
            metricResultCache.invalidate();
            support.firePropertyChange("dbChange", false, true);
        }
    }

    public void setDbHandlerZonedDateTime(ZonedDateTime zdt) {
        ZonedDateTime currentZdt = this.zdt;
        if (currentZdt != zdt) {
            this.zdt = zdt;
            support.firePropertyChange("dbChange", false, true);
        }
    }

    public RecordIdentifier getDbHandlerRid() {
        return rid;
    }

    public ZonedDateTime getDbHandlerZdt() {
        return zdt;
    }

    // --- Ensemble data (delegated to EnsembleDataCache) ---

    public EnsembleTimeSeries getEnsembleTimeSeries() {
        return ensembleDataCache.getEnsembleTimeSeries(rid, ensembleDatabase);
    }

    public Ensemble getEnsemble() {
        return ensembleDataCache.getEnsemble(rid, zdt, ensembleDatabase);
    }

    public EnsembleTimeSeries getCumulativeEnsembleTimeSeries() {
        return ensembleDataCache.getCumulativeEnsembleTimeSeries();
    }

    public void setCumulativeEnsembleTimeSeries(EnsembleTimeSeries ensembleTimeSeries) {
        ensembleDataCache.setCumulativeEnsembleTimeSeries(ensembleTimeSeries);
    }

    // --- Metric results (delegated to MetricResultCache) ---

    public Map<Statistics, MetricCollectionTimeSeries> getMetricCollectionTimeSeriesMap() {
        return metricResultCache.getMetricMap();
    }

    public void setMetricCollectionTimeSeriesMap(Statistics stat, MetricCollectionTimeSeries metricCollectionTimeSeries) {
        metricResultCache.putMetric(stat, metricCollectionTimeSeries);
    }

    public void refreshMetricCollectionTimeSeriesMap() {
        metricResultCache.clearMetrics();
    }

    public void setResidentMetricCollectionTimeSeries(MetricCollectionTimeSeries metricCollectionsTimeSeries) {
        metricResultCache.setResidentMetric(metricCollectionsTimeSeries);
    }

    public String getResidentMetricStatisticsList() {
        return metricResultCache.getResidentMetricStatisticsList(zdt);
    }

    public float[][] getResidentMetricStatisticsValues() {
        return metricResultCache.getResidentMetricStatisticsValues(zdt);
    }

    // --- Probability results (delegated to MetricResultCache) ---

    public void setEnsembleProbabilityMap(String stat, Map<Float, Float> ensembleProb) {
        metricResultCache.putProbability(stat, ensembleProb);
    }

    public Map<String, Map<Float, Float>> getEnsembleProbabilityList() {
        return metricResultCache.getProbabilityList();
    }

    public void refreshEnsembleProbabilityList() {
        metricResultCache.clearProbabilities();
    }

    // --- Save metrics to database ---

    public List<String> saveComputedMetricsToDatabase(ChartType chartType) {
        List<String> savedMetrics = new java.util.ArrayList<>();
        if (ensembleDatabase == null || !(ensembleDatabase instanceof MetricDatabase)) {
            return savedMetrics;
        }
        MetricTypes targetType = (chartType == ChartType.TIMEPLOT)
                ? MetricTypes.TIMESERIES_OF_ARRAY
                : MetricTypes.ARRAY_OF_ARRAY;
        MetricDatabase metricDb = (MetricDatabase) ensembleDatabase;
        Map<Statistics, MetricCollectionTimeSeries> metricMap = getMetricCollectionTimeSeriesMap();
        for (Map.Entry<Statistics, MetricCollectionTimeSeries> entry : metricMap.entrySet()) {
            if (entry.getValue().getMetricType() != targetType) {
                continue;
            }
            try {
                metricDb.write(entry.getValue());
                savedMetrics.add(entry.getKey().toString());
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to save metric " + entry.getKey() + " to database", e);
            }
        }
        return savedMetrics;
    }
}