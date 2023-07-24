package hec.ensembleview.tabs;

import hec.ensembleview.controllers.ComputePanelController;
import hec.ensembleview.viewpanels.TimeSeriesDataTransformView;
import hec.ensembleview.viewpanels.StatTimeSeriesComputePanelView;
import hec.ensembleview.charts.EnsembleChartAcrossTime;
import hec.ensembleview.controllers.EnsembleTimeSeriesChartManager;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;

public class TimeSeriesTab extends JPanel {
    private final ChartPanel ensembleChart;
    private final JPanel statAndDataViewPanel = new JPanel();
    private transient ComputePanelController computePanelController;

    public TimeSeriesTab() {
        this.ensembleChart = createEnsembleChart();
        initiateChartManager();

        StatTimeSeriesComputePanelView statTimeSeriesComputePanelView = new StatTimeSeriesComputePanelView(computePanelController);
        TimeSeriesDataTransformView timeSeriesDataTransformView = new TimeSeriesDataTransformView(computePanelController);

        setupStatisticsAndDataViewPanel(statTimeSeriesComputePanelView, timeSeriesDataTransformView);
        setLayout(new BorderLayout());
        add(ensembleChart, BorderLayout.CENTER);
        add(statAndDataViewPanel, BorderLayout.NORTH);
    }

    private void setupStatisticsAndDataViewPanel(JPanel statsPanel, JPanel dataViewPanel) {
        statAndDataViewPanel.setLayout(new BorderLayout());
        statAndDataViewPanel.add(statsPanel, BorderLayout.NORTH);
        statAndDataViewPanel.add(dataViewPanel, BorderLayout.SOUTH);
    }

    public ChartPanel createEnsembleChart() {
        return new EnsembleChartAcrossTime().generateChart();
    }

    void initiateChartManager() { //The Chart Manager is a controller for the chart.
        computePanelController = new ComputePanelController();
        new EnsembleTimeSeriesChartManager(computePanelController.getStatisticsMap(), ensembleChart);
    }
}
