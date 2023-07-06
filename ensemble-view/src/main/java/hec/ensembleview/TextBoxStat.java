package hec.ensembleview;

import hec.ensemble.stats.Statistics;
import hec.ensembleview.mappings.StatisticsStringMap;
import hec.ensemble.stats.Statistics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class TextBoxStat extends JPanel implements EnsembleViewStat {
    private JTextField textField;
    private JCheckBox checkBox;
    private final Statistics stat;


    public TextBoxStat(Statistics stat) {
//        label = new JLabel(StatisticsStringMap.map.get(stat));
        checkBox = new JCheckBox(StatisticsStringMap.map.get(stat));
        textField = new JTextField();
        textField.setFont(DefaultSettings.setSegoeFontText());
        checkBox.setFont(DefaultSettings.setSegoeFontText());
        this.stat = stat;

        setToolTipFont();
        setToolTipMessages();

        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        setPreferredSize(getPreferredSize());
        validate();
    }

        if(stat == Statistics.CUMULATIVE) {
            textField.setToolTipText("Enter value in days");
        } else if (stat == Statistics.PERCENTILE) {
            textField.setToolTipText("Enter percentile as decimal.  Multiple percentiles can be entered by adding commas");
        } else {
            textField.setToolTipText("Enter value in hours");
        }
    }

    private void setToolTipFont() {
        // Set the tooltip font
        Font tooltipFont = DefaultSettings.setSegoeFontText(); // Replace with your desired font
        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setReshowDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);
        UIManager.put("ToolTip.font", tooltipFont);

        // Set the tooltip text
        textField.setToolTipText("<html><font face='" + tooltipFont.getName() + "' size='" + tooltipFont.getSize() + "'>Enter value in days</font></html>");

        GridLayout gl = new GridLayout(1, 2);
        setLayout(gl);
        add(checkBox);
        add(textField);
        this.stat = stat;
        textField.setEditable(false);
        checkBox.addActionListener(e -> {
            if(checkBox.isSelected()) {
                textField.setEditable(true);
            } else if(!checkBox.isSelected()) {
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
        return StatisticUIType.TEXTBOX;
    }

    @Override
    public Statistics getStatType() {
        return stat;
    }

    @Override
    public void addActionListener(ActionListener l) {
        checkBox.addActionListener(l);
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
