package hec.ensembleview;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.stats.Statistics;

import java.awt.event.ActionListener;
import java.time.ZonedDateTime;

public interface EnsembleViewStat { //is implemented by the buttons.
    StatisticUIType getStatUIType();
    Statistics getStatType();
    void addActionListener(ActionListener l);  //action listener that needs to be overridden by class that implments
    boolean hasInput();
    //boolean isEnabled();
}
