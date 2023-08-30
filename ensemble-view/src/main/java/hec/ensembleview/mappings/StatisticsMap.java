package hec.ensembleview.mappings;

import hec.ensembleview.charts.ChartType;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

public class StatisticsMap { //maps the statistics to the specific statistics panel
    private Map<String, Boolean> timeSeriesMapList = new HashMap<>();
    private final Map<String, Boolean> ensembleSeriesMapList = new HashMap<>();
    private boolean isCumulativeSelected = false;
    private boolean isProbabilitySelected = false;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public void addStatisticsMapChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void setTimeStatisticsMap() {
        if(this.timeSeriesMapList.size() == 0) {
            return;
        }
        support.firePropertyChange("time", false, true);
    }

    public void setEnsembleStatisticsMap() {
        if(this.ensembleSeriesMapList.size() == 0) {
            return;
        }
        support.firePropertyChange("ensemble", false, true);
    }

    public void setDataView(boolean isSelected, ChartType chartType) {
        if(chartType == ChartType.TIMEPLOT) {
            boolean currentSelection = this.isCumulativeSelected;
            if(currentSelection != isSelected) {
                this.isCumulativeSelected = isSelected;
            }
            support.firePropertyChange("cumulative", currentSelection, isSelected);
        } else if (chartType == ChartType.SCATTERPLOT) {
            boolean currentSelection = this.isProbabilitySelected;
            if(currentSelection != isSelected) {
                this.isProbabilitySelected = isSelected;
            }
            support.firePropertyChange("probability", currentSelection, isSelected);
        }
    }

    public void storeTimeStatisticList(Map<String, Boolean> map) {
        timeSeriesMapList.putAll(map);
    }

    public void storeEnsembleStatisticList(Map<String, Boolean> map) {
        ensembleSeriesMapList.putAll(map);
    }

    public Map<String, Boolean> getTimeSeriesMapList() {
        return timeSeriesMapList;
    }

    public Map<String, Boolean> getEnsembleSeriesMapList() {
        return ensembleSeriesMapList;
    }

    public void refreshStatisticMapList() {
        timeSeriesMapList.clear();
        ensembleSeriesMapList.clear();
    }
}
