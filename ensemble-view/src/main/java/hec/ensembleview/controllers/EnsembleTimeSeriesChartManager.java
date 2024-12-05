package hec.ensembleview.controllers;

import hec.ensembleview.DatabaseHandlerService;
import hec.ensembleview.MetricCollectionTimeSeriesContainer;
import hec.ensembleview.PlotStatisticsForChartType;
import hec.ensembleview.charts.EnsembleChart;
import hec.ensembleview.charts.EnsembleChartAcrossTime;
import hec.ensembleview.mappings.StatisticsMap;
import hec.gfx2d.G2dPanel;
import hec.metrics.MetricCollectionTimeSeries;
import hec.metrics.MetricTypes;

import java.beans.PropertyChangeEvent;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EnsembleTimeSeriesChartManager extends ChartManager {
    private static final Logger logger = Logger.getLogger(EnsembleTimeSeriesChartManager.class.getName());
    private boolean isCumulative = false;

    public EnsembleTimeSeriesChartManager(G2dPanel chartPanel, ComputePanelController controller) {
        super(chartPanel, controller.getStatisticsMap());
    }

    @Override
    EnsembleChart createEnsembleChart() {
        if (databaseHandlerService.getDbHandlerRid() == null || databaseHandlerService.getDbHandlerZdt() == null) {
            return null;
        }

        chart = new EnsembleChartAcrossTime();
        isEnsembleDataViewTypeCumulative(isCumulative);
        units = ensemble.getUnits();
        setYAxisChartLabels();
        PlotStatisticsForChartType.addLineMembersToChart(chart, ensemble.getValues(), ensemble.startDateTime());

        return chart;
    }

    @Override
    Map<String, float[]> getMetricValuesFromResidentMetricDatabase() {
        Map<String, float[]> metricValues = new HashMap<>();

        //get map of MetricCollectionTImeSeries data and create a list of stats in map
        List<MetricCollectionTimeSeriesContainer> containers = databaseHandlerService.getMetricCollectionTimeSeriesContainer();

        //iterate through map
        for (MetricCollectionTimeSeriesContainer container : containers) {
            MetricCollectionTimeSeries residentMetricCollectionTimeSeries = container.getMetricCollectionTimeSeries();
            databaseHandlerService.setResidentMetricCollectionTimeSeries(residentMetricCollectionTimeSeries);
            if (isMetricTimeSeries(residentMetricCollectionTimeSeries)) {
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

    void setYAxisChartLabels() {
        String yLabelText = databaseHandlerService.getDbHandlerRid().parameter + " (" + units + ")";
        chart.setYLabel(yLabelText);
    }

    private boolean isMetricTimeSeries(MetricCollectionTimeSeries metricCollections) {
        return metricCollections.getMetricType() == MetricTypes.TIMESERIES_OF_ARRAY;
    }

    void plotResidentMetricChart(EnsembleChart chart, ZonedDateTime[] dates) {
        refreshEnsemblePlot();
        getMetricValuesFromResidentMetricDatabase().forEach((stat, vals) -> {
            try {
                PlotStatisticsForChartType.addMetricStatisticsToTimePlot((EnsembleChartAcrossTime) chart, stat, vals, dates);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error in the date time parse when adding metric statistics to the " +
                        "time plot from the EnsembleChartAcrossTime view class");
                e.printStackTrace();
            }
        });
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void refreshEnsemblePlot() {
        ((EnsembleChartAcrossTime) chart).updateEnsembleLineSpec(isStatisticsChecked());
    }

    private boolean isStatisticsChecked() {
        for(String key : statisticsMap.getTimeSeriesMapList().keySet()) {
            boolean value = statisticsMap.getTimeSeriesMapList().get(key);
            if(value) {
                return true;
            }
        }
        return false;
    }

    private void isEnsembleDataViewTypeCumulative(boolean isCumulativeSelected) {
        if(isCumulativeSelected) {
            ensemble = databaseHandlerService.getCumulativeEnsembleTimeSeries().getEnsemble(databaseHandlerService.getDbHandlerZdt());
        } else {
            ensemble = databaseHandlerService.getEnsemble();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getSource() instanceof StatisticsMap && evt.getPropertyName().equalsIgnoreCase("time")) {
            addEnsembleValues();
            databaseHandlerService.refreshMetricCollectionTimeSeriesMap(statisticsMap);
            try {
                plotResidentMetricChart(chart, ensemble.startDateTime());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error in the date time parse when adding metric statistics to the " +
                        "time plot from the EnsembleChartAcrossTime view class");
            }

        } else if(evt.getSource() instanceof StatisticsMap && evt.getPropertyName().equalsIgnoreCase("cumulative")) {  //This must happen before adding Ensembles
            isCumulative = (boolean) evt.getNewValue();
            isEnsembleDataViewTypeCumulative(isCumulative);
            addEnsembleValues();
        }
        else if(evt.getSource() instanceof DatabaseHandlerService && evt.getOldValue() != null) {
            isCumulative = false;
            this.databaseHandlerService = DatabaseHandlerService.getInstance();
            addEnsembleValues();
        }
    }
}
