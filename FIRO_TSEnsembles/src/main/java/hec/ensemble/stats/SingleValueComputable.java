package hec.ensemble.stats;

public interface SingleValueComputable extends StatisticsReportable{
    float compute(float[][] values);
    String getOutputUnits();

}
