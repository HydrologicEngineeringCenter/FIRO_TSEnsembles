package hec.ensembleview.controllers;

import hec.ensemble.stats.Statistics;
import hec.ensembleview.DatabaseHandlerService;
import hec.ensembleview.PlotStatisticsForChartType;
import hec.ensembleview.charts.EnsembleChart;
import hec.ensembleview.charts.EnsembleChartAcrossEnsembles;
import hec.ensembleview.mappings.StatisticsMap;
import hec.gfx2d.G2dPanel;
import hec.metrics.MetricCollectionTimeSeries;
import hec.metrics.MetricTypes;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EnsembleArrayChartManager extends ChartManager {
    private static final Logger logger = Logger.getLogger(EnsembleArrayChartManager.class.getName());
    private boolean isProbability = false;
    private MetricCollectionTimeSeries residentMetricCollectionTimeSeries;
    private String secondaryUnits;

    public EnsembleArrayChartManager(StatisticsMap statisticsMap, G2dPanel chartPanel) {
        super(statisticsMap, chartPanel);
    }

    @Override
    EnsembleChart createEnsembleChart() {
        if (databaseHandlerService.getDbHandlerRid() == null || databaseHandlerService.getDbHandlerZdt() == null) {
            return null;
        }
        chart = new EnsembleChartAcrossEnsembles();

        if(isProbability) {
            plotProbabilityChart(chart);
        } else {
            plotResidentMetricChart(chart);
        }
        return chart;
    }
    @Override
    Map<String, float[]> getMetricValuesFromResidentMetricDatabase() {
        Map<String, float[]> metricValues = new HashMap<>();

        //get map of MetricCollectionTImeSeries data and create a list of stats in map
        Map<Statistics, MetricCollectionTimeSeries> metricCollectionTimeSeriesMap = databaseHandlerService.getMetricCollectionTimeSeriesMap();
        List<Statistics> list = new ArrayList<>(metricCollectionTimeSeriesMap.keySet());

        for (Statistics stat : list) {
            residentMetricCollectionTimeSeries = metricCollectionTimeSeriesMap.get(stat);
            chartLabelsForStatistic(stat);
            databaseHandlerService.setResidentMetricCollectionTimeSeries(residentMetricCollectionTimeSeries);
            if (isMetricArrayOfArray(residentMetricCollectionTimeSeries)) {
                String stats = databaseHandlerService.getResidentMetricStatisticsList();
                String[] statCollection = stats.split("\\|");
                float[][] vals = databaseHandlerService.getResidentMetricStatisticsValues();
                for (int i = 0; i < vals.length; i++) {
                    metricValues.put(statCollection[i], vals[i]);
                }
            }
        }
        return metricValues;
    }

    private void chartLabelsForStatistic(Statistics stat) {
        if(PlotStatisticsForChartType.getRangeAxis(stat) == 0) {
            units = residentMetricCollectionTimeSeries.getUnits();
        } else if(PlotStatisticsForChartType.getRangeAxis(stat) == 1) {
            secondaryUnits = residentMetricCollectionTimeSeries.getUnits();
        }
        setYAxisChartLabels();
    }

    private void setYAxisChartLabels() {
        String parameter = databaseHandlerService.getDbHandlerRid().parameter;

        String yLabelText = parameter + " (" + units + ")";
        chart.setYLabel(yLabelText);

        if (chart instanceof EnsembleChartAcrossEnsembles) {
            String y2LabelText = parameter + " (" + secondaryUnits + ")";
            ((EnsembleChartAcrossEnsembles) chart).setY2Label(y2LabelText);
        }
    }


    private boolean isMetricArrayOfArray(MetricCollectionTimeSeries metricCollections) {
        return metricCollections.getMetricType() == MetricTypes.ARRAY_OF_ARRAY;
    }

    private void plotResidentMetricChart(EnsembleChart chart) {
        for (Map.Entry<String, float[]> entry : getMetricValuesFromResidentMetricDatabase().entrySet()) {
            String stat = entry.getKey();
            float[] vals = entry.getValue();
            try {
                PlotStatisticsForChartType.addStatisticsToEnsemblePlot((EnsembleChartAcrossEnsembles) chart, stat, vals);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error adding metric statistics to time plot");
                e.printStackTrace();
            }
        }
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void plotProbabilityChart(EnsembleChart chart) {
        Map<Statistics, MetricCollectionTimeSeries> metricCollectionTimeSeriesMap = databaseHandlerService.getMetricCollectionTimeSeriesMap();

        Map<String, Map<Float, Float>> probList = databaseHandlerService.getEnsembleProbabilityList();
        for(Map.Entry<String, Map<Float, Float>> entry : probList.entrySet()) {
            residentMetricCollectionTimeSeries = metricCollectionTimeSeriesMap.get(Statistics.getStatName(entry.getKey()));
            chartLabelsForStatistic(Statistics.getStatName(entry.getKey()));
            PlotStatisticsForChartType.addStatisticsToEnsemblePlot((EnsembleChartAcrossEnsembles) chart, entry.getKey(), entry.getValue());
        }
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getSource() instanceof StatisticsMap && evt.getPropertyName().equalsIgnoreCase("ensemble")) {
            addEnsembleValues();
            databaseHandlerService.refreshMetricCollectionTimeSeriesMap();
            databaseHandlerService.refreshEnsembleProbabilityList();
        } else if (evt.getSource() instanceof  StatisticsMap && evt.getPropertyName().equalsIgnoreCase("probability")) {
            isProbability = (boolean) evt.getNewValue();
        }

        else if(evt.getSource() instanceof DatabaseHandlerService) {
            databaseHandlerService.refreshMetricCollectionTimeSeriesMap();
            databaseHandlerService.refreshEnsembleProbabilityList();
            this.databaseHandlerService = DatabaseHandlerService.getInstance();
            addEnsembleValues();
        }
    }
}
