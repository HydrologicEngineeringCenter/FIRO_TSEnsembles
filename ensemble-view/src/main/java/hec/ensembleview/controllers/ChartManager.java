package hec.ensembleview.controllers;

import hec.ensemble.Ensemble;
import hec.ensembleview.DatabaseHandlerService;
import hec.ensembleview.charts.EnsembleChart;
import hec.ensembleview.mappings.StatisticsMap;
import hec.gfx2d.G2dPanel;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.Map;

public abstract class ChartManager implements PropertyChangeListener {
    final StatisticsMap statisticsMap;
    final G2dPanel chartPanel;
    DatabaseHandlerService databaseHandlerService;
    Ensemble ensemble;
    EnsembleChart chart;
    String units;

    protected ChartManager(StatisticsMap map, G2dPanel chartPanel) {
        databaseHandlerService = DatabaseHandlerService.getInstance();
        databaseHandlerService.addDatabaseChangeListener(this);

        this.statisticsMap = map;
        statisticsMap.addStatisticsMapChangeListener(this);

        this.chartPanel = chartPanel;
    }

    public void addEnsembleValues() {
        EnsembleChart ec;
        try {
            ec = createEnsembleChart();  // createChart for raw Ensembles
            if (ec == null)
                return;
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        chartPanel.removeAll();
        chartPanel.revalidate();
        chartPanel.repaint();

        chartPanel.setLayout(new BorderLayout());
        chartPanel.add(ec.generateChart(), BorderLayout.CENTER);
        chartPanel.repaint();
    }

    abstract EnsembleChart createEnsembleChart() throws ParseException;

    abstract Map<String, float[]> getMetricValuesFromResidentMetricDatabase();

}
