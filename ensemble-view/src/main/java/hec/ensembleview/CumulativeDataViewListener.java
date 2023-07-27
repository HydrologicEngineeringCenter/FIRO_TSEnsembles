package hec.ensembleview;

import hec.ensembleview.charts.ChartType;

public interface CumulativeDataViewListener {
    void setIsDataViewCumulative(boolean cumulative);
    void initiateTimeSeriesCompute(ChartType chartType);
}
