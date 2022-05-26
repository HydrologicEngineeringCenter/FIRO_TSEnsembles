package hec.ensembleview.tabs;

import hec.ensembleview.ChartType;
import hec.ensembleview.SingleValueSummaryType;
import hec.ensembleview.StatisticUIType;
import hec.ensembleview.mappings.SingleValueComboBoxMap;
import hec.ensembleview.mappings.StatisticsUITypeMap;
import hec.stats.Statistics;
//import javafx.scene.chart.Chart;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class SingleValueSummaryTab extends JPanel {
    JPanel leftPanel;
    JPanel rightPanel;

    JComboBox<String> summaryTypeComboBox;
    JComboBox<Statistics> statComboBox1;
    JComboBox<Statistics> statComboBox2;
    JTextField textField1;
    JTextField textField2;

    JPanel buttonPanel;
    public JButton computeButton;
    JButton cleanButton;

    JTextArea outputArea;

    public SingleValueSummaryTab() {
        initializeUI();
        organizeUI();
        setSummaryTypeComboBox();
        setActionListeners();
    }

    public Statistics getFirstStat()
    {
        return (Statistics)statComboBox1.getSelectedItem();
    }

    public String getFirstStatString() {
        String r;
        Statistics stat = getFirstStat();
        float[] vals = getFirstTextFieldValue();
        if (StatisticsUITypeMap.map.get(stat) == StatisticUIType.TEXTBOX) {
            if (stat == Statistics.PERCENTILE) {
                r = String.format("%.2f%% %s", vals[0] * 100, stat);
            } else if (stat == Statistics.MAXAVERAGEDURATION || stat == Statistics.MAXACCUMDURATION) {
                r = String.format("%d hour %s", (int)vals[0], stat);
            }
            else {
                r = stat.toString();
            }
        } else {
            if (stat == Statistics.CUMULATIVE) {
                r = String.format("%s value on day %d", stat, (int)vals[0]);
            } else
                r = stat.toString();
        }

        return r;
    }

    public String getSecondStatString() {
        String r;
        Statistics stat = getSecondStat();
        float[] vals = getSecondTextFieldValue();
        if (StatisticsUITypeMap.map.get(stat) == StatisticUIType.TEXTBOX) {
            if (stat == Statistics.PERCENTILE) {
                r = String.format("%.2f%% %s", vals[0] * 100, stat);
            } else if (stat == Statistics.MAXAVERAGEDURATION || stat == Statistics.MAXACCUMDURATION) {
                r = String.format("%d hour %s", (int)vals[0], stat);
            }
            else {
                r = stat.toString();
            }
        } else {
            r = stat.toString();
        }

        return r;
    }

    public Statistics getSecondStat()
    {
        return (Statistics)statComboBox2.getSelectedItem();
    }

    public SingleValueSummaryType getSummaryType() {
        for (SingleValueSummaryType type : SingleValueComboBoxMap.summaryComboBoxMap.keySet()) {
            if (SingleValueComboBoxMap.summaryComboBoxMap.get(type) == summaryTypeComboBox.getSelectedItem())
                return type;
        }
        return null;
    }

    public float[] getFirstTextFieldValue() {
        if (StatisticsUITypeMap.map.get((Statistics)statComboBox1.getSelectedItem()) == StatisticUIType.CHECKBOX)
            return null;

        String textValues = textField1.getText();

        if (Objects.equals(textValues, ""))
            return null;

        String[] textValuesParse = textValues.trim().split("[,:;]");
        float[] floatValuesParse = new float[textValuesParse.length];
        for(int i = 0; i < textValuesParse.length; i++) {
            floatValuesParse[i] = Float.parseFloat(textValuesParse[i]);
        }
        return floatValuesParse;
    }

    public float[] getSecondTextFieldValue() {
        if (StatisticsUITypeMap.map.get((Statistics)statComboBox2.getSelectedItem()) != StatisticUIType.TEXTBOX)
            return null;

        String textValues = textField2.getText();

        if (Objects.equals(textValues, ""))
            return null;

        String[] textValuesParse = textValues.trim().split("[,:;]");
        float[] floatValuesParse = new float[textValuesParse.length];
        for(int i = 0; i < textValuesParse.length; i++) {
            floatValuesParse[i] = Float.parseFloat(textValuesParse[i]);
        }
        return floatValuesParse;
    }

    public void writeLn(String output) {
        outputArea.append("\n" + output);
    }


    private void setActionListeners() {
        summaryTypeComboBox.addActionListener(e -> {
            String s = (String)summaryTypeComboBox.getSelectedItem();
            SingleValueSummaryType type = null;
            for (SingleValueSummaryType t : SingleValueComboBoxMap.summaryComboBoxMap.keySet()) {
                if (Objects.equals(SingleValueComboBoxMap.summaryComboBoxMap.get(t), s)) {
                    type = t;
                    break;
                }
            }
            setupStatComboBoxes(type);

        });

        statComboBox1.addActionListener(e ->
                textField1.setEditable(StatisticsUITypeMap.map.get((Statistics) (statComboBox1.getSelectedItem())) != StatisticUIType.CHECKBOX));

        statComboBox2.addActionListener(e ->
                textField2.setEditable(StatisticsUITypeMap.map.get((Statistics) (statComboBox2.getSelectedItem())) != StatisticUIType.CHECKBOX));

        cleanButton.addActionListener(e -> {
            outputArea.setText("");
        });
    }

    private void organizeUI() {
        setLayout(new GridLayout(1,2));

        add(leftPanel);
        add(rightPanel);

        GroupLayout layout = new GroupLayout(leftPanel);
        leftPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup()
                        .addComponent(summaryTypeComboBox)
                        .addComponent(statComboBox1)
                        .addComponent(statComboBox2))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(textField1)
                                .addComponent(textField2)
                                .addComponent(computeButton))
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(summaryTypeComboBox)).addContainerGap().addGap(50)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(statComboBox1)
                                .addComponent(textField1)).addContainerGap().addGap(50)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(statComboBox2)
                                .addComponent(textField2))
                        .addComponent(computeButton)
        );
        BorderLayout rightArea = new BorderLayout(10,10);

        rightPanel.setLayout(rightArea);

        rightPanel.add(outputArea, BorderLayout.CENTER);
        rightPanel.add(cleanButton, BorderLayout.SOUTH);

    }

    private void setSummaryTypeComboBox() {
        for (String option : SingleValueComboBoxMap.summaryComboBoxMap.values())
            summaryTypeComboBox.addItem(option);

        summaryTypeComboBox.setSelectedItem(null);
    }

    private void setupStatComboBoxes(SingleValueSummaryType option) {
            setupStatComboBox1(option);
            setupStatComboBox2(option);
    }

    private void setupStatComboBox1(SingleValueSummaryType option) {
        statComboBox1.removeAllItems();
        for (Statistics stat : SingleValueComboBoxMap.summaryStatisticsMap.get(option).get(0)) {
            statComboBox1.addItem(stat);
        }
    }

    private void setupStatComboBox2(SingleValueSummaryType option) {
        statComboBox2.removeAllItems();
        for (Statistics stat : SingleValueComboBoxMap.summaryStatisticsMap.get(option).get(1)) {
            statComboBox2.addItem(stat);
        }
    }

    private void initializeUI() {
        leftPanel = new JPanel();
        rightPanel = new JPanel();

        summaryTypeComboBox = new JComboBox<>();
        statComboBox1 = new JComboBox<>();
        statComboBox2 = new JComboBox<>();

        textField1 = new JTextField();
        textField2 = new JTextField();

        buttonPanel = new JPanel();
        computeButton = new JButton("Compute");
        cleanButton = new JButton("Clean");

        outputArea = new JTextArea();
        outputArea.setLineWrap(true);

    }

    public void tryShowingOutput(float result) {
        if (getSummaryType() == SingleValueSummaryType.ComputeAcrossEnsembles) {
            writeLn(String.join(" ", "Computing",
                    getFirstStatString() + "across all ensemble members for each time-step,",
                    "then computing" + getSecondStatString() + "across all time-steps",
                    "=", Float.toString(result)));
        } else if (getSummaryType() == SingleValueSummaryType.ComputeAcrossTime) {
            writeLn(String.join(" ", "Computing",
                    getFirstStatString() + "for each ensemble across all time-steps,",
                    "then computing" + getSecondStatString() + "across all ensemble members",
                    "=", Float.toString(result)));
        } else if (getSummaryType() == SingleValueSummaryType.ComputeCumulative) {
            writeLn(String.join(" ", "Computing", getFirstStatString() + ",",
                    "then computing", getSecondStatString(), "across all ensemble members =", Float.toString(result)));
        }
    }
}
