package hec.ensemble.stats;

public interface SingleComputable extends StatisticsReportable{
    float compute(float[][] values);
    String getOutputUnits();

}
