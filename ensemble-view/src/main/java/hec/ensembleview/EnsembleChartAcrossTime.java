package hec.ensembleview;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EnsembleChartAcrossTime implements EnsembleChart, LinePlot {

    private String chartTitle = "";
    private String yLabel = "";
    private String xLabel = "";
    private final Boolean showLegend = true;
    private final Map<Integer, List<LineSpec>> lineSpecMap = new HashMap<>();
    private final Map<Integer, TimeSeriesCollection> timeSeriesCollectionMap = new HashMap<>();
    private final Map<Integer, XYLineAndShapeRenderer> rendererMap = new HashMap<>();

    /**
     * Ensembles Charts Across Time class sets up and displays the metrics for the time series chart.
     */

    public EnsembleChartAcrossTime() {
    }

    @Override
    public void setTitle(String title) {
        chartTitle = title;
    }

    @Override
    public void setYLabel(String label) {
        yLabel = label;
    }

    @Override
    public void setXLabel(String label) {
        xLabel = label;
    }

    @Override
    public void addLine(LineSpec line) throws ParseException {
        TimeSeries newMember = new TimeSeries(line.lineName);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        for (int i = 0; i < line.values.length; i++)
        {
            Date dt = dateFormat.parse(line.dateTimes[i].toLocalDateTime().toString());
            newMember.add(new Second(dt), line.values[i]);
        }

        if (!timeSeriesCollectionMap.containsKey(line.rangeAxis)) {
            timeSeriesCollectionMap.put(line.rangeAxis, new TimeSeriesCollection());
            lineSpecMap.put(line.rangeAxis, new ArrayList<>());
        }

        timeSeriesCollectionMap.get(line.rangeAxis).addSeries(newMember);
        lineSpecMap.get(line.rangeAxis).add(line);
    }

    @Override
    public ChartPanel generateChart() {
        XYPlot plot = createTimeSeriesPlot();

        ChartPanel chart = new ChartPanel(new JFreeChart(chartTitle, plot)){
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

    private XYPlot createTimeSeriesPlot() {
        XYPlot plot = new XYPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        timeSeriesCollectionMap.forEach((k, v) -> {
            rendererMap.put(k, new XYLineAndShapeRenderer(true, false));
            XYLineAndShapeRenderer renderer = rendererMap.get(k);
            plot.setDataset(k, timeSeriesCollectionMap.get(k));
            plot.setRenderer(k, renderer);

            DateAxis domainAxis = new DateAxis(xLabel);
            domainAxis.setTickLabelFont(DefaultSettings.setSegoeFontText());
            plot.setDomainAxis(domainAxis);

            NumberAxis rangeAxis = new NumberAxis(yLabel);
            rangeAxis.setTickLabelFont(DefaultSettings.setSegoeFontText());
            plot.setRangeAxis(k, rangeAxis);

            plot.mapDatasetToDomainAxis(k, 0);
            plot.mapDatasetToRangeAxis(k, k);

            List<LineSpec> linesForRange = lineSpecMap.get(k);
            for (int j = 0; j < linesForRange.size(); j++) {
                LineSpec currentLine = linesForRange.get(j);
                plot.getRenderer(k).setSeriesStroke(j, currentLine.lineStroke);
                if (currentLine.lineColor != null) plot.getRenderer(k).setSeriesPaint(j, currentLine.lineColor);
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

}
