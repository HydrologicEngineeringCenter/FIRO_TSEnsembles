package hec.ensembleview;

import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.stats.*;
import hec.metrics.MetricCollectionTimeSeries;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;

public class StatComputationHelper {
    private StatComputationHelper() {
    }

    /**
     * The StatComputationHelper class calls the metric classes and passes the ensemble time series with
     * necesary information to return computed statistics.  The computed metric is passed to the calling class to graph the results.
     * The computes can iterate across timesteps of ensembles for the time series plot or the compute can iterate
     * across traces of ensembles for the scatterplot
     */

    public static float[] computeStat(EnsembleTimeSeries ets, Statistics stat, ZonedDateTime selectedZdt, ChartType chartType) {
        switch(stat){
            case MIN:
            case MAX:
            case STANDARDDEVIATION:
            case VARIANCE:
            case AVERAGE:
                return computeStatFromMultiStatComputable(ets, stat, selectedZdt, chartType);
            case TOTAL:
                return computeStatFromTotalComputable(ets, selectedZdt);
            case UNDEFINED:
                break;
            default:
                return new float[0];
        }
        return new float[0];
    }

    public static float[][] computeTimeSeriesView(EnsembleTimeSeries ets, Statistics stat, ZonedDateTime selectedZdt) {
        if (stat == Statistics.CUMULATIVE) {
            return computeStatFromCumulativeComputable(ets, selectedZdt);
        }
        return new float[0][0];
    }

    public static Map<Float, Float> computeProbability(EnsembleTimeSeries ets, Statistics stat, ZonedDateTime selectedZdt, ChartType chartType) {
        switch(stat) {
            case MIN:
            case MAX:
            case MEDIAN:
            case AVERAGE:
            case TOTAL:
                float[] metric = computeStat(ets, stat, selectedZdt, chartType);
                return computeStatFromProbabilityComputable(metric);
            case UNDEFINED:
                break;
            default:
                return Collections.emptyMap();
        }
        return Collections.emptyMap();
    }

    public static Map<Float, Float> computeProbability(EnsembleTimeSeries ets, Statistics stat, ZonedDateTime selectedZdt, float[] values, ChartType chartType) {
        switch(stat) {
            case PERCENTILE:
            case NDAYCUMULATIVE:
            case MAXAVERAGEDURATION:
            case MAXACCUMDURATION:
                float[] metric = computeStat(ets, stat, selectedZdt, values, chartType);
                return computeStatFromProbabilityComputable(metric);
            case UNDEFINED:
                break;
            default:
                return Collections.emptyMap();
        }
        return Collections.emptyMap();
    }

    public static float[] computeStat(EnsembleTimeSeries ets, Statistics stat, ZonedDateTime selectedZdt, float[] values, ChartType chartType) {
        switch(stat){
            case PERCENTILE:
                return computeStatFromPercentilesComputable(ets, selectedZdt, values, chartType);
            case NDAYCUMULATIVE:
                return computeStatFromNDayComputable(ets, selectedZdt, values);
            case MAXAVERAGEDURATION:
                return computeStatFromMaxAvgDurationComputable(ets, selectedZdt, (int) values[0]);
            case MAXACCUMDURATION:
                return computeStatFromMaxAccumDurationComputable(ets, selectedZdt, (int) values[0]);
            case UNDEFINED:
                break;
            default:
                return new float[0];
        }
        return new float[0];
    }

