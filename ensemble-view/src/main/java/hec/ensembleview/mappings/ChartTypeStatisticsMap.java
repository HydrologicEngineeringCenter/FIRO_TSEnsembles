package hec.ensembleview.mappings;

import hec.ensembleview.ChartType;
import hec.stats.Statistics;

import java.util.*;

public class ChartTypeStatisticsMap {
    public static Map<ChartType, List<Statistics>> map = new HashMap<>();
    static {
        map.put(ChartType.TimePlot, Arrays.asList(Statistics.MIN, Statistics.MAX, Statistics.MEAN, Statistics.MEDIAN, Statistics.CUMULATIVE, Statistics.NONE, Statistics.PERCENTILE));
        map.put(ChartType.ScatterPlot, Arrays.asList(Statistics.MIN, Statistics.MAX, Statistics.MEAN, Statistics.MEDIAN, Statistics.TOTAL, Statistics.PERCENTILE, Statistics.MAXAVERAGEDURATION, Statistics.MAXACCUMDURATION));
    }
}
