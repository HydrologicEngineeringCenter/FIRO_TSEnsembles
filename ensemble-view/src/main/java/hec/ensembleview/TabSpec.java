package hec.ensembleview;

import javax.swing.*;

public class TabSpec {
    public String tabName;
    public JPanel chartPanel;
    public ChartType chartType;

    public TabSpec(String tabName, JPanel chartPanel, ChartType chartType) {
        this.tabName = tabName;
        this.chartPanel = chartPanel;
        this.chartType = chartType;
    }
}
