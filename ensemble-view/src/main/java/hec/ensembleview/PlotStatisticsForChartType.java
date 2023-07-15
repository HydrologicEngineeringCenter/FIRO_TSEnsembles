package hec.ensembleview;

import hec.EnsembleDatabase;
import hec.RecordIdentifier;
import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.stats.Statistics;
import hec.ensembleview.mappings.StatisticsStringMap;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.util.Random;

public class PlotStatisticsForChartType {
    private final EnsembleDatabase db;
    private final RecordIdentifier rid;
    private final ZonedDateTime zdt;

    public PlotStatisticsForChartType(EnsembleDatabase database, ZonedDateTime zonedDateTime, RecordIdentifier recordIdentifier) {
        db = database;
        zdt = zonedDateTime;
        rid = recordIdentifier;
    }

    public void addStatisticsToProbabilityPlot(EnsembleChartAcrossEnsembles chart, EnsembleViewStat[] stats, EnsembleTimeSeries ets) {
        for (EnsembleViewStat selectedStat : stats) {
            Statistics statType = selectedStat.getStatType();
            switch (statType) {
                case MIN:
                case MAX:
                case AVERAGE:
                case MEDIAN:
                case MAXACCUMDURATION:
                case MAXAVERAGEDURATION:
                case TOTAL:
                    chart.addProbPoint(
                            new PointSpec(0,
                                    StatComputationHelper.computeProbability(ets, statType, zdt, ChartType.SCATTERPLOT),
                                    getStrokeForStatType(statType),
                                    getColorForStatType(statType),
                                    StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case PERCENTILE:
                    float[] percentiles = ((TextBoxStat) selectedStat).getTextFieldValue();
                    DecimalFormat df = new DecimalFormat("0.0");

                    for(int i = 0; i < percentiles.length; i++) {
                        chart.addProbPoint(
                                new PointSpec(0, StatComputationHelper.computeProbability(ets, statType, zdt, new float[] {(percentiles[i])}, ChartType.SCATTERPLOT),
                                        getStrokeForStatType(statType), randomColor(i+100), StatisticsStringMap.map.get(selectedStat.getStatType()) + " " + df.format(percentiles[i]*100) + "%"));
                    }
                    break;
                case NDAYCUMULATIVE:
                    float[] accumulatedDays = ((TextBoxStat) selectedStat).getTextFieldValue();

                    for(int i = 0; i< accumulatedDays.length; i++) {
                        chart.addProbPoint(
                                new PointSpec(1, StatComputationHelper.computeProbability(db.getEnsembleTimeSeries(rid), statType, zdt, new float[] {(accumulatedDays[i])}, ChartType.SCATTERPLOT),
                                        getStrokeForStatType(statType), randomColor(i + 100), StatisticsStringMap.map.get(selectedStat.getStatType()) + accumulatedDays[i] + " days"));
                    }
                    break;
            }
        }
    }

    public void addStatisticsToTimePlot(EnsembleChartAcrossTime chart, EnsembleViewStat[] stats, EnsembleTimeSeries ets, ZonedDateTime[] dates) throws Exception {
        for (EnsembleViewStat selectedStat : stats) {

            Statistics statType = selectedStat.getStatType();

            switch (statType) {
                case MIN:
                case MAX:
                case AVERAGE:
                case MEDIAN:
                case STANDARDDEVIATION:
                case VARIANCE:
                    Color lineColor = getColorForStatType(statType);
                    BasicStroke stroke = getStrokeForStatType(statType);
                    String statLabel = StatisticsStringMap.map.get(statType);
                    chart.addLine(
                            new LineSpec(0, StatComputationHelper.computeStat(ets, statType, zdt, ChartType.TIMEPLOT), dates, stroke, lineColor, statLabel));
                    break;

                case PERCENTILE:
                    float[] percentiles = null; // Initialize percentiles if needed
                    percentiles = ((TextBoxStat) selectedStat).getTextFieldValue();
                    DecimalFormat df = new DecimalFormat("0.0");

                    for (int i = 0; i < percentiles.length; i++) {
                        float percentileValue = percentiles[i];
                        chart.addLine(
                                new LineSpec(0, StatComputationHelper.computeStat(ets, statType, zdt, new float[]{percentileValue}, ChartType.TIMEPLOT),
                                        dates, getStrokeForStatType(statType), randomColor(i + 1),
                                        statType + " " + df.format(percentileValue * 100) + "%"));
                    }
                    break;
            }
        }
    }

    public void addStatisticsToScatterPlot(EnsembleChartAcrossEnsembles chart, EnsembleViewStat[] stats, EnsembleTimeSeries ets) throws Exception {
        for (EnsembleViewStat selectedStat : stats) {
            Statistics statType = selectedStat.getStatType();

            switch (statType) {
                case MIN:
                case MAX:
                case AVERAGE:
                case MEDIAN:
                case STANDARDDEVIATION:
                case VARIANCE:
                case TOTAL:
                case MAXAVERAGEDURATION:
                case MAXACCUMDURATION:
                    chart.addPoint(
                            new PointSpec(0,
                                    StatComputationHelper.computeStat(ets, selectedStat.getStatType(), zdt, ChartType.SCATTERPLOT),
                                    getStrokeForStatType(statType),
                                    getColorForStatType(statType),
                                    StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case PERCENTILE:
                    float[] percentiles = ((TextBoxStat) selectedStat).getTextFieldValue();
                    DecimalFormat df = new DecimalFormat("0.0");

                    for(int i = 0; i < percentiles.length; i++) {

                        chart.addPoint(
                                new PointSpec(0, StatComputationHelper.computeStat(ets, selectedStat.getStatType(), zdt, new float[] {(percentiles[i])}, ChartType.SCATTERPLOT),
                                        new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                1.0f, new float[]{6.0f, 6.0f}, 0.0f), randomColor(i+1), StatisticsStringMap.map.get(selectedStat.getStatType()) + " " + df.format(percentiles[i]*100) + "%"));
                    }
                    break;
                case NDAYCUMULATIVE:
                    float[] accumulatedDays = ((TextBoxStat) selectedStat).getTextFieldValue();

                    for(int i = 0; i< accumulatedDays.length; i++) {
                        chart.addPoint(
                                new PointSpec(1, StatComputationHelper.computeStat(db.getEnsembleTimeSeries(rid), selectedStat.getStatType(), zdt, new float[] {(accumulatedDays[i])}, ChartType.SCATTERPLOT),
                                        new BasicStroke(3.0f), Color.DARK_GRAY, StatisticsStringMap.map.get(selectedStat.getStatType()) + accumulatedDays[i] + " days"));
                    }
                    break;
            }
        }
    }

    public EnsembleTimeSeries getCumulativeEnsembles(Ensemble ensemble) {
        float[][] cumulative = StatComputationHelper.computeTimeSeriesView(db.getEnsembleTimeSeries(rid),
                Statistics.CUMULATIVE, zdt);
        EnsembleTimeSeries ets = new EnsembleTimeSeries(rid, "acre-ft", "data_type", "version");
        ets.addEnsemble(new Ensemble(ensemble.getIssueDate(), cumulative, ensemble.getStartDateTime(), ensemble.getInterval(), ensemble.getUnits()));

        return ets;
    }


    private BasicStroke getStrokeForStatType(Statistics statType) {
        switch (statType) {
            case MIN:
            case MAX:
            case PERCENTILE:
                return new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1.0f, new float[]{6.0f, 6.0f}, 0.0f);
            default:
                return new BasicStroke(3.0f);
        }
    }

    private Color getColorForStatType(Statistics statType) {
        switch (statType) {
            case MIN:
                return randomColor(1);
            case MAX:
                return randomColor(2);
            case AVERAGE:
                return randomColor(3);
            case MEDIAN:
                return randomColor(4);
            case STANDARDDEVIATION:
                return randomColor(5);
            case VARIANCE:
                return randomColor(6);
            case MAXAVERAGEDURATION:
                return randomColor(7);
            case MAXACCUMDURATION:
                return randomColor(8);
            case NDAYCUMULATIVE:
                return randomColor(9);
            case TOTAL:
                return randomColor(10);
            default:
                return randomColor(100);
        }
    }

    private Color randomColor(int i) {
        Random rand = new Random(i);
        float r = rand.nextFloat();
        float g = rand.nextFloat();
        float b = rand.nextFloat();

        return new Color(r, g, b);
    }
}
