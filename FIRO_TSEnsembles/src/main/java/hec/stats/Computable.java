package hec.stats;

public interface Computable extends StatisticsReportable {
    float compute(float[] values);
}