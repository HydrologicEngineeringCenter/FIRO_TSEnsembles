package hec.ensembleview.mappings;

import hec.ensembleview.ChartType;
import hec.ensembleview.SingleValueSummaryType;
import hec.stats.Statistics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleValueComboBoxMap {
    public final static Map<SingleValueSummaryType, String> summaryComboBoxMap = new HashMap<>();
    public final static Map<SingleValueSummaryType, List<List<Statistics>>> summaryStatisticsMap = new HashMap<>();

    static {
        summaryComboBoxMap.put(SingleValueSummaryType.ComputeAcrossEnsembles, "Compute Across Ensembles for Each Time Step");
        summaryComboBoxMap.put(SingleValueSummaryType.ComputeAcrossTime, "Compute Across Time Steps for Each Ensemble");
        summaryComboBoxMap.put(SingleValueSummaryType.ComputeCumulative, "Computing Cumulative");

        summaryStatisticsMap.put(SingleValueSummaryType.ComputeAcrossEnsembles,
                Arrays.asList(
                        Arrays.asList(Statistics.MIN, Statistics.MAX, Statistics.MEAN, Statistics.MEDIAN, Statistics.PERCENTILE),
                        Arrays.asList(Statistics.MIN, Statistics.MAX, Statistics.MEAN, Statistics.MEDIAN, Statistics.PERCENTILE, Statistics.MAXACCUMDURATION, Statistics.MAXAVERAGEDURATION)
                )
        );
        summaryStatisticsMap.put(SingleValueSummaryType.ComputeAcrossTime,
                Arrays.asList(
                        Arrays.asList(Statistics.MIN, Statistics.MAX, Statistics.MEAN, Statistics.MEDIAN, Statistics.PERCENTILE, Statistics.TOTAL, Statistics.MAXACCUMDURATION, Statistics.MAXAVERAGEDURATION),
                        Arrays.asList(Statistics.MIN, Statistics.MAX, Statistics.MEAN, Statistics.MEDIAN, Statistics.PERCENTILE)
                )
        );
        summaryStatisticsMap.put(SingleValueSummaryType.ComputeCumulative,
                Arrays.asList(
                        Arrays.asList(Statistics.CUMULATIVE),
                        Arrays.asList(Statistics.MIN, Statistics.MAX, Statistics.MEAN, Statistics.MEDIAN, Statistics.PERCENTILE, Statistics.TOTAL)
                )
        );
    }
}