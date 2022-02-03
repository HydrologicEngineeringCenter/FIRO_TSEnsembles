package hec.ensembleview;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.stats.Statistics;

import java.awt.event.ActionListener;
import java.time.ZonedDateTime;

public interface EnsembleViewStat {
    StatisticUIType getStatUIType();
    Statistics getStatType();
    void addActionListener(ActionListener l);
    boolean hasInput();
    //boolean isEnabled();
}
