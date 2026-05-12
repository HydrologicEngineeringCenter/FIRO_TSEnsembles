package hec.ensemble.stats;

public interface ComputableIndex extends StatisticsReportable {
    int compute(float[] values);
    String getOutputUnits();
}