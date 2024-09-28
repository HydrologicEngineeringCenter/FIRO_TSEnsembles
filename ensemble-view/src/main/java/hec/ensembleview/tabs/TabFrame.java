package hec.ensembleview.tabs;

import hec.ensembleview.DefaultSettings;
import hec.ensembleview.charts.EnsembleChartAcrossEnsembles;
import hec.ensembleview.charts.EnsembleChartAcrossTime;
import hec.ensembleview.controllers.ComputePanelController;
import hec.ensembleview.controllers.EnsembleArrayChartManager;
import hec.ensembleview.controllers.EnsembleTimeSeriesChartManager;
import hec.ensembleview.viewpanels.*;
import hec.ensembleview.viewpanels.MenuBar;
import hec.gfx2d.G2dPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TabFrame {
    private final List<TabSpec> tabs = new ArrayList<>();
    private JTabbedPane tabPane;
    private final MenuBar frame;

    public TabFrame(MenuBar frame) {
        this.frame = frame;

        createTimeSeriesTab();
        createScatterPlotTab();
        createSingleValueSummaryTab();
        createTabs();
    }

    private void createTimeSeriesTab() {
        G2dPanel timeChart = new EnsembleChartAcrossTime().generateChart();
        JPanel timeSeriesPanel = new JPanel();
        timeSeriesPanel.setLayout(new BorderLayout());

        TabSpec tabSpec = new TabSpec.TabSpecBuilder()
                .addTabName("Time Series Plot")
                .addPanel(timeSeriesPanel)
                .addStatsPanel(new StatTimeSeriesComputePanelView(), new TimeSeriesDataTransformView())
                .addG2dPanel(timeChart)
                .build();

        tabs.add(tabSpec);

        ComputePanelController timeSeriesController = new ComputePanelController(tabSpec, frame);
        new EnsembleTimeSeriesChartManager(timeChart, timeSeriesController);
    }

    private void createScatterPlotTab() {
        G2dPanel ensembleChart = new EnsembleChartAcrossEnsembles().generateChart();
        JPanel ensembleArrayPanel = new JPanel();
        ensembleArrayPanel.setLayout(new BorderLayout());

        TabSpec tabSpec = new TabSpec.TabSpecBuilder()
                .addTabName("Scatter Plot")
                .addPanel(ensembleArrayPanel)
                .addStatsPanel(new StatEnsembleComputePanelView(), new EnsembleDataTransformView())
                .addG2dPanel(ensembleChart)
                .build();

        tabs.add(tabSpec);

        ComputePanelController ensembleArrayController = new ComputePanelController(tabSpec, frame);
        new EnsembleArrayChartManager(ensembleChart, ensembleArrayController);
    }

    private void createSingleValueSummaryTab() {
        TabSpec tabSpec = new TabSpec.TabSpecBuilder()
                .addTabName("Single Value Summary")
                .addPanel(new SingleValueSummaryTab())
                .build();

        tabs.add(tabSpec);
    }

    private void createTabs() { // create three types of charts in viewer
        tabPane = new JTabbedPane();
        for(TabSpec tab: tabs) {
            tabPane.addTab(tab.getTabName(), tab.getPanel());
        }
        tabPane.setFont(DefaultSettings.setSegoeFontText());
    }

    public JTabbedPane getTabPane() {
        return tabPane;
    }

}
