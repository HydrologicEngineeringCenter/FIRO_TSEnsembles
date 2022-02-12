package hec.ensembleview;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.ensembleview.mappings.StatisticsStringMap;
import hec.stats.Statistics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.ZonedDateTime;
import java.util.Arrays;

public class TextBoxStat extends JPanel implements EnsembleViewStat {
//    private JLabel label;
    private JTextField textField;
    private JCheckBox checkBox;
    private final Statistics stat;


    public TextBoxStat(Statistics stat) {
//        label = new JLabel(StatisticsStringMap.map.get(stat));
        checkBox = new JCheckBox(StatisticsStringMap.map.get(stat));
        textField = new JTextField();
        setLayout(new GridLayout(0, 2));
        add(checkBox);
        add(textField);
        this.stat = stat;

    }

    public float[] getTextFieldValue() {
        String textValues = textField.getText();
        String[] textValuesParse = textValues.strip().split("[,:;]");
        float[] floatValuesParse = new float[textValuesParse.length];
        for(int i = 0; i < textValuesParse.length; i++) {
            floatValuesParse[i] = Float.parseFloat(textValuesParse[i]);
        }
        return floatValuesParse;
    }

    @Override
    public StatisticUIType getStatUIType() {
        return StatisticUIType.TEXTBOX;
    }

    @Override
    public Statistics getStatType() {
        return stat;
    }

    @Override
    public void addActionListener(ActionListener l) {
            textField.addActionListener(l);

    }

    @Override
    public boolean hasInput() {
        if(checkBox.isSelected() && !textField.getText().isEmpty()) {
            return checkBox.isSelected();
        }
        return false;
    }
}
