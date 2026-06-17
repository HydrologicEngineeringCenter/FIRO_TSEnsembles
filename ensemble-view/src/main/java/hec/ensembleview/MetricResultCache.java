package hec.ensembleview;

import hec.ensemble.stats.Statistics;
import hec.metrics.MetricCollectionTimeSeries;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Caches computed metric results (MetricCollectionTimeSeries) keyed by Statistics,
 * and probability computation results. Results are retained until explicitly
 * invalidated (e.g., when the selected record or datetime changes).
 */
class MetricResultCache {
    private final EnumMap<Statistics, MetricCollectionTimeSeries> metricMap = new EnumMap<>(Statistics.class);
    private MetricCollectionTimeSeries residentMetricCollectionTimeSeries;
    private final Map<String, Map<Float, Float>> probabilityList = new HashMap<>();
    private final Map<String, int[]> probabilityMemberIndices = new HashMap<>();

    // --- Metric map ---

    Map<Statistics, MetricCollectionTimeSeries> getMetricMap() {
        return metricMap;
    }

    void putMetric(Statistics stat, MetricCollectionTimeSeries mct) {
        metricMap.put(stat, mct);
    }

    void clearMetrics() {
        metricMap.clear();
    }

    // --- Resident metric (temporary working slot used during chart building) ---

    void setResidentMetric(MetricCollectionTimeSeries mct) {
        this.residentMetricCollectionTimeSeries = mct;
    }

    String getResidentMetricStatisticsList(ZonedDateTime zdt) {
        return residentMetricCollectionTimeSeries.getMetricCollection(zdt).getMetricStatistics();
    }

    float[][] getResidentMetricStatisticsValues(ZonedDateTime zdt) {
        return residentMetricCollectionTimeSeries.getMetricCollection(zdt).getValues();
    }

    // --- Probability list ---

    Map<String, Map<Float, Float>> getProbabilityList() {
        return probabilityList;
    }

    void putProbability(String stat, Map<Float, Float> prob) {
        probabilityList.put(stat, prob);
    }

    void putProbabilityMemberIndices(String stat, int[] indices) {
        probabilityMemberIndices.put(stat, indices);
    }

    int[] getProbabilityMemberIndices(String stat) {
        return probabilityMemberIndices.get(stat);
    }

    void clearProbabilities() {
        probabilityList.clear();
        probabilityMemberIndices.clear();
    }

    // --- Bulk invalidation ---

    /**
     * Clears all cached metrics and probability results.
     */
    void invalidate() {
        clearMetrics();
        clearProbabilities();
        residentMetricCollectionTimeSeries = null;
    }
}