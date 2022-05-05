package hec.ensembleview;

import javax.swing.*;

public class TabSpec {
    public String tabName;
    public JPanel chartPanel;
    public ComponentsPanel statPanel;
    public ChartType chartType;

    public TabSpec(String tabName, JPanel chartPanel, ComponentsPanel statPanel, ChartType chartType) {
        this.tabName = tabName;
        this.chartPanel = chartPanel;
        this.statPanel = statPanel;
        this.chartType = chartType;
    }
}