    public static float computeTwoStepComputable(EnsembleTimeSeries ets, ZonedDateTime selectedZdt, Statistics stepOne, float[] stepOneValues, Statistics stepTwo, float[] stepTwoValues, boolean computeAcrossEnsembles) {
        SingleComputable compute;
        if(stepOne == Statistics.CUMULATIVE) {
            compute = new TwoStepComputable(new NDayMultiComputable(new CumulativeComputable(), (int) stepOneValues[0]), getComputable(stepTwo, stepTwoValues), false);
        } else {
            compute = new TwoStepComputable(getComputable(stepOne, stepOneValues), getComputable(stepTwo, stepTwoValues), computeAcrossEnsembles);
        }
        Ensemble e = ets.getEnsemble(selectedZdt);
        return e.singleComputeForEnsemble(compute);
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
            case PERCENTILE:
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

    private static float[] computeStatFromMultiStatComputable(EnsembleTimeSeries ets, Statistics stat, ZonedDateTime selectedZdt, ChartType chartType) {
        if(chartType == ChartType.TIMEPLOT) {
            MetricCollectionTimeSeries mct = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(new MultiStatComputable(new Statistics[]{stat}));
            return mct.getMetricCollection(selectedZdt).getComputedValuesForStatistic();
        } else if (chartType == ChartType.SCATTERPLOT) {
            MetricCollectionTimeSeries mct = ets.iterateAcrossTracesOfEnsemblesWithMultiComputable(new MultiStatComputable(new Statistics[] {stat}));
            return mct.getMetricCollection(selectedZdt).getComputedValuesForStatistic();
        }
        return new float[0];
    }

    private static float[] computeStatFromPercentilesComputable(EnsembleTimeSeries ets, ZonedDateTime selectedZdt, float[] percentiles, ChartType chartType) {
        if(chartType == ChartType.TIMEPLOT) {
            MetricCollectionTimeSeries mct = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(new PercentilesComputable(percentiles));
            return mct.getMetricCollection(selectedZdt).getComputedValuesForStatistic();
        } else if (chartType == ChartType.SCATTERPLOT) {
            MetricCollectionTimeSeries mct = ets.iterateAcrossTracesOfEnsemblesWithMultiComputable(new PercentilesComputable(percentiles));
            return mct.getMetricCollection(selectedZdt).getComputedValuesForStatistic();
        }
        return new float[0];
    }

    private static float[] computeStatFromMaxAvgDurationComputable(EnsembleTimeSeries ets, ZonedDateTime selectedZdt, int value) {
        MetricCollectionTimeSeries mct = ets.iterateAcrossEnsembleTracesWithSingleComputable(
                new MaxAvgDuration(value));

        return mct.getMetricCollection(selectedZdt).getComputedValuesForStatistic();
    }

    private static float[] computeStatFromMaxAccumDurationComputable(EnsembleTimeSeries ets, ZonedDateTime selectedZdt, int value) {
        MetricCollectionTimeSeries mct = ets.iterateAcrossEnsembleTracesWithSingleComputable(
                new MaxAccumDuration(value));

        return mct.getMetricCollection(selectedZdt).getComputedValuesForStatistic();
    }

    private static float[] computeStatFromTotalComputable(EnsembleTimeSeries ets, ZonedDateTime selectedZdt) {
        MetricCollectionTimeSeries mct = ets.iterateAcrossEnsembleTracesWithSingleComputable(
                new Total());

        return mct.getMetricCollection(selectedZdt).getComputedValuesForStatistic();
    }

    private static float[] computeStatFromNDayComputable(EnsembleTimeSeries ets, ZonedDateTime selectedZdt, float[] value) {
        MetricCollectionTimeSeries mct = ets.iterateAcrossEnsembleTracesWithSingleComputable(
                new NDayMultiComputable(new CumulativeComputable(), value));

        return mct.getMetricCollection(selectedZdt).getComputedValuesForStatistic();
    }

    private static Map<Float, Float> computeStatFromProbabilityComputable(float[] metric) {
        PlottingPositionComputable plottingPositionComputable = new PlottingPositionComputable(PlottingType.WEIBULL);
        float[] prob = plottingPositionComputable.multiCompute(metric);
        float[] values = plottingPositionComputable.orderValues(metric);

        return plottingPositionComputable.assignProbability(values, prob);
    }

    private static float[][] computeStatFromCumulativeComputable(EnsembleTimeSeries ets, ZonedDateTime selectedZdt) {
        MetricCollectionTimeSeries mct = ets.iterateTracesOfEnsemblesWithMultiComputable(
                new CumulativeComputable());

        return mct.getMetricCollection(selectedZdt).getValues();
    }
}
