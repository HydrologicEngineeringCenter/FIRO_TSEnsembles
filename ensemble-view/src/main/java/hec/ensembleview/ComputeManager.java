package hec.ensembleview;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.ensemble.EnsembleTimeSeries;
import hec.metrics.MetricCollectionTimeSeries;
import hec.stats.MultiStatComputable;
import hec.stats.Statistics;

import java.time.ZonedDateTime;

public class ComputeManager {
    static public float[] computeStat(SqliteDatabase db, Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt) {
        switch(stat){
            case MIN:
            case MAX:
            case MEAN:
                return computeStatFromMultiStatComputable(db, stat, selectedRid, selectedZdt);
            default:
                return new float[0];
        }

    }

    static private float[] computeStatFromMultiStatComputable(SqliteDatabase db, Statistics stat, RecordIdentifier selectedRid, ZonedDateTime selectedZdt) {
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(selectedRid);

        MetricCollectionTimeSeries mct = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(
                new MultiStatComputable(new Statistics[] {stat}));

        return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
    }
}
