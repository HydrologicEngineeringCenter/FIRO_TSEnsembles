package hec.ensembleview;

import hec.ensembleview.mappings.MovingAvgComboBoxMap;
import hec.ensembleview.RadioButtonStat;
import hec.ensembleview.mappings.StatisticsStringMap;
import hec.ensembleview.mappings.StatisticsUITypeMap;
import hec.stats.MovingAvg;
import hec.stats.Statistics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Objects;

public class TextBoxRadioStat extends JPanel implements EnsembleViewStat {
    //    private JLabel label;
    private JTextField textField;
    private JRadioButton radioButton;
    private JComboBox <String> comboBox;
    private final Statistics stat;


    public TextBoxRadioStat(Statistics stat) {
//        label = new JLabel(StatisticsStringMap.map.get(stat));

        radioButton = new JRadioButton(StatisticsStringMap.map.get(stat));
        textField = new JTextField();
        comboBox = new JComboBox<String>();
        setMovingAvgComboBox();

        GridLayout layout = new GridLayout(1,3);
        setLayout(layout);

        add(radioButton);
        layout.setHgap(0);
        add(comboBox);
        layout.setHgap(25);
        add(textField);


        this.stat = stat;
        textField.setEditable(false);
        comboBox.setEnabled(false);

        radioButton.addActionListener(e -> {
            if (radioButton.isSelected()) {
                textField.setEditable(true);
                comboBox.setEnabled(true);
            } else if (!radioButton.isSelected()) {
                textField.setEditable(false);
                comboBox.setEnabled(false);
            }
        });

        setPreferredSize(getPreferredSize());
        validate();
    }

    public float[] getTextFieldValue() {
        String textValues = textField.getText();
        String[] textValuesParse = textValues.trim().split("[,:;]");
        float[] floatValuesParse = new float[textValuesParse.length];
        for (int i = 0; i < textValuesParse.length; i++) {
            floatValuesParse[i] = Float.parseFloat(textValuesParse[i]);
        }
        return floatValuesParse;
    }
    public String getMovingAvgType() {
       for (MovingAvgType type : MovingAvgComboBoxMap.MovingAvgComboBoxMap.keySet()) {
            if (MovingAvgComboBoxMap.MovingAvgComboBoxMap.get(type) == comboBox.getSelectedItem())
                return comboBox.getSelectedItem().toString();
        }
        return null;
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
        comboBox.addActionListener(l);

    }

    @Override
    public boolean hasInput() {
        if (radioButton.isSelected() && !textField.getText().isEmpty()){
            return radioButton.isSelected();
        }
        return false;
    }

    private void setMovingAvgComboBox() {
        for (String option : MovingAvgComboBoxMap.MovingAvgComboBoxMap.values())
            comboBox.addItem(option);

        comboBox.setSelectedItem(null);
    }


}