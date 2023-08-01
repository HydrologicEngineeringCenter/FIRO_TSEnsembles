package hec.ensembleview.tabs;

import hec.ensembleview.viewpanels.ComputePanelView;
import hec.ensembleview.viewpanels.DataTransformView;
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

    public TimeSeriesTab() {
        this.ensembleChart = createEnsembleChart();

        ComputePanelView statTimeSeriesComputePanelView = new StatTimeSeriesComputePanelView();
        DataTransformView timeSeriesDataTransformView = new TimeSeriesDataTransformView();

        initiateChartManager(timeSeriesDataTransformView, statTimeSeriesComputePanelView);
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

    private ChartPanel createEnsembleChart() {
        return new EnsembleChartAcrossTime().generateChart();
    }

    private void initiateChartManager(DataTransformView dataTransformView, ComputePanelView computePanelView) { //The Chart Manager is a controller for the chart.
        ComputePanelController computePanelController = new ComputePanelController(dataTransformView, computePanelView);
        new EnsembleTimeSeriesChartManager(computePanelController.getStatisticsMap(), ensembleChart);
    }
}
