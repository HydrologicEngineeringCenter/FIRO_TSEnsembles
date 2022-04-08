package hec.ensembleview;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.ensemble.EnsembleTimeSeries;
import hec.metrics.MetricCollectionTimeSeries;
import hec.stats.*;

import java.time.ZonedDateTime;

public class EnsembleViewerModel {
    public SqliteDatabase db;

    public EnsembleViewerModel(String dbFile) throws Exception {
        this.db = new SqliteDatabase(dbFile, SqliteDatabase.CREATION_MODE.OPEN_EXISTING_NO_UPDATE);
    }

    public float[] computeCheckBoxStat(Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt) {
        switch(stat){
            case MIN:
            case MAX:
            case MEAN:
                return computeStatFromMultiStatComputable(stat, selectedRid, selectedZdt);
            case TOTAL:
                return computeStatFromTotalComputable(stat, selectedRid, selectedZdt);
            case CUMULATIVE:
                return computeStatFromCumulativeComputable(stat, selectedRid, selectedZdt);
            default:
                return new float[0];
        }

    }

    public float[] computeTextBoxStat(Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt, float[] values) {
        switch(stat){
            case PERCENTILE:
                return computeStatFromPercentilesComputable(db, stat, selectedRid, selectedZdt, values);
            case MAXAVERAGEDURATION:
                return computeStatFromMaxAvgDurationComputable(db, stat, selectedRid,selectedZdt, (int) values[0]);
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

    private float[] computeStatFromMaxAvgDurationComputable(SqliteDatabase db, Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt, int value) {
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(selectedRid);

        MetricCollectionTimeSeries mct = ets.iterateAcrossTimestepsOfEnsemblesWithSingleComputable(
                new MaxAvgDuration(value));

        return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
    }

    private float[] computeStatFromTotalComputable(Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt) {
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(selectedRid);

        MetricCollectionTimeSeries mct = ets.iterateAcrossTimestepsOfEnsemblesWithSingleComputable(
                new Total());

        return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
    }
    private float[] computeStatFromCumulativeComputable(Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt) {
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(selectedRid);

        MetricCollectionTimeSeries mct = ets.iterateTracesOfEnsemblesWithMultiComputable(
                new CumulativeComputable());

        return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
    }
}
