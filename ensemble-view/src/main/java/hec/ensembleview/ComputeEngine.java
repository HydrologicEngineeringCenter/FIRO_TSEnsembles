package hec.ensembleview;

import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.metrics.MetricCollectionTimeSeries;
import hec.stats.*;

import java.time.ZonedDateTime;

public class ComputeEngine {

    /**
     * The Compute Engine class calls the metrics class and passes the compute as a checkbox, textbox, or radio button.  Compute iterates across timesteps of ensembles
     * for the time series plot or the compute can iterate across traces of ensembles for the scatterplot
     */

    public ComputeEngine() {
    }

    public float[] computeCheckBoxStat(EnsembleTimeSeries ets, Statistics stat, ZonedDateTime selectedZdt, ChartType chartType) {
        switch(stat){
            case MIN:
            case MAX:
            case AVERAGE:
                return computeStatFromMultiStatComputable(ets, stat, selectedZdt, chartType);
            case TOTAL:
                return computeStatFromTotalComputable(ets, stat, selectedZdt);
            default:
                return new float[0];
        }
    }

    public float[][] computeRadioButtonTimeSeriesView(EnsembleTimeSeries ets, Statistics stat, ZonedDateTime selectedZdt, ChartType chartType) {
        switch(stat) {
            case CUMULATIVE:
                return computeStatFromCumulativeComputable(ets, selectedZdt);
        }
        return new float[0][0];
    }

    public float[] computeTextBoxStat(EnsembleTimeSeries ets, Statistics stat, ZonedDateTime selectedZdt, float[] values, ChartType chartType) {
        switch(stat){
            case PERCENTILE:
                return computeStatFromPercentilesComputable(ets, stat, selectedZdt, values, chartType);
            case MAXAVERAGEDURATION:
                return computeStatFromMaxAvgDurationComputable(ets, stat, selectedZdt, (int) values[0]);
            case MAXACCUMDURATION:
                return computeStatFromMaxAccumDurationComputable(ets, stat, selectedZdt, (int) values[0]);
            default:
                return new float[0];
        }
    }

    public float computeTwoStepComputable(EnsembleTimeSeries ets, ZonedDateTime selectedZdt, Statistics stepOne, float[] stepOneValues, Statistics stepTwo, float[] stepTwoValues, boolean computeAcrossEnsembles) {
        SingleComputable compute;
        if(stepOne == Statistics.CUMULATIVE) {
            compute = new TwoStepComputable(new NDayMultiComputable(new CumulativeComputable(), (int) stepOneValues[0]), getComputable(stepTwo, stepTwoValues), false);
        } else {
            compute = new TwoStepComputable(getComputable(stepOne, stepOneValues), getComputable(stepTwo, stepTwoValues), computeAcrossEnsembles);
        }
        Ensemble e = ets.getEnsemble(selectedZdt);
        return e.singleComputeForEnsemble(compute);
    }

    private Computable getComputable(Statistics stat, float[] values) {
        switch (stat) {
            case MIN:
                return new MinComputable();
            case MAX:
                return new MaxComputable();
            case AVERAGE:
                return new MeanComputable();
            case MEDIAN:
                return new MedianComputable();
            case PERCENTILE:
                return new PercentilesComputable(values);
            case TOTAL:
                return new Total();
            case MAXACCUMDURATION:
                return new MaxAccumDuration((int) values[0]);
            case MAXAVERAGEDURATION:
                return new MaxAvgDuration((int) values[0]);
            default:
                return null;
        }
    }

/*    public float computeNDayMultiComputable(EnsembleTimeSeries ets, ZonedDateTime selectedZdt, Statistics stepOne, Statistics stepTwo, float[] stepTwoValues) {
        SingleComputable compute
    }*/

    private float[] computeStatFromMultiStatComputable(EnsembleTimeSeries ets, Statistics stat, ZonedDateTime selectedZdt, ChartType chartType) {
        if(chartType == ChartType.TimePlot) {
            MetricCollectionTimeSeries mct = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(new MultiStatComputable(new Statistics[]{stat}));
            return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
        } else if (chartType == ChartType.ScatterPlot) {
            MetricCollectionTimeSeries mct = ets.iterateAcrossTracesOfEnsemblesWithMultiComputable(new MultiStatComputable(new Statistics[] {stat}));
            return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
        }
        return null;
    }

    private float[] computeStatFromPercentilesComputable(EnsembleTimeSeries ets, Statistics stat, ZonedDateTime selectedZdt, float[] percentiles, ChartType chartType) {
        if(chartType == ChartType.TimePlot) {
            MetricCollectionTimeSeries mct = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(new PercentilesComputable(percentiles));
            return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
        } else if (chartType == ChartType.ScatterPlot) {
            MetricCollectionTimeSeries mct = ets.iterateAcrossTracesOfEnsemblesWithMultiComputable(new PercentilesComputable(percentiles));
            return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
        }
        return null;
    }

    private float[] computeStatFromMaxAvgDurationComputable(EnsembleTimeSeries ets, Statistics stat, ZonedDateTime selectedZdt, int value) {
        MetricCollectionTimeSeries mct = ets.iterateAcrossEnsembleTracesWithSingleComputable(
                new MaxAvgDuration(value));

        return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
    }

    private float[] computeStatFromMaxAccumDurationComputable(EnsembleTimeSeries ets, Statistics stat, ZonedDateTime selectedZdt, int value) {
        MetricCollectionTimeSeries mct = ets.iterateAcrossEnsembleTracesWithSingleComputable(
                new MaxAccumDuration(value));

        return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
    }

    private float[] computeStatFromTotalComputable(EnsembleTimeSeries ets, Statistics stat, ZonedDateTime selectedZdt) {
        MetricCollectionTimeSeries mct = ets.iterateAcrossEnsembleTracesWithSingleComputable(
                new Total());

        return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
    }

    private float[][] computeStatFromCumulativeComputable(EnsembleTimeSeries ets, ZonedDateTime selectedZdt) {
        MetricCollectionTimeSeries mct = ets.iterateTracesOfEnsemblesWithMultiComputable(
                new CumulativeComputable());

        return mct.getMetricCollection(selectedZdt).getValues();
    }
}
