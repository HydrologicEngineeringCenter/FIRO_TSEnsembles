package hec.ensembleview;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.ensemble.EnsembleTimeSeries;
import hec.metrics.MetricCollectionTimeSeries;
import hec.stats.*;

import java.time.ZonedDateTime;

public class ComputeEngine {
    public SqliteDatabase db;

    public ComputeEngine(String dbFile) throws Exception {
        this.db = new SqliteDatabase(dbFile, SqliteDatabase.CREATION_MODE.OPEN_EXISTING_NO_UPDATE);
    }

    public float[] computeCheckBoxStat(Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt, ChartType chartType) {
        switch(stat){
            case MIN:
            case MAX:
            case MEAN:
                return computeStatFromMultiStatComputable(stat, selectedRid, selectedZdt, chartType);
            case TOTAL:
                return computeStatFromTotalComputable(stat, selectedRid, selectedZdt);
            default:
                return new float[0];
        }
    }

    public static float[] computeCheckBoxStat(EnsembleTimeSeries ets, Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt, ChartType chartType) {
        switch(stat){
            case MIN:
            case MAX:
            case MEAN:
                return computeStatFromMultiStatComputable(ets, stat, selectedRid, selectedZdt, chartType);
            case TOTAL:
                return computeStatFromTotalComputable(ets, stat, selectedRid, selectedZdt);
            default:
                return new float[0];
        }
    }

    public float[][] computeRadioButtonStat(Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt, ChartType chartType) {
        switch(stat) {
            case CUMULATIVE:
                return computeStatFromCumulativeComputable(stat, selectedRid, selectedZdt);
        }
        return new float[0][0];
    }

    public float[] computeTextBoxStat(Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt, float[] values, ChartType chartType) {
        switch(stat){
            case PERCENTILE:
                return computeStatFromPercentilesComputable(db, stat, selectedRid, selectedZdt, values, chartType);
            case MAXAVERAGEDURATION:
                return computeStatFromMaxAvgDurationComputable(db, stat, selectedRid,selectedZdt, (int) values[0]);
            case MAXACCUMDURATION:
                return computeStatFromMaxAccumDurationComputable(db, stat, selectedRid,selectedZdt, (int) values[0]);
            default:
                return new float[0];
        }
    }

    public static float[] computeTextBoxStat(EnsembleTimeSeries ets, Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt, float[] values, ChartType chartType) {
        switch(stat){
            case PERCENTILE:
                return computeStatFromPercentilesComputable(ets, stat, selectedRid, selectedZdt, values, chartType);
            case MAXAVERAGEDURATION:
                return computeStatFromMaxAvgDurationComputable(ets, stat, selectedRid,selectedZdt, (int) values[0]);
            case MAXACCUMDURATION:
                return computeStatFromMaxAccumDurationComputable(ets, stat, selectedRid,selectedZdt, (int) values[0]);
            default:
                return new float[0];
        }
    }

    private float[] computeStatFromMultiStatComputable(Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt, ChartType chartType) {
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(selectedRid);
        if(chartType == ChartType.TimePlot) {
            MetricCollectionTimeSeries mct = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(new MultiStatComputable(new Statistics[]{stat}));
            return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
        } else if (chartType == ChartType.ScatterPlot) {
            MetricCollectionTimeSeries mct = ets.iterateAcrossTracesOfEnsemblesWithMultiComputable(new MultiStatComputable(new Statistics[] {stat}));
            return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
        }
        return null;
    }

    private static float[] computeStatFromMultiStatComputable(EnsembleTimeSeries ets, Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt, ChartType chartType) {
        if(chartType == ChartType.TimePlot) {
            MetricCollectionTimeSeries mct = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(new MultiStatComputable(new Statistics[]{stat}));
            return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
        } else if (chartType == ChartType.ScatterPlot) {
            MetricCollectionTimeSeries mct = ets.iterateAcrossTracesOfEnsemblesWithMultiComputable(new MultiStatComputable(new Statistics[] {stat}));
            return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
        }
        return null;
    }

    private float[] computeStatFromPercentilesComputable(SqliteDatabase db, Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt, float[] percentiles, ChartType chartType) {
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(selectedRid);
        if(chartType == ChartType.TimePlot) {
            MetricCollectionTimeSeries mct = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(new PercentilesComputable(percentiles));
            return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
        } else if (chartType == ChartType.ScatterPlot) {
            MetricCollectionTimeSeries mct = ets.iterateAcrossTracesOfEnsemblesWithMultiComputable(new PercentilesComputable(percentiles));
            return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
        }
        return null;
    }

    private static float[] computeStatFromPercentilesComputable(SqliteDatabase db, Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt, float[] percentiles, ChartType chartType) {
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(selectedRid);
        if(chartType == ChartType.TimePlot) {
            MetricCollectionTimeSeries mct = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(new PercentilesComputable(percentiles));
            return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
        } else if (chartType == ChartType.ScatterPlot) {
            MetricCollectionTimeSeries mct = ets.iterateAcrossTracesOfEnsemblesWithMultiComputable(new PercentilesComputable(percentiles));
            return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
        }
        return null;
    }

    private float[] computeStatFromMaxAvgDurationComputable(SqliteDatabase db, Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt, int value) {
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(selectedRid);

        MetricCollectionTimeSeries mct = ets.iterateAcrossEnsembleTracesWithSingleComputable(
                new MaxAvgDuration(value));

        return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
    }

    private float[] computeStatFromMaxAccumDurationComputable(SqliteDatabase db, Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt, int value) {
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(selectedRid);

        MetricCollectionTimeSeries mct = ets.iterateAcrossEnsembleTracesWithSingleComputable(
                new MaxAccumDuration(value));

        return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
    }

    private float[] computeStatFromTotalComputable(Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt) {
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(selectedRid);

        MetricCollectionTimeSeries mct = ets.iterateAcrossEnsembleTracesWithSingleComputable(
                new Total());

        return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
    }

    private static float[] computeStatFromTotalComputable(EnsembleTimeSeries ets, Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt) {
        MetricCollectionTimeSeries mct = ets.iterateAcrossEnsembleTracesWithSingleComputable(
                new Total());

        return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
    }

    private float[][] computeStatFromCumulativeComputable(Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt) {
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(selectedRid);

        MetricCollectionTimeSeries mct = ets.iterateTracesOfEnsemblesWithMultiComputable(
                new CumulativeComputable());

        return mct.getMetricCollection(selectedZdt).getValues();
    }
}
