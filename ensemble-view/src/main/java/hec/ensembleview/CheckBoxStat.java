package hec.ensembleview;

import hec.ensembleview.mappings.StatisticsStringMap;
import hec.ensemble.stats.Statistics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class CheckBoxStat extends JPanel implements EnsembleViewStat {
    private final JCheckBox checkBox;
    private final Statistics stat;

    public CheckBoxStat(Statistics stat) {
        setLayout(new BorderLayout());
        checkBox = new JCheckBox(StatisticsStringMap.map.get(stat));
        checkBox.setFont(DefaultSettings.setSegoeFontText());
        add(checkBox);
        this.stat = stat;
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
