package hec.ensembleview.tabs;

import hec.ensembleview.charts.EnsembleChartAcrossTime;
import hec.ensembleview.controllers.ComputePanelController;
import hec.ensembleview.controllers.EnsembleTimeSeriesChartManager;
import hec.ensembleview.viewpanels.ComputePanelView;
import hec.ensembleview.viewpanels.DataTransformView;
import hec.ensembleview.viewpanels.StatTimeSeriesComputePanelView;
import hec.ensembleview.viewpanels.TimeSeriesDataTransformView;
import hec.gfx2d.G2dPanel;

import javax.swing.*;
import java.awt.*;

public class TimeSeriesTab extends JPanel {
    private final G2dPanel ensembleChart;
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

    private G2dPanel createEnsembleChart() {
        return new EnsembleChartAcrossTime().generateChart();
    }

    private void initiateChartManager(DataTransformView dataTransformView, ComputePanelView computePanelView) { //The Chart Manager is a controller for the chart.
        ComputePanelController computePanelController = new ComputePanelController(dataTransformView, computePanelView);
        new EnsembleTimeSeriesChartManager(computePanelController.getStatisticsMap(), ensembleChart);
    }
}
