package hec.ensembleview;

import hec.ensemble.stats.Statistics;
import hec.ensembleview.charts.*;
import hec.gfx2d.ViewportLayout;

import java.awt.*;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static hec.ensembleview.charts.EnsembleChart.getLinePatternForStatType;
import static hec.ensembleview.charts.EnsembleChartAcrossTime.getColorForStatType;
import static hec.ensembleview.charts.EnsembleChartAcrossTime.getLineWidthForStatType;

/**
 * This class checks which statistics type exists in the database and adds the line or
 * point to the Ensemble Chart
 */

public class PlotStatisticsForChartType {
    private static final Logger logger = Logger.getLogger(PlotStatisticsForChartType.class.getName());

    private PlotStatisticsForChartType() {
    }

    public static void addMetricStatisticsToTimePlot(EnsembleChartAcrossTime chart, String stat, float[] val, ZonedDateTime[] dates) {
        Color lineColor;
        int lineWidth;
        float[] linePattern;
        Statistics statType = Statistics.getStatName(stat);
        switch (statType) {
            case MIN:
            case MAX:
            case AVERAGE:
            case MEDIAN:
            case STANDARDDEVIATION:
            case PERCENTILES:
                lineColor = getColorForStatType(statType);
                lineWidth = getLineWidthForStatType(statType);
                linePattern = getLinePatternForStatType(statType);
                chart.addLine(
                        new LineSpec(0, val, dates, lineWidth, lineColor, linePattern, stat));
                break;
            default:
                logger.log(Level.INFO, "Statistic does not exist for Time plot metric compute");
                break;
        }
    }

    public static int getRangeAxis(Statistics statistic) {
        switch (statistic) {
            case MIN:
            case MAX:
            case AVERAGE:
            case MEDIAN:
            case STANDARDDEVIATION:
            case PERCENTILES:
                return 0;
            case NDAYCOMPUTABLE:
            case TOTAL:
                return 1;
            default:
                logger.log(Level.INFO, "Statistic does not exist for Ensemble plot metric compute");
        }
        return 0;
    }

    public static void addLineMembersToChart(EnsembleChart chart, float[][] vals, ZonedDateTime[] dates) {//This is for the Raw ensemble data itself
        for (int i = 0; i < vals.length; i++) {
            ((EnsembleChartAcrossTime) (chart)).addLine(new LineSpec(0, vals[i], dates,
                    getLineWidthForStatType(Statistics.NONE), randomColor(i),
                    getLinePatternForStatType(Statistics.NONE), "Member " + (i + 1)));
        }
    }

    static Color randomColor(int i) {
        Random rand = new Random(i);
        float r = rand.nextFloat();
        float g = rand.nextFloat();
        float b = rand.nextFloat();

        return new Color(r, g, b);
    }

    public static void addStatisticsToEnsemblePlot(EnsembleChartAcrossEnsembles chart, String stat, float[] val) {
        Statistics statType = Statistics.getStatName(stat);
        switch (statType) {
            case MIN:
            case MAX:
            case AVERAGE:
            case MEDIAN:
            case STANDARDDEVIATION:
            case PERCENTILES:
                chart.addPoint(
                        new PointSpec(ViewportLayout.Y1, val, getLineWidthForStatType(statType), getColorForStatType(statType), stat));
                break;
            case NDAYCOMPUTABLE:
            case TOTAL:
                chart.addPoint(
                        new PointSpec(ViewportLayout.Y2, val, getLineWidthForStatType(statType), getColorForStatType(statType), stat));
                break;
            default:
                logger.log(Level.INFO, "Statistic does not exist for Ensemble plot metric compute");
        }
    }

    // Overloading EnsemblePlot since probability requires both an probability value (x value) and the metric value (y value).
    public static void addStatisticsToEnsemblePlot(EnsembleChartAcrossEnsembles chart, String stat, Map<Float, Float> probValues) {
        Statistics statType = Statistics.getStatName(stat);
        switch (statType) {
            case MIN:
            case MAX:
            case AVERAGE:
            case MEDIAN:
            case STANDARDDEVIATION:
            case PERCENTILES:
                chart.addProbPoint(
                        new PointSpec(ViewportLayout.Y1, probValues, getLineWidthForStatType(statType), getColorForStatType(statType), stat));
                break;
            case NDAYCOMPUTABLE:
            case TOTAL:
                chart.addProbPoint(
                        new PointSpec(ViewportLayout.Y2, probValues, getLineWidthForStatType(statType), getColorForStatType(statType), stat));
                break;
            default:
                logger.log(Level.INFO, "Statistic does not exist for Probability plot metric compute");
        }
    }
}
