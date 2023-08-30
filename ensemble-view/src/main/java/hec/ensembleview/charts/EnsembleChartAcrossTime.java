package hec.ensembleview.charts;

import hec.gfx2d.*;
import hec.heclib.util.HecTime;
import hec.heclib.util.HecTimeArray;
import hec.io.TimeSeriesContainer;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class EnsembleChartAcrossTime extends EnsembleChart {
    private final List<TimeSeriesDataSet> timeSeriesDataSetList = new ArrayList<>();
    private final List<G2dLineProperties> g2dLinePropertiesList = new ArrayList<>();


    /**
     * Ensembles Charts Across Time class sets up and displays the metrics for the time series chart.
     */

    public EnsembleChartAcrossTime() {
        super();
        layout.setHasLegend(true);
    }

    public void addLine(LineSpec line) {
        int length = line.dateTimes.length;
        Duration duration = Duration.between(line.dateTimes[0], line.dateTimes[1]);
        HecTime startTime = convertZdtToHecTime(line);

        HecTimeArray timeArray = new HecTimeArray(length);
        for (int i = 0; i < length; i++) {
            timeArray.set(i, startTime);
            startTime.addMinutes((int)duration.toMinutes());
        }

        // Create TimeSeriesContainer and set Time and Values
        TimeSeriesContainer timeSeriesContainer = new TimeSeriesContainer();
        double[] values = floatToDoubleConversion(line.values);
        timeSeriesContainer.setValues(values);
        timeSeriesContainer.setTimes(timeArray);
        timeSeriesContainer.setName(line.lineName);

        // Create TimeSeriesDataSet and add to arraylist
        TimeSeriesDataSet timeSeriesDataSet = new TimeSeriesDataSet(timeSeriesContainer);
        timeSeriesDataSet.setName(line.lineName);
        timeSeriesDataSetList.add(timeSeriesDataSet);

        // Create Properties
        G2dLineProperties props = new G2dLineProperties();
        setG2dLineProperties(props, line);

        if(view != null) {
            view.addCurve(ViewportLayout.Y1, timeSeriesDataSet, props);
            plotPanel.buildComponents(layout);
        }
    }

    private HecTime convertZdtToHecTime(LineSpec line) {
        ZonedDateTime startDate = line.dateTimes[0];
        int year = startDate.getYear();
        String month = startDate.getMonth().toString();
        int day = startDate.getDayOfMonth();
        String hour = String.valueOf(startDate.getHour());
        String minutes = String.valueOf(startDate.getMinute());

        return new HecTime(day + month + year, hour + minutes);
    }

    private void setG2dLineProperties(G2dLineProperties props, LineSpec lineSpec) {
        props.setLineWidth(lineSpec.lineWidth);
        props.setLinePattern(lineSpec.linePattern);
        props.setName(lineSpec.lineName);
        props.setLabel(lineSpec.lineName);
        props.setLineColor(lineSpec.lineColor);
        props.setDrawLine(true);
        props.setDrawPoints(false);
        g2dLinePropertiesList.add(props);
    }

    //Refreshes time series ensembles to one color if metric is selected
    public void updateEnsembleLineSpec(boolean isChecked) {
        List<G2dLineProperties> list = view.getY1DataProperties(ViewportLayout.X1);
        if (isChecked) {
            for (G2dLineProperties lineProperties : list) {
                if (lineProperties.getName().contains("Member")) {
                    lineProperties._lineColor = ensembleColor();
                    lineProperties._lineTransparency = 80;
                }
            }
        } else {
            for (G2dLineProperties lineProperties : list) {
                if (lineProperties.getName().contains("Member")) {
                    lineProperties._lineColor = randomColor(1);
                }
            }
        }
    }

    @Override
    public G2dPanel generateChart() {
        super.generateChart();
        if(view == null) {
            view = layout.addViewport(1.0);
            view.setAxisLabel(ViewportLayout.Y1, yLabel);
            view.setAxisLabel(ViewportLayout.X1, xLabel);
        }

        buildViewPortGraph();
        plotPanel.buildComponents(layout);

        return plotPanel;
    }

    private void buildViewPortGraph() {
        for (int i = 0; i < timeSeriesDataSetList.size(); i++) {
            view.addCurve(ViewportLayout.Y1, timeSeriesDataSetList.get(i), g2dLinePropertiesList.get(i));
        }
    }
}
