package hec.ensembleview.controllers;

import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.stats.Statistics;
import hec.ensembleview.*;
import hec.ensembleview.charts.ChartType;
import hec.ensembleview.mappings.StatisticsMap;
import hec.ensembleview.viewpanels.*;

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

    public ComputePanelController(DataTransformView dataTransformView, ComputePanelView computePanelView) {
        this.databaseHandlerService = DatabaseHandlerService.getInstance();
        databaseHandlerService.addDatabaseChangeListener(this);

        if(computePanelView instanceof StatTimeSeriesComputePanelView) {
            initiateTimeSeriesComputePanelListener(computePanelView);
        } else if(computePanelView instanceof StatEnsembleComputePanelView) {
            initiateEnsembleArrayComputePanelListener(computePanelView);
        }

        if(dataTransformView instanceof TimeSeriesDataTransformView) {
            initiateTimeSeriesDataView(dataTransformView);
        } else if (dataTransformView instanceof EnsembleDataTransformView) {
            initiateEnsembleDataView(dataTransformView);
        }
    }

    private void initiateTimeSeriesDataView(DataTransformView dataTransformView) {
        ((TimeSeriesDataTransformView)dataTransformView).setCumulativeListener(new CumulativeDataViewListener() {
            @Override
            public void setIsDataViewCumulative(boolean cumulative) {
                isCumulative = cumulative;
                timeSeriesType(isCumulative);
                statisticsMap.setDataView(isCumulative, ChartType.TIMEPLOT);
            }

            @Override
            public void initiateTimeSeriesCompute(ChartType chartType) {
                for (String stat : statisticsMap.getTimeSeriesMapList().keySet()) {
                    if (isCumulative) {
                        ets = databaseHandlerService.getCumulativeEnsembleTimeSeries();
                    } else {
                        ets = databaseHandlerService.getEnsembleTimeSeries();
                    }
                    if (Boolean.TRUE.equals(statisticsMap.getTimeSeriesMapList().get(stat))) {
                        computeMetric(ets, stat, chartType);
                    }
                }
                statisticsMap.setTimeStatisticsMap();
            }
        });
    }

    private void initiateEnsembleDataView(DataTransformView dataTransformView) {
        ((EnsembleDataTransformView)dataTransformView).setProbabilityListener(new ProbabilityDataViewListener() {
            @Override
            public void setIsDataViewProbability(Boolean prob) {
                isProbability = prob;
                timeSeriesType(isProbability);
                statisticsMap.setDataView(isProbability, ChartType.SCATTERPLOT);
            }

            @Override
            public void initiateEnsembleCompute(ChartType chartType) {
                ets = databaseHandlerService.getEnsembleTimeSeries();
                for (String stat : statisticsMap.getEnsembleSeriesMapList().keySet()) {
                    if (isProbability) {
                        if (Boolean.TRUE.equals(statisticsMap.getEnsembleSeriesMapList().get(stat))) {
                            computeMetric(ets, stat, chartType);
                            statComputationHelper.computeStatFromProbabilityComputable();
                        }
                    } else if (Boolean.TRUE.equals(statisticsMap.getEnsembleSeriesMapList().get(stat))) {
                        computeMetric(ets, stat, chartType);
                    }
                }
                statisticsMap.setEnsembleStatisticsMap();
            }
        });
    }

    private void initiateTimeSeriesComputePanelListener(ComputePanelView computePanelView) {
        ((StatTimeSeriesComputePanelView)computePanelView).setTimeListener(new TimeSeriesComputePanelListener() {
            @Override
            public void initiateTimeSeriesCompute() {
                for (String stat : statisticsMap.getTimeSeriesMapList().keySet()) {
                    if (isCumulative) {
                        ets = databaseHandlerService.getCumulativeEnsembleTimeSeries();
                    } else {
                        ets = databaseHandlerService.getEnsembleTimeSeries();
                    }
                    if (Boolean.TRUE.equals(statisticsMap.getTimeSeriesMapList().get(stat))) {
                        computeMetric(ets, stat, ChartType.TIMEPLOT);
                    }
                }
                statisticsMap.setTimeStatisticsMap();
            }

            @Override
            public void setCheckedStatistics(String name) {
                Map<String, Boolean> map = new HashMap<>();
                map.put(name, true);
                statisticsMap.storeTimeStatisticList(map);

            }

            @Override
            public void setRemovedStatistics(String name) {
                Map<String, Boolean> map = new HashMap<>();
                map.put(name, false);
                statisticsMap.storeTimeStatisticList(map);
            }

            @Override
            public void getTextFieldValues(JTextField textField) {
                String textValues = textField.getText();
                String[] textValuesParse = textValues.trim().split("[,:;]");
                floatPercentileValuesParse = new float[textValuesParse.length];
                for (int i = 0; i < textValuesParse.length; i++) {
                    floatPercentileValuesParse[i] = Float.parseFloat(textValuesParse[i]);
                }
            }
        });
    }

    private void initiateEnsembleArrayComputePanelListener(ComputePanelView computePanelView) {
        ((StatEnsembleComputePanelView) computePanelView).setEnsembleListener(new EnsembleArrayComputePanelListener() {
            @Override
            public void initiateEnsembleCompute() {
                ets = databaseHandlerService.getEnsembleTimeSeries();
                for (String stat : statisticsMap.getEnsembleSeriesMapList().keySet()) {
                    boolean isStatSelected = Boolean.TRUE.equals(statisticsMap.getEnsembleSeriesMapList().get(stat));
                    if (isProbability && isStatSelected) {
                        computeProbability(ets, stat);
                    } else if (!isProbability && isStatSelected) {
                        computeMetric(ets, stat, ChartType.SCATTERPLOT);
                    }
                }
                statisticsMap.setEnsembleStatisticsMap();
            }

            @Override
            public void setCheckedStatistics(String name) {
                Map<String, Boolean> map = new HashMap<>();
                map.put(name, true);
                statisticsMap.storeEnsembleStatisticList(map);
            }

            @Override
            public void setRemovedStatistics(String name) {
                Map<String, Boolean> map = new HashMap<>();
                map.put(name, false);
                statisticsMap.storeEnsembleStatisticList(map);
            }

            @Override
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
        });
    }

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
