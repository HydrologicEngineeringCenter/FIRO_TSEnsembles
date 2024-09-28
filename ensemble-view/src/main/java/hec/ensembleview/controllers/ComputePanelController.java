package hec.ensembleview.controllers;

import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.stats.Statistics;
import hec.ensembleview.DatabaseHandlerService;
import hec.ensembleview.MetricCollectionTimeSeriesContainer;
import hec.ensembleview.StatComputationHelper;
import hec.ensembleview.charts.ChartType;
import hec.ensembleview.mappings.StatisticsMap;
import hec.ensembleview.tabs.TabSpec;
import hec.ensembleview.viewpanels.ComputePanelView;
import hec.ensembleview.viewpanels.DataTransformView;
import hec.ensembleview.viewpanels.MenuBar;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is a controller class and handles events from the ComputePanel and DataView Panel.
 */

public class ComputePanelController implements PropertyChangeListener{
    private final DatabaseHandlerService databaseHandlerService;
    private float[] floatPercentileValuesParse;
    private float[] floatCumulativeValuesParse;
    private boolean isCumulative = false;
    private boolean isProbability = false;
    private final StatisticsMap statisticsMap = new StatisticsMap();
    private final StatComputationHelper statComputationHelper = new StatComputationHelper();

    public ComputePanelController(TabSpec tabSpec, MenuBar frame) {
        this.databaseHandlerService = DatabaseHandlerService.getInstance();
        databaseHandlerService.addDatabaseChangeListener(this);
        ComputePanelView computePanelView = tabSpec.getComputePanelView();
        DataTransformView dataTransformView = tabSpec.getDataTransformView();

        initiateComputePanelListener(computePanelView);
        initiateDataView(dataTransformView);
        initiateMenuListener(frame);
    }

