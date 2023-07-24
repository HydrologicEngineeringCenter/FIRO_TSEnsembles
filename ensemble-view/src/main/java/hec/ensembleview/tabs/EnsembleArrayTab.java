package hec.ensembleview.tabs;

import hec.ensembleview.controllers.ComputePanelController;
import hec.ensembleview.viewpanels.EnsembleDataTransformView;
import hec.ensembleview.viewpanels.StatEnsembleComputePanelView;
import hec.ensembleview.controllers.EnsembleArrayChartManager;
import hec.ensembleview.charts.EnsembleChartAcrossTime;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;

public class EnsembleArrayTab extends JPanel {
    private final ChartPanel chartPanel;
    private final JPanel statAndDataViewPanel = new JPanel();
    private transient ComputePanelController computePanelController;

    public EnsembleArrayTab() {
        this.chartPanel = createChart();
        initiateEnsembleChart();

        StatEnsembleComputePanelView statEnsembleComputePanelView = new StatEnsembleComputePanelView(computePanelController);
        EnsembleDataTransformView ensembleDataTransformView = new EnsembleDataTransformView(computePanelController);

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

    void initiateEnsembleChart() {
        computePanelController = new ComputePanelController();
        new EnsembleArrayChartManager(computePanelController.getStatisticsMap(), chartPanel);
    }
}
