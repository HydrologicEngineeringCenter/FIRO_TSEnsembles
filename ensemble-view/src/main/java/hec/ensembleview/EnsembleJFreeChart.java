package hec.ensembleview;

import org.jfree.chart.*;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;

public class EnsembleJFreeChart implements EnsembleChart{

    private JFreeChart chart;

    private String chartTitle = "";
    private String yLabel = "";
    private String xLabel = "";
    private Boolean showLegend = true;
    private TimeSeriesCollection members;

    public EnsembleJFreeChart() {
        members = new TimeSeriesCollection();
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
    public void addLine(float[] values, ZonedDateTime[] dateTimes, String name) throws ParseException {
        TimeSeries newMember = new TimeSeries(name);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        for (int i = 0; i < values.length; i++)
        {
            Date dt = dateFormat.parse(dateTimes[i].toLocalDateTime().toString());
            newMember.add(new Second(dt), values[i]);
        }
        members.addSeries(newMember);
    }

    @Override
    public ChartPanel getChart() {
        chart = ChartFactory.createTimeSeriesChart(chartTitle, xLabel, yLabel, members);
        return new ChartPanel(chart);
    }
}
