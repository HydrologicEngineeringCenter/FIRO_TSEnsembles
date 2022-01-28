package hec.ensembleview.mappings;

import hec.stats.Statistics;

import java.util.HashMap;
import java.util.Map;

public class StatisticsStringMap {
    public final static Map<Statistics, String> map = new HashMap<>();

    static {
        map.put(Statistics.MAX, "Max");
        map.put(Statistics.MIN, "Min");
        map.put(Statistics.MEAN, "Mean");
    }
}
