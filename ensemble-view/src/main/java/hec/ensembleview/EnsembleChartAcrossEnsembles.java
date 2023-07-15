package hec.ensembleview;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnsembleChartAcrossEnsembles implements EnsembleChart, ScatterPlot {

    private String chartTitle = "";
    private String yLabel = "";
    private String xLabel = "";
    private final Map<Integer, List<PointSpec>> pointSpecMap = new HashMap<>();
    private final Map<Integer, XYSeriesCollection> xYSeriesCollectionMap = new HashMap<>();
    private final Map<Integer, XYLineAndShapeRenderer> rendererMap = new HashMap<>();

    /**
     * Ensembles Charts Across Ensembles class sets up and displays the metrics for the scatter plot chart
     */

    public EnsembleChartAcrossEnsembles() {
    }

    @Override
    public void setTitle(String title) {chartTitle = title;
    }

    @Override
    public void setYLabel(String label) {yLabel = label;
    }

    @Override
    public void setXLabel(String label) {xLabel = label;
    }

    @Override
    public void addPoint(PointSpec point) throws ParseException {
        XYSeries newMember = new XYSeries(point.pointName);
        for (int i = 0; i < point.yValue.length; i++) {
            newMember.add((double) i + 1, point.yValue[i]);  // this adds the x value for the scatter plot.  Need to allow the value to be probability
        }

        if (!xYSeriesCollectionMap.containsKey(point.rangeAxis)) {  // range axis is the first or second y-axis
            xYSeriesCollectionMap.put(point.rangeAxis, new XYSeriesCollection());
            pointSpecMap.put(point.rangeAxis, new ArrayList<>());
        }

        xYSeriesCollectionMap.get(point.rangeAxis).addSeries(newMember);
        pointSpecMap.get(point.rangeAxis).add(point);
    }

    public void addProbPoint(PointSpec point) {  // first attempt to add probability values to chart
        XYSeries newMember = new XYSeries(point.pointName);
        Map<Float, Float> probValues = point.prob;

        for (Map.Entry<Float, Float> entry : probValues.entrySet()) {
            newMember.add(entry.getValue(), entry.getKey());
        }

        if (!xYSeriesCollectionMap.containsKey(point.rangeAxis)) {
            xYSeriesCollectionMap.put(point.rangeAxis, new XYSeriesCollection());
            pointSpecMap.put(point.rangeAxis, new ArrayList<>());
        }

        xYSeriesCollectionMap.get(point.rangeAxis).addSeries(newMember);
        pointSpecMap.get(point.rangeAxis).add(point);
    }

    @Override
    public ChartPanel generateChart() {
        XYPlot plot = createXyPlot();
        ChartPanel chart = new ChartPanel(new JFreeChart(chartTitle, plot)) {
            @Override
            public void mousePressed(MouseEvent e)
            {
                int mods = e.getModifiers();
                int panMask = InputEvent.BUTTON1_MASK;
                if (mods == InputEvent.BUTTON1_MASK+ InputEvent.SHIFT_MASK) {
                    panMask = 255; //The pan test will match nothing and the zoom rectangle will be activated.
                }try {
                    Field mask = ChartPanel.class.getDeclaredField("panMask");
                    mask.setAccessible(true);
                    mask.set(this, panMask);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                super.mousePressed(e);
            }
        };
        addChartFeatures(chart);

        return chart;
    }

    private void addChartFeatures(ChartPanel chart) {
        chart.setMouseZoomable(false);
        chart.setMouseWheelEnabled(true);
        chart.setDomainZoomable(true);
        chart.setRangeZoomable(true);

        setChartToolTip(chart);
    }

    private XYPlot createXyPlot() {
        XYPlot plot = new XYPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        xYSeriesCollectionMap.forEach((k, v) -> {
            rendererMap.put(k, new XYLineAndShapeRenderer(false, true));
            XYLineAndShapeRenderer renderer = rendererMap.get(k);
            plot.setDataset(k, xYSeriesCollectionMap.get(k));
            plot.setRenderer(k, renderer);

            NumberAxis domainAxis = new NumberAxis(xLabel);
            domainAxis.setTickLabelFont(DefaultSettings.setSegoeFontText());
            plot.setDomainAxis(domainAxis);

            NumberAxis rangeAxis = new NumberAxis(yLabel);
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

    private void setChartToolTip(ChartPanel chart) {
        XYToolTipGenerator xyToolTipGenerator = (dataset, series, item) -> {
            Number x1 = dataset.getX(series, item);
            Number y1 = dataset.getY(series, item);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(String.format("<html><p style='color:#0000ff;'>%s</p>", dataset.getSeriesKey(series)));
            stringBuilder.append(String.format("Ensemble:'%s'<br/>", x1));
            stringBuilder.append(String.format("Y:'%s'", y1.toString()));
            stringBuilder.append("</html>");
            return stringBuilder.toString();
        };

        rendererMap.forEach((k, v) -> {
            XYLineAndShapeRenderer renderer = ((XYLineAndShapeRenderer)chart.getChart().getXYPlot().getRenderer(k));
            renderer.setDefaultToolTipGenerator(xyToolTipGenerator);
        });

        chart.setDismissDelay(Integer.MAX_VALUE);

    }

}

