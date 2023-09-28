package hec.ensembleview;

import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.stats.*;
import hec.ensembleview.charts.ChartType;
import hec.metrics.MetricCollection;
import hec.metrics.MetricCollectionTimeSeries;
import hec.metrics.MetricTypes;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;

public class StatComputationHelper {
    public final DatabaseHandlerService databaseHandlerService;
    public StatComputationHelper() {
        databaseHandlerService = DatabaseHandlerService.getInstance();
    }

    /**
     * The StatComputationHelper class calls the metric classes and passes the ensemble time series with
     * necessary information to return computed statistics.  The computed metric is passed to the calling class to graph the results.
     * The computes can iterate across time-steps of ensembles for the time series plot or compute can iterate
     * across traces of ensembles for the scatter plot
     */

    public void computeStat(EnsembleTimeSeries ets, Statistics stat, ChartType chartType) {
        switch (stat) {
            case MIN:
            case MAX:
            case STANDARDDEVIATION:
            case VARIANCE:
            case AVERAGE:
                computeStatFromMultiStatComputable(ets, stat, chartType);
                break;
            case TOTAL:
                computeStatFromTotalComputable(ets, stat);
                break;
            default:
                break;
        }
    }

    public void computeStat(EnsembleTimeSeries ets, Statistics stat, float[] values, ChartType chartType) {
        switch(stat){
            case PERCENTILES:
                computeStatFromPercentilesComputable(ets, values, chartType);
                break;
            case NDAYCOMPUTABLE:
                computeStatFromNDayComputable(ets, stat, values);
                break;
            default:
                break;
        }
    }

    public MetricCollectionTimeSeries computeTwoStepComputable(Statistics stepOne, float[] stepOneValues, Statistics stepTwo, float[] stepTwoValues, boolean computeAcrossEnsembles) {
        SingleComputable compute;
        if(stepOne == Statistics.CUMULATIVE) {
            compute = new TwoStepComputable(new NDayMultiComputable(new CumulativeComputable(), stepOneValues), getComputable(stepTwo, stepTwoValues), false);
        } else {
            compute = new TwoStepComputable(getComputable(stepOne, stepOneValues), getComputable(stepTwo, stepTwoValues), computeAcrossEnsembles);
        }
        return databaseHandlerService.getEnsembleTimeSeries().computeSingleValueSummary(compute);
    }

    public static Computable getComputable(Statistics stat, float[] values) {
        switch (stat) {
            case MIN:
                return new MinComputable();
            case MAX:
                return new MaxComputable();
            case AVERAGE:
                return new MeanComputable();
            case MEDIAN:
                return new MedianComputable();
            case STANDARDDEVIATION:
                return new MultiStatComputable(new Statistics[] {Statistics.STANDARDDEVIATION});
            case VARIANCE:
                return new MultiStatComputable(new Statistics[] {Statistics.VARIANCE});
            case PERCENTILES:
                return new PercentilesComputable(values);
            case TOTAL:
                return new Total();
            case MAXACCUMDURATION:
                return new MaxAccumDuration((int) values[0]);
            case MAXAVERAGEDURATION:
                return new MaxAvgDuration((int) values[0]);
            case UNDEFINED:
                break;
            default:
                return null;
        }
        return null;
    }

    private void computeStatFromMultiStatComputable(EnsembleTimeSeries ets, Statistics stat, ChartType chartType) {
        if (chartType == ChartType.TIMEPLOT) {
            MetricCollectionTimeSeries mct = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(new MultiStatComputable(new Statistics[]{stat}));
            databaseHandlerService.setMetricCollectionTimeSeriesMap(stat, mct);

        } else if (chartType == ChartType.SCATTERPLOT) {
            MetricCollectionTimeSeries mct = ets.iterateAcrossTracesOfEnsemblesWithMultiComputable(new MultiStatComputable(new Statistics[]{stat}));
            databaseHandlerService.setMetricCollectionTimeSeriesMap(stat, mct);
        }
    }

