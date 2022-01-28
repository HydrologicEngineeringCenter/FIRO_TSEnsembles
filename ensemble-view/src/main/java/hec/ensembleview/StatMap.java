package hec.ensembleview;

import hec.stats.Statistics;
import java.util.HashMap;
import java.util.Map;

public class StatMap {
    public final static Map<Statistics, StatType> map = new HashMap<>();

    static {
        map.put(Statistics.MAX, StatType.CHECKBOX);
        map.put(Statistics.MIN, StatType.CHECKBOX);
        map.put(Statistics.MEAN, StatType.CHECKBOX);
    }
}
