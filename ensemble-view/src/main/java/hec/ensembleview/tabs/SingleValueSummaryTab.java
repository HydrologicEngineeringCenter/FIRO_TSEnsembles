package hec.ensembleview.tabs;

import hec.ensembleview.ChartType;
import hec.ensembleview.mappings.ChartTypeStatisticsMap;
import hec.stats.Statistics;
import javafx.scene.chart.Chart;

import javax.swing.*;
import java.awt.*;

public class SingleValueSummaryTab extends JPanel {
    JPanel leftPanel;
    JPanel rightPanel;

    JComboBox<ChartType> chartTypeComboBox;
    JComboBox<Statistics> statComboBox1;
    JComboBox<Statistics> statComboBox2;
    JTextField textField1;
    JTextField textField2;

    JPanel buttonPanel;
    JButton computeButton;
    JButton cleanButton;

    JTextArea outputArea;

    public SingleValueSummaryTab() {
        initializeUI();
        organizeUI();
        setChartTypeComboBox();
    }

    private void organizeUI() {
        setLayout(new GridLayout(1, 2));

        add(leftPanel);
        add(rightPanel);

        leftPanel.setLayout(new GridLayout(0, 1));
        rightPanel.setLayout(new BorderLayout());
        buttonPanel.setLayout(new BorderLayout());

        buttonPanel.add(computeButton, BorderLayout.WEST);
        buttonPanel.add(cleanButton, BorderLayout.EAST);

        JPanel chartTypeComboBoxPanel = new JPanel();
        chartTypeComboBoxPanel.add(chartTypeComboBox);
        leftPanel.add(chartTypeComboBoxPanel);

        JPanel statComboBox1Panel = new JPanel();
        statComboBox1Panel.setLayout(new GridLayout(1, 2));
        statComboBox1Panel.add(statComboBox1);
        statComboBox1Panel.add(textField1);
        leftPanel.add(statComboBox1Panel);

        JPanel statComboBox2Panel = new JPanel();
        statComboBox2Panel.setLayout(new GridLayout(1, 2));
        statComboBox2Panel.add(statComboBox2);
        statComboBox2Panel.add(textField2);
        leftPanel.add(statComboBox2Panel);

        leftPanel.add(buttonPanel);

        rightPanel.add(outputArea);
    }

    private void setChartTypeComboBox() {
        for (ChartType type : ChartType.values())
            chartTypeComboBox.addItem(type);

        chartTypeComboBox.addActionListener(e -> setupStatComboBoxes((ChartType)chartTypeComboBox.getSelectedItem()));
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
