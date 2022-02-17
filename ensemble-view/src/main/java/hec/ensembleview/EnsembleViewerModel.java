package hec.ensembleview;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.ensemble.EnsembleTimeSeries;
import hec.metrics.MetricCollectionTimeSeries;
import hec.stats.MaxAvgDuration;
import hec.stats.MultiStatComputable;
import hec.stats.PercentilesComputable;
import hec.stats.Statistics;

import java.time.ZonedDateTime;

public class EnsembleViewerModel {
    public SqliteDatabase db;

    public EnsembleViewerModel(SqliteDatabase db) {
        this.db = db;
    }

    public float[] computeCheckBoxStat(Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt) {
        switch(stat){
            case MIN:
            case MAX:
            case MEAN:
                return computeStatFromMultiStatComputable(stat, selectedRid, selectedZdt);
            default:
                return new float[0];
        }

    }

    public float[] computeTextBoxStat(Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt, float[] values) {
        switch(stat){
            case PERCENTILE:
                return computeStatFromPercentilesComputable(db, stat, selectedRid, selectedZdt, values);
            case MAXAVERAGEDURATION:
                return computeStatFromComputable(db, stat, selectedRid,selectedZdt, (int) values[0]);
            default:
                return new float[0];
        }
    }

    private float[] computeStatFromMultiStatComputable(Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt) {
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(selectedRid);

        MetricCollectionTimeSeries mct = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(
                new MultiStatComputable(new Statistics[] {stat}));

        return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
    }

    private float[] computeStatFromPercentilesComputable(SqliteDatabase db, Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt, float[] percentiles) {
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(selectedRid);

        MetricCollectionTimeSeries mct = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(
                new PercentilesComputable(percentiles));

        return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
    }

    private float[] computeStatFromComputable(SqliteDatabase db, Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt, int value) {
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(selectedRid);

        MetricCollectionTimeSeries mct = ets.iterateAcrossTimestepsOfEnsemblesWithSingleComputable(
                new MaxAvgDuration(value));

        return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
    }
}
