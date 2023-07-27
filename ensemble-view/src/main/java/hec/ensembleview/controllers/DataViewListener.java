package hec.ensembleview.controllers;

import hec.ensembleview.charts.ChartType;

public interface DataViewListener {
    void setIsDataViewProbability(Boolean prob);
    void initiateEnsembleCompute(ChartType chartType);
    void setIsDataViewCumulative(boolean cumulative);
    void initiateTimeSeriesCompute(ChartType chartType);

}
