package hec.ensemble.stats;

public interface MultiTimeSeriesComputable extends StatisticsReportable {
    /**
     * Computes multiple time series from ensemble data.
     * @param values ensemble data where rows represent ensemble members and columns are time steps
     * @return float[][] where each row is a time series for one statistic/duration
     */
    float[][] compute(float[][] values);
    
    String getOutputUnits();
    
    /**
     * Returns the number of time series this computable produces
     */
    default int getTimeSeriesCount() {
        return StatisticsLabel().split("\\|").length;
    }
}