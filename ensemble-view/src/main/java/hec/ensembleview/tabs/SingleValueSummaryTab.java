package hec.ensembleview.tabs;

import hec.ensembleview.ChartType;
import hec.ensembleview.ComputeEngine;
import hec.ensembleview.StatisticUIType;
import hec.ensembleview.mappings.ChartTypeStatisticsMap;
import hec.ensembleview.mappings.ChartTypeStringMap;
import hec.ensembleview.mappings.StatisticsUITypeMap;
import hec.stats.Statistics;
//import javafx.scene.chart.Chart;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class SingleValueSummaryTab extends JPanel {
    JPanel leftPanel;
    JPanel rightPanel;

    JComboBox<String> chartTypeComboBox;
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
        setChartTypeComboBox();
        setActionListeners();
    }

    public Statistics getFirstStat()
    {
        return (Statistics)statComboBox1.getSelectedItem();
    }

    public Statistics getSecondStat()
    {
        return (Statistics)statComboBox2.getSelectedItem();
    }

    public ChartType getChartType() {
        return ChartTypeStringMap.map.get((String)chartTypeComboBox.getSelectedItem());
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
                        .addComponent(chartTypeComboBox)
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
                        .addComponent(chartTypeComboBox)).addContainerGap().addGap(50)
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

    private void setChartTypeComboBox() {
        for (String option : ChartTypeStringMap.map.keySet())
            chartTypeComboBox.addItem(option);

        chartTypeComboBox.addActionListener(e ->
                setupStatComboBoxes(ChartTypeStringMap.map.get((String)chartTypeComboBox.getSelectedItem())));
    }

    private void setupStatComboBoxes(ChartType selectedItem) {
        if (selectedItem == ChartType.TimePlot){
            setupStatComboBox1(ChartType.TimePlot);
            setupStatComboBox2(ChartType.ScatterPlot);
        } else {
            setupStatComboBox1(ChartType.ScatterPlot);
            setupStatComboBox2(ChartType.TimePlot);
        }
    }

    private void setupStatComboBox1(ChartType type) {
        statComboBox1.removeAllItems();
        for (Statistics stat : ChartTypeStatisticsMap.map.get(type)) {
            statComboBox1.addItem(stat);
        }
    }

    private void setupStatComboBox2(ChartType type) {
        statComboBox2.removeAllItems();
        for (Statistics stat : ChartTypeStatisticsMap.map.get(type)) {
            statComboBox2.addItem(stat);
        }
    }

    private void initializeUI() {
        leftPanel = new JPanel();
        rightPanel = new JPanel();

        chartTypeComboBox = new JComboBox<>();
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
}
