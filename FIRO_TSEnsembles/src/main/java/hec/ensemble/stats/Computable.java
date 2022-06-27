package hec.ensemble.stats;

public interface Computable extends StatisticsReportable {
    float compute(float[] values);
}