    private void initiateMenuListener(MenuBar frame) {
        frame.initializeActionListener();
        List<MetricCollectionTimeSeriesContainer> savedMetrics = databaseHandlerService.getMetricCollectionTimeSeriesContainer();

        frame.setFileMenuListener(() -> {
            if (savedMetrics.isEmpty()) {
                JOptionPane.showMessageDialog(frame.getMenuBar(), "No metrics selected for saving", "Selection Required", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                List<String> list = new ArrayList<>();
                for(MetricCollectionTimeSeriesContainer container : savedMetrics) {
                    databaseHandlerService.saveMctsToDatabase(container.getMetricCollectionTimeSeries());
                    list.add(container.getMetricCollectionTimeSeries().getMetricCollection(databaseHandlerService.getDbHandlerZdt()).getMetricStatistics());
                }
                JOptionPane.showMessageDialog(frame.getMenuBar(), list + " saved to database!", "Save Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame.getMenuBar(), e.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }

    private void initiateDataView(DataTransformView dataTransformView) {
        (dataTransformView).setListener(new DataViewListener() {
            @Override
            public void setIsDataViewProbability(Boolean prob) {
                isProbability = prob;
                statisticsMap.setDataView(isProbability, ChartType.SCATTERPLOT);
            }

            @Override
            public void setIsDataViewCumulative(boolean cumulative) {
                isCumulative = cumulative;
                timeSeriesType(isCumulative);
                statisticsMap.setDataView(isCumulative, ChartType.TIMEPLOT);
            }

            @Override
            public void initiateCompute(ChartType chartType) {
                handleDataViewCompute(chartType);
            }
        });
    }

    private void handleDataViewCompute(ChartType chartType) {
        if (chartType.equals(ChartType.TIMEPLOT)) {
            handleTimePlotComputation();
        } else if (chartType.equals(ChartType.SCATTERPLOT)) {
            handleScatterPlotComputation();
        }
    }

    private void initiateComputePanelListener(ComputePanelView computePanelView) {
        (computePanelView).setListener(new ComputePanelListener() {
            @Override
            public void initiateCompute() {
                handleCompute(computePanelView);
            }

            @Override
            public void setCheckedStatistics(String name, ChartType type) {
                updateStatisticsMap(type, name, true);
            }

            @Override
            public void setRemovedStatistics(String name, ChartType type) {
                updateStatisticsMap(type, name, false);
            }

            @Override
            public void getTextFieldValues(JTextField textField, ChartType type) {
                parseTextFieldValues(textField, type);
            }
        });
    }

    //InitiateCompute logic
    private void handleCompute(ComputePanelView view) {
        ChartType chartType = view.getChartType();
        if (chartType.equals(ChartType.TIMEPLOT)) {
            handleTimePlotComputation();
        } else if (chartType.equals(ChartType.SCATTERPLOT)) {
            handleScatterPlotComputation();
        }
    }

    private void handleTimePlotComputation() {
        EnsembleTimeSeries ensembleTimeSeries = isCumulative
                ? databaseHandlerService.getCumulativeEnsembleTimeSeries()
                : databaseHandlerService.getEnsembleTimeSeries();

        statisticsMap.getTimeSeriesMapList().forEach((stat, isSelected) -> {
            if (Boolean.TRUE.equals(isSelected)) {
                computeMetric(ensembleTimeSeries, stat, ChartType.TIMEPLOT);
            }
        });

        statisticsMap.setTimeStatisticsMap();
    }

    private void handleScatterPlotComputation() {
        EnsembleTimeSeries ensembleTimeSeries = databaseHandlerService.getEnsembleTimeSeries();

        statisticsMap.getEnsembleSeriesMapList().forEach((stat, isSelected) -> {
            if (Boolean.TRUE.equals(isSelected)) {
                if (isProbability) {
                    computeProbability(ensembleTimeSeries, stat);
                } else {
                    computeMetric(ensembleTimeSeries, stat, ChartType.SCATTERPLOT);
                }
            }
        });

        statisticsMap.setEnsembleStatisticsMap();
    }

    private void updateStatisticsMap(ChartType type, String name, boolean isCheckMarkSelected) {
        Map<String, Boolean> map = new HashMap<>();
        map.put(name, isCheckMarkSelected);

        if (type.equals(ChartType.TIMEPLOT)) {
            statisticsMap.storeTimeStatisticList(map);
        } else if (type.equals(ChartType.SCATTERPLOT)) {
            statisticsMap.storeEnsembleStatisticList(map);
        }
    }

    //Parsing text fields
    private void parseTextFieldValues(JTextField textField, ChartType type) {
        String[] parsedValues = parseTextField(textField);

        if (type.equals(ChartType.TIMEPLOT)) {
            floatPercentileValuesParse = convertToFloatArray(parsedValues);
        } else if (type.equals(ChartType.SCATTERPLOT)) {
            handleScatterPlotParsing(textField, parsedValues);
        }
    }

    private static String[] parseTextField(JTextField textField) {
        return textField.getText().trim().split("[,:;]");
    }

    private static float[] convertToFloatArray(String[] values) {
        float[] floatArray = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            floatArray[i] = Float.parseFloat(values[i]);
        }
        return floatArray;
    }

    private void handleScatterPlotParsing(JTextField textField, String[] parsedValues) {
        if ("percentiles".equalsIgnoreCase(textField.getName())) {
            floatPercentileValuesParse = convertToFloatArray(parsedValues);
        } else if ("Cumulative Volume".equalsIgnoreCase(textField.getName())) {
            floatCumulativeValuesParse = convertToFloatArray(parsedValues);
        }
    }
    //Parsing text fields

    private void computeProbability(EnsembleTimeSeries ets, String stat) {
        computeMetric(ets, stat, ChartType.SCATTERPLOT);
        statComputationHelper.computeStatFromProbabilityComputable();
    }

    private void computeMetric(EnsembleTimeSeries ets, String statName, ChartType chartType) {
        if (getStatistic(statName) == Statistics.PERCENTILES) {
            statComputationHelper.computeStat(ets, Statistics.PERCENTILES,
                    floatPercentileValuesParse, chartType);
        } else if (Statistics.getStatName(statName) == Statistics.NDAYCOMPUTABLE) {
            statComputationHelper.computeStat(ets,Statistics.NDAYCOMPUTABLE,
                    floatCumulativeValuesParse, chartType);
        } else {
            statComputationHelper.computeStat(ets, getStatistic(statName),
                    chartType);
        }
    }

    private void timeSeriesType(Boolean isCumulative) {
        if(Boolean.TRUE.equals(isCumulative)) {  // if true, compute cumulative time series.
            statComputationHelper.convertToCumulative(databaseHandlerService.getEnsembleTimeSeries());
        }
    }

    private Statistics getStatistic(String statName) {
        return Statistics.getStatName(statName);
    }

    public StatisticsMap getStatisticsMap() {
        return statisticsMap;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getSource() instanceof DatabaseHandlerService) { //Refresh stat lists after new database or record is added
            statisticsMap.refreshStatisticMapList();
        }
    }
}
