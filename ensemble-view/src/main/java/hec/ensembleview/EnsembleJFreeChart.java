package hec.ensembleview;

import org.jfree.chart.*;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.sound.sampled.Line;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EnsembleJFreeChart implements EnsembleChart{

    private JFreeChart chart;

    private String chartTitle = "";
    private String yLabel = "";
    private String xLabel = "";
    private final Boolean showLegend = true;
    private final TimeSeriesCollection members;
    private List<LineSpec> lines;

    public EnsembleJFreeChart() {
        members = new TimeSeriesCollection();
        chart = ChartFactory.createTimeSeriesChart("", "", "", members);
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
        members.addSeries(newMember);
    }

    @Override
    public ChartPanel getChart() {
        chart = ChartFactory.createTimeSeriesChart(chartTitle, xLabel, yLabel, members);
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();

        for (int i = 0; i < lines.size(); i++) {
            LineSpec currentLine = lines.get(i);
            renderer.setSeriesStroke(i, currentLine.lineStroke);
            if (currentLine.lineColor != null) renderer.setSeriesPaint(i, currentLine.lineColor);
        }

        return new ChartPanel(chart);
    }

}
