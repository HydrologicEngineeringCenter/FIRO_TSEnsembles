package hec.ensembleview;

import hec.ensembleview.mappings.StatisticsStringMap;
import hec.ensemble.stats.Statistics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class RadioButtonStat extends JPanel implements EnsembleViewStat {
    private final JRadioButton radioButton;
    private final Statistics stat;

    public RadioButtonStat(Statistics stat) {
        setLayout(new BorderLayout());
        radioButton = new JRadioButton(StatisticsStringMap.map.get(stat));
        add(radioButton);
        this.stat = stat;
    }

    @Override
    public StatisticUIType getStatUIType() {
        return StatisticUIType.RADIOBUTTON;
    }

    @Override
    public Statistics getStatType() {
        return stat;
    }

    public JRadioButton getRadioButton() {
        return radioButton;
    }

    @Override
    public void addActionListener(ActionListener l) {
        radioButton.addActionListener(l);
    }

    @Override
    public boolean hasInput() {
        return radioButton.isSelected();
    }
}
