package hec.ensemble.stats;

public interface SingleTimeSeriesComputable extends StatisticsReportable{
    float[] compute(float[][] values);
    String getOutputUnits();

}
