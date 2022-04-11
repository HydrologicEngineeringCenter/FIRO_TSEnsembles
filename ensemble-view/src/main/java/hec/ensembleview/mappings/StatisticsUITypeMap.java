package hec.ensembleview.mappings;

import hec.ensembleview.StatisticUIType;
import hec.stats.Statistics;
import java.util.HashMap;
import java.util.Map;

public class StatisticsUITypeMap {
    public final static Map<Statistics, StatisticUIType> map = new HashMap<>();

    static {
        map.put(Statistics.MAX, StatisticUIType.CHECKBOX);
        map.put(Statistics.MIN, StatisticUIType.CHECKBOX);
        map.put(Statistics.MEAN, StatisticUIType.CHECKBOX);
        map.put(Statistics.MEDIAN, StatisticUIType.CHECKBOX);
        map.put(Statistics.TOTAL, StatisticUIType.CHECKBOX);
        map.put(Statistics.CUMULATIVE, StatisticUIType.CHECKBOX);
        map.put(Statistics.PERCENTILE, StatisticUIType.TEXTBOX);
        map.put(Statistics.MAXAVERAGEDURATION, StatisticUIType.TEXTBOX);
        map.put(Statistics.MAXACCUMDURATION, StatisticUIType.TEXTBOX);
    }
}

