package hec.ensemble.stats;

public interface MultiComputable extends StatisticsReportable {
    float[] multiCompute(float[] values);
    String getOutputUnits();
}
