package hec.ensembleview;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensembleview.mappings.StatisticsStringMap;
import hec.metrics.MetricCollectionTimeSeries;
import hec.stats.MultiStatComputable;
import hec.stats.Statistics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.ZonedDateTime;

public class CheckBoxStat extends JPanel implements EnsembleViewStat {
    private final JCheckBox checkBox;
    private final Statistics stat;

    public CheckBoxStat(Statistics stat) {
        setLayout(new GridLayout(0, 1));
        checkBox = new JCheckBox(StatisticsStringMap.map.get(stat));
        add(checkBox);
        this.stat = stat;
    }

    @Override
    public StatisticUIType getStatType() {
        return StatisticUIType.CHECKBOX;
    }

    @Override
    public float[] getStatData(SqliteDatabase db, RecordIdentifier selectedRid, ZonedDateTime selectedZdt) {
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(selectedRid);

        MetricCollectionTimeSeries mct = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(
                new MultiStatComputable(new Statistics[] {stat}));

        return mct.getMetricCollection(selectedZdt).getDateForStatistic(stat);
    }

    @Override
    public Statistics getStat() {
        return stat;
    }

    @Override
    public void addActionListeners(ActionListener l) {
        checkBox.addActionListener(l);
    }
}
