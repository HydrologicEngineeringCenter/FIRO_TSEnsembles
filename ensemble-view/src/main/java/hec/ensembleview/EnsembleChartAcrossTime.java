package hec.ensembleview;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EnsembleChartAcrossTime implements EnsembleChart{

    private String chartTitle = "";
    private String yLabel = "";
    private String xLabel = "";
    private final Boolean showLegend = true;
    private final Map<Integer, List<LineSpec>> lineSpecMap = new HashMap<>();
    private final Map<Integer, TimeSeriesCollection> timeSeriesCollectionMap = new HashMap<>();
    private final Map<Integer, XYLineAndShapeRenderer> rendererMap = new HashMap<>();

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
        XYPlot plot = new XYPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        for (int i = 0; i < timeSeriesCollectionMap.size(); i++) {
            rendererMap.put(i, new XYLineAndShapeRenderer());
            XYLineAndShapeRenderer renderer = rendererMap.get(i);
            plot.setDataset(i, timeSeriesCollectionMap.get(i));
            plot.setRenderer(i, renderer);
            plot.setDomainAxis(new DateAxis(xLabel));
            plot.setRangeAxis(i, new NumberAxis(yLabel));
            plot.mapDatasetToDomainAxis(i, 0);
            plot.mapDatasetToRangeAxis(i, i);
        }

        for (int i = 0; i < lineSpecMap.size(); i++) {
            List<LineSpec> linesForRange = lineSpecMap.get(i);
            for (int j = 0; j < linesForRange.size(); j++) {
                LineSpec currentLine = linesForRange.get(j);
                plot.getRenderer(i).setSeriesStroke(j, currentLine.lineStroke);
                if (currentLine.lineColor != null) plot.getRenderer(i).setSeriesPaint(j, currentLine.lineColor);
            }
        }
        ChartPanel chart = new ChartPanel(new JFreeChart(chartTitle, plot));
        chart.setMouseWheelEnabled(true);


        return chart;
    }

}
