package hec.ensembleview;

import hec.ensembleview.charts.ChartType;

public interface ProbabilityDataViewListener {
    void setIsDataViewProbability(Boolean prob);
    void initiateEnsembleCompute(ChartType chartType);
}
