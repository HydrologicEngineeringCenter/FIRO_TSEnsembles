package hec.ensembleview.mappings;

import hec.ensemble.stats.Statistics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleValueComboBoxMap {
    public final static Map<SingleValueSummaryType, String> summaryComboBoxMap = new HashMap<>();
    public final static Map<SingleValueSummaryType, List<List<Statistics>>> summaryStatisticsMap = new HashMap<>();

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
                        Arrays.asList(Statistics.CUMULATIVE),
                        Arrays.asList(Statistics.MIN, Statistics.MAX, Statistics.AVERAGE, Statistics.MEDIAN, Statistics.STANDARDDEVIATION, Statistics.VARIANCE, Statistics.PERCENTILES, Statistics.TOTAL)
                )
        );
    }
}
