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
    private List<LineSpec> lines;
    private Map<Integer, TimeSeriesCollection> RangeMap = new HashMap<>();

    public EnsembleChartAcrossTime() {
        lines = new ArrayList<>();
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
        lines.add(line);
        TimeSeries newMember = new TimeSeries(line.lineName);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        for (int i = 0; i < line.values.length; i++)
        {
            Date dt = dateFormat.parse(line.dateTimes[i].toLocalDateTime().toString());
            newMember.add(new Second(dt), line.values[i]);
        }

        if (!RangeExists(line.rangeAxis))
            CreateRange(line.rangeAxis);

        AddTimeSeriesToRange(line.rangeAxis, newMember);
    }

    private void AddTimeSeriesToRange(int rangeAxis, TimeSeries newMember) {
        TimeSeriesCollection updatedCollection = RangeMap.get(rangeAxis);
        updatedCollection.addSeries(newMember);
        RangeMap.put(rangeAxis, updatedCollection);
    }

    private void CreateRange(int rangeAxis) {
        RangeMap.put(rangeAxis, new TimeSeriesCollection());
    }

    private boolean RangeExists(int rangeAxis) {
        return RangeMap.containsKey(rangeAxis);
    }

    @Override
    public ChartPanel generateChart() {
        XYPlot plot = new XYPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        for (int i = 0; i < RangeMap.size(); i++) {
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            plot.setDataset(i, RangeMap.get(i));
            plot.setRenderer(i, renderer);
            plot.setDomainAxis(new DateAxis(xLabel));
            plot.setRangeAxis(i, new NumberAxis(yLabel));
            plot.mapDatasetToDomainAxis(0, i);
            plot.mapDatasetToRangeAxis(i, i);
        }

        for (int i = 0; i < lines.size(); i++) {
            LineSpec currentLine = lines.get(i);
            plot.getRenderer().setSeriesStroke(i, currentLine.lineStroke);
            if (currentLine.lineColor != null) plot.getRenderer().setSeriesPaint(i, currentLine.lineColor);
        }
        ChartPanel chart = new ChartPanel(new JFreeChart(chartTitle, plot));
        chart.setMouseWheelEnabled(true);


        return chart;
    }

}
