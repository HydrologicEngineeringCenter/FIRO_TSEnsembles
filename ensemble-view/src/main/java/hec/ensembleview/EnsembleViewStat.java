package hec.ensembleview;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.stats.Statistics;

import java.awt.event.ActionListener;
import java.time.ZonedDateTime;

public interface EnsembleViewStat {
    StatisticUIType getStatType();
    float[] getStatData(SqliteDatabase db, RecordIdentifier selectedRid, ZonedDateTime selectedZdt);
    Statistics getStat();
    void addActionListeners(ActionListener l);
    //boolean isEnabled();
}
