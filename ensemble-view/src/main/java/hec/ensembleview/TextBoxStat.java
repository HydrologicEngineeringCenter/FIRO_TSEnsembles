package hec.ensembleview;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.ensembleview.mappings.StatisticsStringMap;
import hec.stats.Statistics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.ZonedDateTime;

public class TextBoxStat extends JPanel implements EnsembleViewStat {
    private JLabel label;
    private JTextField textField;
    private final Statistics stat;


    public TextBoxStat(Statistics stat) {
        label = new JLabel(StatisticsStringMap.map.get(stat));
        textField = new JTextField();
        setLayout(new GridLayout(0, 2));
        add(label);
        add(textField);
        this.stat = stat;
    }

    @Override
    public StatisticUIType getStatType() {
        return StatisticUIType.TEXTBOX;
    }

    @Override
    public float[] getStatData(SqliteDatabase db, RecordIdentifier selectedRid, ZonedDateTime selectedZdt) {
        return new float[0];
    }

    @Override
    public Statistics getStat() {
        return stat;
    }

    @Override
    public void addActionListener(ActionListener l) {
        textField.addActionListener(l);
    }

    @Override
    public boolean hasInput() {
        return false;
    }
}
