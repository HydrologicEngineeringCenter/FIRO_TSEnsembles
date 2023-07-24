package hec.ensembleview;

import hec.ensemble.stats.Statistics;
import hec.ensembleview.charts.*;

import java.awt.*;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static hec.ensembleview.charts.EnsembleChartAcrossTime.getColorForStatType;
import static hec.ensembleview.charts.EnsembleChartAcrossTime.getStrokeForStatType;

/**
 * This class checks which statistics type exists in the database and adds the line or
 * point to the Ensemble Chart
 */

public class PlotStatisticsForChartType {
    private static final Logger logger = Logger.getLogger(PlotStatisticsForChartType.class.getName());

    private PlotStatisticsForChartType() {
    }

    public static void addMetricStatisticsToTimePlot(EnsembleChartAcrossTime chart, String stat, float[] val, ZonedDateTime[] dates) throws ParseException {
        Color lineColor;
        BasicStroke stroke;

        Statistics statType = Statistics.getStatName(stat);
        switch (statType) {
            case MIN:
            case MAX:
            case AVERAGE:
            case MEDIAN:
            case STANDARDDEVIATION:
            case PERCENTILES:
                lineColor = getColorForStatType(statType);
                stroke = getStrokeForStatType(statType);
                chart.addLine(
                        new LineSpec(0, val, dates, stroke, lineColor, stat));
                break;
            default:
                logger.log(Level.INFO, "Statistic does not exist for Time plot metric compute");
                break;
        }
    }

    public static void addLineMembersToChart(EnsembleChart chart, float[][] vals, ZonedDateTime[] dates) throws ParseException {
        Color c = null;  //This is for the Raw ensemble data itself
        for (int i = 0; i < vals.length; i++) {
            ((EnsembleChartAcrossTime) (chart)).addLine(new LineSpec(0, vals[i], dates, new BasicStroke(1.0f), c, "Member " + (i + 1)));
        }
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
                        new PointSpec(0, val, getStrokeForStatType(statType), getColorForStatType(statType), stat));
                break;
            case CUMULATIVE:
            case TOTAL:
                chart.addPoint(
                        new PointSpec(1, val, getStrokeForStatType(statType), getColorForStatType(statType), stat));
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
                        new PointSpec(0, probValues, getStrokeForStatType(statType), getColorForStatType(statType), stat));
                break;
            case CUMULATIVE:
            case TOTAL:
                chart.addProbPoint(
                        new PointSpec(1, probValues, getStrokeForStatType(statType), getColorForStatType(statType), stat));
                break;
            default:
                logger.log(Level.INFO, "Statistic does not exist for Probability plot metric compute");
        }
    }
}
