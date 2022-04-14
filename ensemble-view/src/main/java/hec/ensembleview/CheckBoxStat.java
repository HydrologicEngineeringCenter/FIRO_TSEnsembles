package hec.ensembleview;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.ensembleview.mappings.StatisticsStringMap;
import hec.stats.Statistics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.ZonedDateTime;

public class CheckBoxStat extends JPanel implements EnsembleViewStat {
    private final JCheckBox checkBox;
    private final Statistics stat;

    public CheckBoxStat(Statistics stat) {
        setLayout(new GridLayout(1, 1));
        checkBox = new JCheckBox(StatisticsStringMap.map.get(stat));
        add(checkBox);
        this.stat = stat;
        setPreferredSize(new Dimension(10, 10));
        validate();
    }

    @Override
    public StatisticUIType getStatUIType() {
        return StatisticUIType.CHECKBOX;
    }

    @Override
    public Statistics getStatType() {
        return stat;
    }

    @Override
    public void addActionListener(ActionListener l) {
        checkBox.addActionListener(l);
    }

    @Override
    public boolean hasInput() {
        return checkBox.isSelected();
    }
}
