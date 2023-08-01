package hec.ensembleview.charts;

import hec.ensemble.stats.Statistics;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class EnsembleChart {
    private final String chartTitle = "";
    protected String yLabel = "";
    protected String xLabel = "";
    protected final Map<Integer, XYLineAndShapeRenderer> rendererMap = new HashMap<>();
    protected XYPlot plot;

    protected EnsembleChart() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
    }

    public void setYLabel(String label) {
        yLabel = label;
    }


    public void setXLabel(String label) {
        xLabel = label;
    }

    protected Color ensembleColor() {
        Color c;
        c = Color.blue;
        int alpha = 8;
        int cInt = (c.getRGB() & 0xffffff) | (alpha << 24);
        c = new Color(cInt, true);
        return c;
    }

    private void addChartFeatures(ChartPanel chart) {
        chart.setMouseZoomable(false);
        chart.setMouseWheelEnabled(true);
        chart.setDomainZoomable(true);
        chart.setRangeZoomable(true);

        setChartToolTip(chart);
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


        rendererMap.forEach((k, v) -> {
            XYLineAndShapeRenderer renderer = ((XYLineAndShapeRenderer)chart.getChart().getXYPlot().getRenderer(k));
            renderer.setDefaultToolTipGenerator(xyToolTipGenerator);
        });

        chart.setDismissDelay(Integer.MAX_VALUE);
    }

    public static Color getColorForStatType(Statistics statType) {
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
            case CUMULATIVE:
                return randomColor(6);
            case TOTAL:
                return randomColor(10);
            case PERCENTILES:
                return randomColor(11);
            default:
                return randomColor(100);
        }
    }

    private static Color randomColor(int i) {
        Random rand = new Random(i);
        float r = rand.nextFloat();
        float g = rand.nextFloat();
        float b = rand.nextFloat();

        return new Color(r, g, b);
    }

    public static BasicStroke getStrokeForStatType(Statistics statType) {
        switch (statType) {
            case MIN:
            case MAX:
            case CUMULATIVE:
            case PERCENTILES:
                return new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1.0f, new float[]{6.0f, 6.0f}, 0.0f);
            default:
                return new BasicStroke(3.0f);
        }
    }

    public ChartPanel generateChart() {
        ChartPanel chart = new ChartPanel(new JFreeChart(chartTitle, plot));
        addChartFeatures(chart);

        return chart;
    }
}
