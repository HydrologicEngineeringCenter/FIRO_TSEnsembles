package hec.ensembleview.mappings;

import hec.ensemble.stats.Statistics;

import java.util.*;

public class SingleValueComboBoxMap {
    private static final Map<SingleValueSummaryType, String> summaryComboBoxMap = new EnumMap<>(SingleValueSummaryType.class);
    private static final Map<SingleValueSummaryType, List<List<Statistics>>> summaryStatisticsMap = new EnumMap<>(SingleValueSummaryType.class);

    static {
        summaryComboBoxMap.put(SingleValueSummaryType.COMPUTEACROSSENSEMBLES, "Compute Across Ensembles for Each Time Step");
        summaryComboBoxMap.put(SingleValueSummaryType.COMPUTEACROSSTIME, "Compute Across Time Steps for Each Ensemble");
        summaryComboBoxMap.put(SingleValueSummaryType.COMPUTECUMULATIVE, "Computing Cumulative");

        summaryStatisticsMap.put(SingleValueSummaryType.COMPUTEACROSSENSEMBLES,
                Arrays.asList(
                        Arrays.asList(Statistics.MIN, Statistics.MAX, Statistics.AVERAGE, Statistics.MEDIAN, Statistics.PERCENTILES),
                        Arrays.asList(Statistics.MIN, Statistics.MAX)
                )
        );
        summaryStatisticsMap.put(SingleValueSummaryType.COMPUTEACROSSTIME,
                Arrays.asList(
                        Arrays.asList(Statistics.MIN, Statistics.MAX, Statistics.AVERAGE, Statistics.TOTAL, Statistics.MAXACCUMDURATION, Statistics.MAXAVERAGEDURATION),
                        Arrays.asList(Statistics.MIN, Statistics.MAX, Statistics.AVERAGE, Statistics.MEDIAN, Statistics.STANDARDDEVIATION, Statistics.VARIANCE, Statistics.PERCENTILES)
                )
        );
        summaryStatisticsMap.put(SingleValueSummaryType.COMPUTECUMULATIVE,
                Arrays.asList(
                        Collections.singletonList(Statistics.CUMULATIVE),
                        Arrays.asList(Statistics.MIN, Statistics.MAX, Statistics.AVERAGE, Statistics.MEDIAN, Statistics.STANDARDDEVIATION, Statistics.VARIANCE, Statistics.PERCENTILES, Statistics.TOTAL)
                )
        );
    }

    public static Map<SingleValueSummaryType, String> getSummaryComboBoxMap() {
        return summaryComboBoxMap;
    }

    public static Map<SingleValueSummaryType, List<List<Statistics>>> getSummaryStatisticsMap() {
        return summaryStatisticsMap;
    }
}
