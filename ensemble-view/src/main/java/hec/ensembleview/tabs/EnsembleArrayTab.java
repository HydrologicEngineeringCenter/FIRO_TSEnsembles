package hec.ensembleview.tabs;

import hec.ensembleview.ComputePanelView;
import hec.ensembleview.DataTransformView;
import hec.ensembleview.charts.EnsembleChartAcrossTime;
import hec.ensembleview.controllers.ComputePanelController;
import hec.ensembleview.controllers.EnsembleArrayChartManager;
import hec.ensembleview.viewpanels.EnsembleDataTransformView;
import hec.ensembleview.viewpanels.StatEnsembleComputePanelView;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;

public class EnsembleArrayTab extends JPanel {
    private final ChartPanel chartPanel;
    private final JPanel statAndDataViewPanel = new JPanel();

    public EnsembleArrayTab() {
        this.chartPanel = createChart();
        ComputePanelView statEnsembleComputePanelView = new StatEnsembleComputePanelView();
        DataTransformView ensembleDataTransformView = new EnsembleDataTransformView();

        initiateEnsembleChart(ensembleDataTransformView, statEnsembleComputePanelView);
        setupStatisticsAndDataViewPanel(statEnsembleComputePanelView, ensembleDataTransformView);

        setLayout(new BorderLayout());
        add(statAndDataViewPanel, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);
    }

    private void setupStatisticsAndDataViewPanel(JPanel statsPanel, JPanel dataViewPanel) {
        statAndDataViewPanel.setLayout(new BorderLayout());
        statAndDataViewPanel.add(statsPanel, BorderLayout.NORTH);
        statAndDataViewPanel.add(dataViewPanel, BorderLayout.SOUTH);
    }

    public ChartPanel createChart() {
        return new EnsembleChartAcrossTime().generateChart();
    }

    void initiateEnsembleChart(DataTransformView dataTransformView, ComputePanelView computePanelView) {
        ComputePanelController computePanelController = new ComputePanelController(dataTransformView, computePanelView);
        new EnsembleArrayChartManager(computePanelController.getStatisticsMap(), chartPanel);
    }
}
