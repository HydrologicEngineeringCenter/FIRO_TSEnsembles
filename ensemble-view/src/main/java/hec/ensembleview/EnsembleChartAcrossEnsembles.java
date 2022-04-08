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

    public EnsembleChartAcrossEnsembles(String title) {
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
        for(int i = 0; i < point.yValue.length;i++) {
            newMember.add(i, point.yValue[i]);
        }

        if(!XYSeriesCollectionMap.containsKey(point.rangeAxis)) {
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

        for(int i = 0; i< XYSeriesCollectionMap.size(); i++) {
            rendererMap.put(i, new XYLineAndShapeRenderer(false, true));
            XYLineAndShapeRenderer renderer = rendererMap.get(i);
            plot.setDataset(i, XYSeriesCollectionMap.get(i));
            plot.setRenderer(i, renderer);
            plot.setDomainAxis(new NumberAxis(xLabel));
            plot.setRangeAxis(new NumberAxis(yLabel));
            plot.mapDatasetToDomainAxis(i, 0);
            plot.mapDatasetToRangeAxis(i, i);

            List<PointSpec> pointsForRange = pointSpecMap.get(i);
            for(int j = 0; j < pointsForRange.size(); j++) {
                PointSpec currentPoint = pointsForRange.get(i);
                plot.getRenderer(i).setSeriesStroke(j, currentPoint.lineStroke);
                if(currentPoint.pointColor != null) plot.getRenderer(i).setSeriesPaint(j, currentPoint.pointColor);
            }
        }
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
            stringBuilder.append(String.format("X:'%s'<br/>", new Date(x1.longValue())));
            stringBuilder.append(String.format("Y:'%s'", y1.toString()));
            stringBuilder.append("</html>");
            return stringBuilder.toString();
        };


        for (int i = 0; i < rendererMap.size(); i++) {
            XYLineAndShapeRenderer renderer = ((XYLineAndShapeRenderer)chart.getChart().getXYPlot().getRenderer(i));
            renderer.setDefaultToolTipGenerator(xyToolTipGenerator);
        }

        chart.setDismissDelay(Integer.MAX_VALUE);

    }

}

