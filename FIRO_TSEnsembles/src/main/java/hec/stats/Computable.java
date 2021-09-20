package hec.stats;

public interface Computable extends StatisticsReportable{
    public float compute(float[] values);
}