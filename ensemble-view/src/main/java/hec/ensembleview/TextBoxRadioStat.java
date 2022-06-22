package hec.ensembleview;

import hec.ensembleview.mappings.StatisticsStringMap;
import hec.stats.Statistics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class TextBoxRadioStat extends JPanel implements EnsembleViewStat {
//    private JLabel label;
    private JTextField textField;
    private JRadioButton radioButton;
    private final Statistics stat;


    public TextBoxRadioStat(Statistics stat) {
//        label = new JLabel(StatisticsStringMap.map.get(stat));
        radioButton = new JRadioButton(StatisticsStringMap.map.get(stat));
        textField = new JTextField();
        setLayout(new GridLayout(1, 2));
        add(radioButton);
        add(textField);
        this.stat = stat;
        textField.setEditable(false);
        radioButton.addActionListener(e -> {
            if(radioButton.isSelected()) {
                textField.setEditable(true);
            } else if(!radioButton.isSelected()) {
                textField.setEditable(false);
            }
        });
        setPreferredSize(getPreferredSize());
        validate();
    }

    public float[] getTextFieldValue() {
        String textValues = textField.getText();
        String[] textValuesParse = textValues.trim().split("[,:;]");
        float[] floatValuesParse = new float[textValuesParse.length];
        for(int i = 0; i < textValuesParse.length; i++) {
            floatValuesParse[i] = Float.parseFloat(textValuesParse[i]);
        }
        return floatValuesParse;
    }

    @Override
    public StatisticUIType getStatUIType() {
        return StatisticUIType.TEXTBOXRADIO;
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
        textField.addActionListener(l);

    }

    @Override
    public boolean hasInput() {
        if(radioButton.isSelected() && !textField.getText().isEmpty()) {
            return radioButton.isSelected();
        }
        return false;
    }
}
