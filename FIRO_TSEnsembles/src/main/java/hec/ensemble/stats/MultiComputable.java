package hec.ensemble.stats;

public interface MultiComputable extends StatisticsReportable {
    public float[] multiCompute(float[] values);
}
