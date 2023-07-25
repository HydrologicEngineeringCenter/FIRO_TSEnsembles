package hec.ensembleview.controllers;

import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.stats.Statistics;
import hec.ensembleview.DatabaseHandlerService;
import hec.ensembleview.StatComputationHelper;
import hec.ensembleview.mappings.StatisticsMap;
import hec.ensembleview.charts.ChartType;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is a controller class and handles events from the ComputePanel and DataView Panel.
 */

public class ComputePanelController implements PropertyChangeListener {
    private final DatabaseHandlerService databaseHandlerService;
    private float[] floatPercentileValuesParse;
    private float[] floatCumulativeValuesParse;
    private boolean isCumulative = false;
    private boolean isProbability = false;
    private EnsembleTimeSeries ets;
    private final StatisticsMap statisticsMap = new StatisticsMap();
    private final StatComputationHelper statComputationHelper = new StatComputationHelper();
    public ComputePanelController() {
        this.databaseHandlerService = DatabaseHandlerService.getInstance();
        databaseHandlerService.addDatabaseChangeListener(this);
    }

    public void initiateTimeSeriesCompute(ChartType chartType) {
        for (String stat : statisticsMap.getTimeSeriesMapList().keySet()) {
            if (isCumulative) {
                this.ets = databaseHandlerService.getCumulativeEnsembleTimeSeries();
            } else {
                this.ets = databaseHandlerService.getEnsembleTimeSeries();
            }
            if (Boolean.TRUE.equals(statisticsMap.getTimeSeriesMapList().get(stat))) {
                computeTimeSeriesStat(this.ets, stat, chartType);
            }
        }
        statisticsMap.setTimeStatisticsMap();
    }

    public void initiateEnsembleCompute(ChartType chartType) {
        this.ets = databaseHandlerService.getEnsembleTimeSeries();

        for (String stat : statisticsMap.getEnsembleSeriesMapList().keySet()) {
            if (isProbability) {
                if (Boolean.TRUE.equals(statisticsMap.getEnsembleSeriesMapList().get(stat))) {
                    computeTimeSeriesStat(this.ets, stat, chartType);
                    statComputationHelper.computeStatFromProbabilityComputable();
                }
            } else if (Boolean.TRUE.equals(statisticsMap.getEnsembleSeriesMapList().get(stat))) {
                computeTimeSeriesStat(this.ets, stat, chartType);
            }
        }
        statisticsMap.setEnsembleStatisticsMap();
    }

    private void computeTimeSeriesStat(EnsembleTimeSeries ets, String statName, ChartType chartType) {
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

    public void setIsDataViewCumulative(boolean isCumulative) {  // is set by DataView Panel
        this.isCumulative = isCumulative;
        timeSeriesType(isCumulative);
        statisticsMap.setDataView(this.isCumulative, ChartType.TIMEPLOT);
    }

    public void setIsDataViewProbability(boolean isProbability) {  // is set by DataView Panel
        this.isProbability = isProbability;
        statisticsMap.setDataView(this.isProbability, ChartType.SCATTERPLOT);
    }

    public void setCheckedStatistics(String name, ChartType chartType) {  // sets selected stat and adds to list
        Map<String, Boolean> map = new HashMap<>();
        map.put(name, true);
        if(chartType == ChartType.TIMEPLOT) {
            statisticsMap.storeTimeStatisticList(map);
        } else if (chartType == ChartType.SCATTERPLOT) {
            statisticsMap.storeEnsembleStatisticList(map);
        }
    }

    public void setRemovedStatistics(String name, ChartType chartType) {  // remove stat from stat and adds to list
        Map<String, Boolean> map = new HashMap<>();
        map.put(name, false);
        if(chartType == ChartType.TIMEPLOT) {
            statisticsMap.storeTimeStatisticList(map);
        } else if (chartType == ChartType.SCATTERPLOT) {
            statisticsMap.storeEnsembleStatisticList(map);
        }
    }

    public void getTextFieldValues(JTextField textField) {
        String textValues = textField.getText();
        String[] textValuesParse = textValues.trim().split("[,:;]");

        if(textField.getName().equalsIgnoreCase("percentiles")) {
            floatPercentileValuesParse = new float[textValuesParse.length];
            for(int i = 0; i < textValuesParse.length; i++) {
                floatPercentileValuesParse[i] = Float.parseFloat(textValuesParse[i]);
            }
        }else if(textField.getName().equalsIgnoreCase("Cumulative Volume")) {
            floatCumulativeValuesParse = new float[textValuesParse.length];
            for(int i = 0; i < textValuesParse.length; i++) {
                floatCumulativeValuesParse[i] = Float.parseFloat(textValuesParse[i]);
            }
        }
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
