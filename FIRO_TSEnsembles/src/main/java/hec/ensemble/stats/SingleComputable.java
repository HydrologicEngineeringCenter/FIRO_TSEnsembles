package hec.ensemble.stats;

public interface SingleComputable extends StatisticsReportable{
    public float compute(float[][] values);
}
