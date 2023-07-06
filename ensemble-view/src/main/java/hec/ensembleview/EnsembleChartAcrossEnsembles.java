package hec.ensembleview;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import java.text.ParseException;
import java.util.*;
import java.util.List;

public class EnsembleChartAcrossEnsembles implements EnsembleChart, ScatterPlot {

    private String chartTitle = "";
    private String yLabel = "";
    private String xLabel = "";
    private final Boolean showLegend = true;
    private final Map<Integer, List<PointSpec>> pointSpecMap = new HashMap<>();
    private final Map<Integer, XYSeriesCollection> XYSeriesCollectionMap = new HashMap<>();
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
            newMember.add(i + 1, point.yValue[i]);
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
        XYPlot plot = new XYPlot();
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
            rangeAxis.setTickLabelFont(DefaultSettings.setSegoeFontText());
            plot.setRangeAxis(k, rangeAxis);

            plot.mapDatasetToDomainAxis(k, 0);
            plot.mapDatasetToRangeAxis(k, k);

            List<PointSpec> pointsForRange = pointSpecMap.get(k);
            for(int j = 0; j < pointsForRange.size(); j++) {
                PointSpec currentPoint = pointsForRange.get(j);
                plot.getRenderer(k).setSeriesStroke(j, currentPoint.lineStroke);
                if(currentPoint.pointColor != null) plot.getRenderer(k).setSeriesPaint(j, currentPoint.pointColor);
            }
        });

        ChartPanel chart = new ChartPanel(new JFreeChart(chartTitle, plot));
        chart.setMouseWheelEnabled(true);
        setChartToolTip(chart);
        return chart;
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

