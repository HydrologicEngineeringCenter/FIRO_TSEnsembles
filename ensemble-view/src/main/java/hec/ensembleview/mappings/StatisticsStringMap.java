package hec.ensembleview.mappings;

import hec.ensembleview.StatisticUIType;
import hec.stats.Statistics;

import java.util.HashMap;
import java.util.Map;

public class StatisticsStringMap {
    public final static Map<Statistics, String> map = new HashMap<>();

    static {
        map.put(Statistics.MAX, "Max");
        map.put(Statistics.MIN, "Min");
        map.put(Statistics.MEAN, "Mean");
        map.put(Statistics.MEDIAN, "Median");
        map.put(Statistics.PERCENTILE, "Percentile");
        map.put(Statistics.CUMULATIVE, "Cumulative");
        map.put(Statistics.MAXAVERAGEDURATION, "Max Average Duration");
    }
}
