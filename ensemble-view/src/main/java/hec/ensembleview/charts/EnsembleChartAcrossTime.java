package hec.ensembleview.charts;

import hec.ensembleview.DefaultSettings;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
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

public class EnsembleChartAcrossTime extends EnsembleChart {
    private final Map<Integer, List<LineSpec>> lineSpecMap = new HashMap<>();
    private final Map<Integer, TimeSeriesCollection> timeSeriesCollectionMap = new HashMap<>();

    /**
     * Ensembles Charts Across Time class sets up and displays the metrics for the time series chart.
     */

    public EnsembleChartAcrossTime() {
        super();
    }

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

    //Refreshes time series ensembles to one color if metric is selected
    public void updateEnsembleLineSpec(boolean isChecked) {
        List<LineSpec> linesForRange = lineSpecMap.get(0);
        if(isChecked) {
            for (int j = 0; j < linesForRange.size(); j++) {
                LineSpec currentLine = linesForRange.get(j);
                if(currentLine.lineName.contains("Member")) {
                    currentLine.lineColor = ensembleColor();
                    plot.getRenderer().setSeriesPaint(j, currentLine.lineColor);
                }
            }
        } else {
            for (int j = 0; j < linesForRange.size(); j++) {
                LineSpec currentLine = linesForRange.get(j);
                if(currentLine.lineName.contains("Member")) {
                    currentLine.lineColor = null;
                    plot.getRenderer().setSeriesPaint(j, currentLine.lineColor);
                }
            }
        }
    }

    @Override
    public ChartPanel generateChart() {
        plot = createTimeSeriesPlot();

        ChartPanel chart = new ChartPanel(new JFreeChart(chartTitle, plot)){

            @Override
            public void mousePressed(MouseEvent e) {
                int mods = e.getModifiersEx();
                int panMask = InputEvent.BUTTON1_DOWN_MASK;
                if (mods == InputEvent.BUTTON1_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK) {
                    panMask = 255; //The pan test will match nothing and the zoom rectangle will be activated.
                }
                try {
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

    // --Commented out by Inspection START (7/23/23, 2:47 PM) - or viewing statistics:
//
//    public void hideLine(String stat) {
//        List<LineSpec> linesForRange = lineSpecMap.get(0);
//        for (int j = 0; j < linesForRange.size(); j++) {
//            LineSpec currentLine = linesForRange.get(j);
//            if (currentLine.lineName.equalsIgnoreCase(stat.toUpperCase())) {
//                plot.getRenderer(0).setSeriesVisible(j, false);
//                break;
//            } else if (currentLine.lineName.contains(stat.toUpperCase())) {
//                plot.getRenderer(0).setSeriesVisible(j, false);
//            }
//        }
//    }
// --Commented out by Inspection STOP (7/23/23, 2:47 PM)

// --Commented out by Inspection START (7/23/23, 2:48 PM):
//    public void showLine(String stat) {
//        if (plot == null) {
//            return;
//        }
//
//        List<LineSpec> linesForRange = lineSpecMap.get(0);
//        for (int j = 0; j < linesForRange.size(); j++) {
//            LineSpec currentLine = linesForRange.get(j);
//            if (currentLine.lineName.equalsIgnoreCase(stat)) {
//                plot.getRenderer(0).setSeriesVisible(j, true);
//                break;
//            } else if (currentLine.lineName.contains(stat.toUpperCase())) {
//                plot.getRenderer(0).setSeriesVisible(j, true);
//            }
//        }
//    }
// --Commented out by Inspection STOP (7/23/23, 2:48 PM)


}
