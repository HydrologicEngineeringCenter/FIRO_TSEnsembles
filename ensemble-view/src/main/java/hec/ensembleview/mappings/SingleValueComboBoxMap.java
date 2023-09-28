package hec.ensembleview.mappings;

import hec.ensemble.stats.Statistics;

import java.util.*;

public class SingleValueComboBoxMap {
    private static final Map<SingleValueSummaryType, List<List<Statistics>>> summaryStatisticsMap = new EnumMap<>(SingleValueSummaryType.class);

    static {
        summaryStatisticsMap.put(SingleValueSummaryType.COMPUTEACROSSENSEMBLES,
                Arrays.asList(
                        Arrays.asList(Statistics.MIN, Statistics.MAX, Statistics.AVERAGE, Statistics.MEDIAN, Statistics.PERCENTILES),
                        Arrays.asList(Statistics.MIN, Statistics.MAX)
                )
        );
        summaryStatisticsMap.put(SingleValueSummaryType.COMPUTEACROSSTIME,
                Arrays.asList(
                        Arrays.asList(Statistics.MIN, Statistics.MAX, Statistics.AVERAGE, Statistics.TOTAL, Statistics.CUMULATIVE),
                        Arrays.asList(Statistics.MIN, Statistics.MAX, Statistics.AVERAGE, Statistics.MEDIAN, Statistics.STANDARDDEVIATION, Statistics.PERCENTILES)
                )
        );
    }

    public static Map<SingleValueSummaryType, List<List<Statistics>>> getSummaryStatisticsMap() {
        return summaryStatisticsMap;
    }
}
