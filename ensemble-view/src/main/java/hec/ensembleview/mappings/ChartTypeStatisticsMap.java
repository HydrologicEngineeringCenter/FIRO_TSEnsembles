package hec.ensembleview.mappings;

import hec.ensembleview.ChartType;
import hec.ensemble.stats.Statistics;

import java.util.*;

public class ChartTypeStatisticsMap { //maps the statistics to the specific statistics panel
    public static Map<ChartType, List<Statistics>> map = new HashMap<>();
    static {
        map.put(ChartType.TimePlot, Arrays.asList(Statistics.MIN, Statistics.MAX, Statistics.AVERAGE, Statistics.MEDIAN, Statistics.CUMULATIVE, Statistics.NONE, Statistics.PERCENTILE, Statistics.STANDARDDEVIATION, Statistics.VARIANCE));
        map.put(ChartType.ScatterPlot, Arrays.asList(Statistics.MIN, Statistics.MAX, Statistics.AVERAGE, Statistics.MEDIAN, Statistics.TOTAL, Statistics.PERCENTILE, Statistics.MAXAVERAGEDURATION, Statistics.MAXACCUMDURATION, Statistics.STANDARDDEVIATION, Statistics.VARIANCE));
    }
}
