package hec.ensembleview.mappings;

import hec.ensemble.stats.Statistics;

import java.util.HashMap;
import java.util.Map;

public class StatisticsStringMap {
    public final static Map<Statistics, String> map = new HashMap<>();

    static {
        map.put(Statistics.MAX, "Max (cfs)");
        map.put(Statistics.MIN, "Min (cfs)");
        map.put(Statistics.AVERAGE, "Average (cfs)");
        map.put(Statistics.MEDIAN, "Median (cfs)");
        map.put(Statistics.STANDARDDEVIATION, "Standard Deviation (cfs)");
        map.put(Statistics.VARIANCE, "Variance");
        map.put(Statistics.PERCENTILE, "Percentile (%)");
        map.put(Statistics.TOTAL, "Total Volume (acre-ft");
        map.put(Statistics.CUMULATIVE, "Cumulative Volume");
        map.put(Statistics.NONE, "Original");
        map.put(Statistics.MAXAVERAGEDURATION, "Max Average Duration (cfs)");
        map.put(Statistics.MAXACCUMDURATION, "Max Cumulative Volume (acre-ft)");
        map.put(Statistics.PLOTTINGPOSITION, "Probability Plot");
        map.put(Statistics.NDAYCUMULATIVE, "Cumulative Volume (acre-ft)");
    }
}
