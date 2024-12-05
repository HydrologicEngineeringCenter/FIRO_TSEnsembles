package hec.ensembleview.controllers;

import hec.ensembleview.charts.ChartType;

public interface DataViewListener {
    void setIsDataViewProbability(Boolean prob);
    void setIsDataViewCumulative(boolean cumulative);
    void initiateCompute(ChartType chartType);
}
