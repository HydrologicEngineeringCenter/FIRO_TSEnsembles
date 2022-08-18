package hec.ensembleview.mappings;

import hec.ensembleview.StatisticUIType;
import hec.ensemble.stats.Statistics;
import java.util.HashMap;
import java.util.Map;

public class StatisticsUITypeMap {
    public final static Map<Statistics, StatisticUIType> map = new HashMap<>();

    static {
        map.put(Statistics.MAX, StatisticUIType.CHECKBOX);
        map.put(Statistics.MIN, StatisticUIType.CHECKBOX);
        map.put(Statistics.AVERAGE, StatisticUIType.CHECKBOX);
        map.put(Statistics.MEDIAN, StatisticUIType.CHECKBOX);
        map.put(Statistics.TOTAL, StatisticUIType.CHECKBOX);
        map.put(Statistics.STANDARDDEVIATION, StatisticUIType.CHECKBOX);
        map.put(Statistics.VARIANCE, StatisticUIType.CHECKBOX);
        map.put(Statistics.CUMULATIVE, StatisticUIType.RADIOBUTTON);
        map.put(Statistics.NONE, StatisticUIType.RADIOBUTTON);
        map.put(Statistics.PERCENTILE, StatisticUIType.TEXTBOX);
        map.put(Statistics.MAXAVERAGEDURATION, StatisticUIType.TEXTBOX);
        map.put(Statistics.MAXACCUMDURATION, StatisticUIType.TEXTBOX);
    }
}

