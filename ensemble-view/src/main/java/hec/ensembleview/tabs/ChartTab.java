package hec.ensembleview.tabs;

import hec.ensembleview.ChartType;
import hec.ensembleview.ComponentsPanel;
import javafx.scene.chart.Chart;

import javax.swing.*;
import java.awt.*;

public class ChartTab extends JPanel {
    public JPanel chartPanel;
    public ComponentsPanel componentsPanel;
    public ChartType chartType;

    public ChartTab(JPanel chartPanel, ComponentsPanel componentsPanel, ChartType chartType) {
        this.chartPanel = chartPanel;
        this.componentsPanel = componentsPanel;
        this.chartType = chartType;

        setLayout(new BorderLayout());
        add(componentsPanel.getPanel(), BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);
    }
}
