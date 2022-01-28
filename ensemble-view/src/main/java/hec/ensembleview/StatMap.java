package hec.ensembleview;

import hec.stats.Statistics;
import java.util.HashMap;
import java.util.Map;

public class StatMap {
    public final static Map<Statistics, StatisticUIType> map = new HashMap<>();

    static {
        map.put(Statistics.MAX, StatisticUIType.CHECKBOX);
        map.put(Statistics.MIN, StatisticUIType.CHECKBOX);
        map.put(Statistics.MEAN, StatisticUIType.CHECKBOX);
    }
}
