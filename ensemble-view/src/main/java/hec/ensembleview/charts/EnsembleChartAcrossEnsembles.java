package hec.ensembleview.charts;

import hec.ensembleview.DefaultSettings;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnsembleChartAcrossEnsembles extends EnsembleChart {
    private String y2Label = "";
    private final Map<Integer, List<PointSpec>> pointSpecMap = new HashMap<>();
    private final Map<Integer, XYSeriesCollection> XYSeriesCollectionMap = new HashMap<>();

    /**
     * Ensembles Charts Across Ensembles class sets up and displays the metrics for the scatter plot chart
     */

    public void setY2Label(String label) {
        y2Label = label;
    }


    public void addPoint(PointSpec point) {
        XYSeries newMember = new XYSeries(point.pointName);
        for (int i = 0; i < point.yValue.length; i++) {
            newMember.add(i + 1d, point.yValue[i]);
        }

        if (!XYSeriesCollectionMap.containsKey(point.rangeAxis)) {
            XYSeriesCollectionMap.put(point.rangeAxis, new XYSeriesCollection());
            pointSpecMap.put(point.rangeAxis, new ArrayList<>());
        }

        XYSeriesCollectionMap.get(point.rangeAxis).addSeries(newMember);
        pointSpecMap.get(point.rangeAxis).add(point);
    }

    public void addProbPoint(PointSpec point) {
        XYSeries newMember = new XYSeries(point.pointName);
        Map<Float, Float> probValues = point.prob;

        for(Map.Entry<Float, Float> entry : probValues.entrySet()) {
            newMember.add(entry.getValue(), entry.getKey());
        }

        if (!XYSeriesCollectionMap.containsKey(point.rangeAxis)) {
            XYSeriesCollectionMap.put(point.rangeAxis, new XYSeriesCollection());
            pointSpecMap.put(point.rangeAxis, new ArrayList<>());
        }

        XYSeriesCollectionMap.get(point.rangeAxis).addSeries(newMember);
        pointSpecMap.get(point.rangeAxis).add(point);
    }

    @Override
    public ChartPanel generateChart() {
        plot = createXyPlot();
        ChartPanel chart = new ChartPanel(new JFreeChart(chartTitle, plot));
        addChartFeatures(chart);

        return chart;
    }

    private XYPlot createXyPlot() {
        plot = new XYPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        XYSeriesCollectionMap.forEach((k, v) -> {
            rendererMap.put(k, new XYLineAndShapeRenderer(false, true));
            XYLineAndShapeRenderer renderer = rendererMap.get(k);
            plot.setDataset(k, XYSeriesCollectionMap.get(k));
            plot.setRenderer(k, renderer);

            NumberAxis domainAxis = new NumberAxis(xLabel);
            domainAxis.setTickLabelFont(DefaultSettings.setSegoeFontText());
            plot.setDomainAxis(domainAxis);

            NumberAxis rangeAxis = new NumberAxis(yLabel);

            if(k == 1) {
                rangeAxis.setLabel(y2Label);
            }

            rangeAxis.setTickLabelFont(DefaultSettings.setSegoeFontText());
            plot.setRangeAxis(k, rangeAxis);

            plot.mapDatasetToDomainAxis(k, 0);
            plot.mapDatasetToRangeAxis(k, k);

            List<PointSpec> pointsForRange = pointSpecMap.get(k);
            for (int j = 0; j < pointsForRange.size(); j++) {
                PointSpec currentPoint = pointsForRange.get(j);
                plot.getRenderer(k).setSeriesStroke(j, currentPoint.lineStroke);
                if (currentPoint.pointColor != null) plot.getRenderer(k).setSeriesPaint(j, currentPoint.pointColor);
            }
        });

        return plot;
    }


    // --Commented out by Inspection START (7/23/23, 2:45 PM) - for viewing statistics:
//    public void hidePoint(String stat, int rangeAxis) {
//        List<PointSpec> pointsForRange = pointSpecMap.get(rangeAxis);
//        for (int j = 0; j < pointsForRange.size(); j++) {
//            PointSpec currentPoint = pointsForRange.get(j);
//            if(currentPoint.pointName.equalsIgnoreCase(stat)) {
//                plot.getRenderer(rangeAxis).setSeriesVisible(j, false);
//                break;
//            }
//        }
//    }
// --Commented out by Inspection STOP (7/23/23, 2:45 PM)

// --Commented out by Inspection START (7/23/23, 2:46 PM):
//    public void showPoint(String stat, int rangeAxis) {
//        if(plot == null) {
//            return;
//        }
//
//        List<PointSpec> pointsForRange = pointSpecMap.get(rangeAxis);
//        for (int j = 0; j < pointsForRange.size(); j++) {
//            PointSpec currentPoint = pointsForRange.get(j);
//            if(currentPoint.pointName.equalsIgnoreCase(stat)) {
//                plot.getRenderer(rangeAxis).setSeriesVisible(j, true);
//                break;
//            }
//        }
//    }
// --Commented out by Inspection STOP (7/23/23, 2:46 PM)
}

