package hec.ensembleview.controllers;

import hec.ensemble.Ensemble;
import hec.ensembleview.DatabaseHandlerService;
import hec.ensembleview.charts.EnsembleChart;
import hec.ensembleview.charts.EnsembleDataTablePanel;
import hec.ensembleview.mappings.StatisticsMap;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.Map;

public abstract class ChartManager implements PropertyChangeListener {
    final StatisticsMap statisticsMap;
    final JPanel chartPanel;
    final EnsembleDataTablePanel tablePanel;
    DatabaseHandlerService databaseHandlerService;
    Ensemble ensemble;
    EnsembleChart chart;
    String units;

    protected ChartManager(StatisticsMap map, JPanel chartPanel, EnsembleDataTablePanel tablePanel) {
        databaseHandlerService = DatabaseHandlerService.getInstance();
        databaseHandlerService.addDatabaseChangeListener(this);

        this.statisticsMap = map;
        statisticsMap.addStatisticsMapChangeListener(this);

        this.chartPanel = chartPanel;
        if (!(chartPanel.getLayout() instanceof BorderLayout)) {
            chartPanel.setLayout(new BorderLayout());
        }
        this.tablePanel = tablePanel;
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
        chartPanel.add(ec.generateChart(), BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    abstract EnsembleChart createEnsembleChart() throws ParseException;

    abstract Map<String, float[]> getMetricValuesFromResidentMetricDatabase();

    abstract void updateTableData();

}
