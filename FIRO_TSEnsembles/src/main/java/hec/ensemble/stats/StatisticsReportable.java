package hec.ensemble.stats;

public interface StatisticsReportable {
    @Deprecated
    Statistics[] Statistics();
    String StatisticsLabel();
}
