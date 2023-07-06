package hec.ensembleview.mappings;

import hec.ensemble.stats.Statistics;

import java.util.HashMap;
import java.util.Map;

public class StatisticsStringMap {
    public final static Map<Statistics, String> map = new HashMap<>();

    static {
        map.put(Statistics.MAX, "Max");
        map.put(Statistics.MIN, "Min");
        map.put(Statistics.AVERAGE, "Average");
        map.put(Statistics.MEDIAN, "Median");
        map.put(Statistics.STANDARDDEVIATION, "Standard Deviation");
        map.put(Statistics.VARIANCE, "Variance");
        map.put(Statistics.PERCENTILE, "Percentile");
        map.put(Statistics.TOTAL, "Total Flow");
        map.put(Statistics.CUMULATIVE, "Cumulative");
        map.put(Statistics.NONE, "Original");
        map.put(Statistics.MAXAVERAGEDURATION, "Max Average Duration (cfs)");
        map.put(Statistics.MAXACCUMDURATION, "Max Cumulative Volume (acre-ft)");
        map.put(Statistics.PLOTTINGPOSITION, "Probability Plot");
        map.put(Statistics.NDAYCUMULATIVE, "Cumulative Volume (acre-ft)");
    }
}
