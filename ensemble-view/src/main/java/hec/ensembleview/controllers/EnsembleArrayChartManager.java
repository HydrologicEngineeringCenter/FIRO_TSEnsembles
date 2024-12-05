package hec.ensembleview.controllers;

import hec.ensemble.stats.Statistics;
import hec.ensembleview.DatabaseHandlerService;
import hec.ensembleview.MetricCollectionTimeSeriesContainer;
import hec.ensembleview.PlotStatisticsForChartType;
import hec.ensembleview.charts.EnsembleChart;
import hec.ensembleview.charts.EnsembleChartAcrossEnsembles;
import hec.ensembleview.mappings.StatisticsMap;
import hec.gfx2d.G2dPanel;
import hec.metrics.MetricCollectionTimeSeries;
import hec.metrics.MetricTypes;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class EnsembleArrayChartManager extends ChartManager {
    private static final Logger logger = Logger.getLogger(EnsembleArrayChartManager.class.getName());
    private boolean isProbability = false;
    private MetricCollectionTimeSeries residentMetricCollectionTimeSeries;
    private String secondaryUnits;

    public EnsembleArrayChartManager(G2dPanel chartPanel, ComputePanelController controller) {
        super(chartPanel, controller.getStatisticsMap());
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
        List<MetricCollectionTimeSeriesContainer> containers = databaseHandlerService.getMetricCollectionTimeSeriesContainer();

        for (MetricCollectionTimeSeriesContainer container : containers) {
            residentMetricCollectionTimeSeries = container.getMetricCollectionTimeSeries();
            chartLabelsForStatistic(container.getStatistics());
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
        List<MetricCollectionTimeSeriesContainer> containers = databaseHandlerService.getMetricCollectionTimeSeriesContainer();
        Map<String, Map<Float, Float>> probList = databaseHandlerService.getEnsembleProbabilityList();

        // Create a map for quick lookup of containers by statistics
        Map<Statistics, MetricCollectionTimeSeriesContainer> containerMap = containers.stream()
                .filter(container -> container.getMetricTypes().equals(MetricTypes.ARRAY_OF_ARRAY))
                .collect(Collectors.toMap(
                        MetricCollectionTimeSeriesContainer::getStatistics,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        for (Map.Entry<String, Map<Float, Float>> entry : probList.entrySet()) {
            Statistics statName = Statistics.getStatName(entry.getKey());
            MetricCollectionTimeSeriesContainer container = containerMap.get(statName);

            if (container != null) {
                residentMetricCollectionTimeSeries = container.getMetricCollectionTimeSeries();
                chartLabelsForStatistic(statName);
                PlotStatisticsForChartType.addStatisticsToEnsemblePlot((EnsembleChartAcrossEnsembles) chart, entry.getKey(), entry.getValue());
            }
        }

        chartPanel.revalidate();
        chartPanel.repaint();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getSource() instanceof StatisticsMap && evt.getPropertyName().equalsIgnoreCase("ensemble")) {
            databaseHandlerService.refreshMetricCollectionEnsembleSeriesMap(statisticsMap);
            addEnsembleValues();
            databaseHandlerService.refreshEnsembleProbabilityList();
        } else if (evt.getSource() instanceof  StatisticsMap && evt.getPropertyName().equalsIgnoreCase("probability")) {
            isProbability = (boolean) evt.getNewValue();
        }

        else if(evt.getSource() instanceof DatabaseHandlerService && evt.getOldValue() != null) {
            databaseHandlerService.refreshEnsembleProbabilityList();
            this.databaseHandlerService = DatabaseHandlerService.getInstance();
            addEnsembleValues();
        }
    }
}