    private void computeStatFromPercentilesComputable(EnsembleTimeSeries ets, float[] percentiles, ChartType chartType) {
        if(chartType == ChartType.TIMEPLOT) {
            MetricCollectionTimeSeries mct = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(new PercentilesComputable(percentiles));
            databaseHandlerService.setMetricCollectionTimeSeriesMap(Statistics.PERCENTILES, mct);
        } else if (chartType == ChartType.SCATTERPLOT) {
            MetricCollectionTimeSeries mct = ets.iterateAcrossTracesOfEnsemblesWithMultiComputable(new PercentilesComputable(percentiles));
            databaseHandlerService.setMetricCollectionTimeSeriesMap(Statistics.PERCENTILES, mct);
        }
    }

    private void computeStatFromTotalComputable(EnsembleTimeSeries ets, Statistics stat) {
        MetricCollectionTimeSeries mct = ets.iterateAcrossEnsembleTracesWithSingleComputable(new Total());
        databaseHandlerService.setMetricCollectionTimeSeriesMap(stat, mct);
    }

    private void computeStatFromNDayComputable(EnsembleTimeSeries ets, Statistics stat, float[] value) {
        MetricCollectionTimeSeries mct = ets.iterateAcrossTracesOfEnsemblesWithMultiComputable(new NDayMultiComputable(new CumulativeComputable(), value));
        databaseHandlerService.setMetricCollectionTimeSeriesMap(stat, mct);
    }

    public void computeStatFromProbabilityComputable() {
        PlottingPositionComputable plottingPositionComputable = new PlottingPositionComputable(PlottingType.WEIBULL);
        Map<Statistics, MetricCollectionTimeSeries> computedMetrics = databaseHandlerService.getMetricCollectionTimeSeriesMap();

        for(Map.Entry<Statistics, MetricCollectionTimeSeries> entry : computedMetrics.entrySet()) {
            if(entry.getValue().getMetricType() == MetricTypes.ARRAY_OF_ARRAY) {
                MetricCollection mc = entry.getValue().getMetricCollection(databaseHandlerService.getDbHandlerZdt());
                String savedStat = mc.getMetricStatistics();
                String[] savedStats = savedStat.split("\\|");
                float[][] vals = mc.getValues();
                for(int i = 0; i < vals.length; i++) {
                    float[] prob = plottingPositionComputable.multiCompute(vals[i]);
                    float[] values = plottingPositionComputable.orderValues(vals[i]);
                    databaseHandlerService.setEnsembleProbabilityMap(savedStats[i], plottingPositionComputable.assignProbability(prob, values));
                }
            }
        }
    }

    public void convertToCumulative(EnsembleTimeSeries ets) {
        MetricCollectionTimeSeries mct = computeStatFromCumulativeComputable(ets);

        float[][] cumulativeEnsembles= mct.getMetricCollection(databaseHandlerService.getDbHandlerZdt()).getValues();
        ZonedDateTime issueDate = ets.getEnsemble(databaseHandlerService.getDbHandlerZdt()).getIssueDate();
        ZonedDateTime startDate = ets.getEnsemble(databaseHandlerService.getDbHandlerZdt()).getStartDateTime();
        Duration duration = ets.getEnsemble(databaseHandlerService.getDbHandlerZdt()).getInterval();
        String dataType = ets.getDataType();
        String version = ets.getVersion();
        String units = mct.getUnits();

        EnsembleTimeSeries cumulativeEts = new EnsembleTimeSeries(databaseHandlerService.getDbHandlerRid(), units, dataType,version);
        cumulativeEts.addEnsemble(new Ensemble(issueDate, cumulativeEnsembles, startDate, duration, units));
        databaseHandlerService.setCumulativeEnsembleTimeSeries(cumulativeEts);
    }

    private MetricCollectionTimeSeries computeStatFromCumulativeComputable(EnsembleTimeSeries ets) {
        return ets.iterateTracesOfEnsemblesWithMultiComputable(new CumulativeComputable());
    }
}
