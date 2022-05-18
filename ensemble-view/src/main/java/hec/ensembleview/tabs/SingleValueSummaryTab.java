package hec.ensembleview.tabs;

import hec.ensembleview.ChartType;
import hec.ensembleview.mappings.ChartTypeStatisticsMap;
import hec.stats.Statistics;
//import javafx.scene.chart.Chart;

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
        setLayout(new GridLayout(1,2));

        add(leftPanel);
        add(rightPanel);

        GroupLayout layout = new GroupLayout(leftPanel);
        leftPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        /*layout.linkSize(SwingConstants.HORIZONTAL, cleanButton, computeButton);*/

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup()
                        .addComponent(chartTypeComboBox)
                        .addComponent(statComboBox1)
                        .addComponent(statComboBox2))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(textField1)
                                .addComponent(textField2)
                                .addComponent(computeButton)
                                /*.addComponent(cleanButton)*/)
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
                        /*.addComponent(cleanButton)*/
        );
        BorderLayout rightArea = new BorderLayout(10,10);

        rightPanel.setLayout(rightArea);

        rightPanel.add(outputArea, BorderLayout.CENTER);
        rightPanel.add(cleanButton, BorderLayout.SOUTH);




        /*buttonPanel.setLayout(new BorderLayout());

        buttonPanel.add(computeButton, BorderLayout.WEST);
        buttonPanel.add(cleanButton, BorderLayout.EAST);*/

/*        JPanel chartTypeComboBoxPanel = new JPanel();
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

        leftPanel.add(buttonPanel);*/

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
