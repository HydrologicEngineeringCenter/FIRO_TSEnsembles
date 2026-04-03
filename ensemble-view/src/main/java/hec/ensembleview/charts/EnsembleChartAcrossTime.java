package hec.ensembleview.charts;

import hec.gfx2d.G2dLineProperties;
import hec.gfx2d.G2dPanel;
import hec.gfx2d.TimeSeriesDataSet;
import hec.gfx2d.ViewportLayout;
import hec.heclib.util.HecTime;
import hec.heclib.util.HecTimeArray;
import hec.io.TimeSeriesContainer;

import java.awt.Color;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class EnsembleChartAcrossTime extends EnsembleChart {
    private final List<TimeSeriesDataSet> timeSeriesDataSetList = new ArrayList<>();
    private final List<G2dLineProperties> g2dLinePropertiesList = new ArrayList<>();
    private final List<String> rangeAxisList = new ArrayList<>();
    private int memberCount = 0;
    private String y2Label = "";
    private static final int LEGEND_MEMBER_LIMIT = 60;
    private final List<double[][]> crosshairSeriesList = new ArrayList<>();
    private final List<String> crosshairSeriesNames = new ArrayList<>();


    /**
     * Ensembles Charts Across Time class sets up and displays the metrics for the time series chart.
     */

    public EnsembleChartAcrossTime() {
        super();
    }

    /**
     * Records the number of ensemble members. If over the limit,
     * the legend only shows computed metric lines, not individual members.
     */
    public void setY2Label(String label) {
        y2Label = label;
        if (view != null) {
            view.setAxisLabel(ViewportLayout.Y2, label);
        }
    }

    public void setMemberCount(int count) {
        this.memberCount = count;
        layout.setHasLegend(count <= LEGEND_MEMBER_LIMIT);
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

        // Store raw data for crosshair snap lookups
        double[] xTimes = new double[length];
        HecTime ht = convertZdtToHecTime(line);
        for (int i = 0; i < length; i++) {
            xTimes[i] = ht.value();
            ht.addMinutes((int)duration.toMinutes());
        }
        crosshairSeriesList.add(new double[][]{xTimes, values});
        crosshairSeriesNames.add(line.lineName);

        // Create Properties
        G2dLineProperties props = new G2dLineProperties();
        setG2dLineProperties(props, line);

        String axis = (line.rangeAxis == 0) ? ViewportLayout.Y1 : ViewportLayout.Y2;
        rangeAxisList.add(axis);

        if(view != null) {
            view.addCurve(axis, timeSeriesDataSet, props);
            plotPanel.buildComponents(layout);
            setPanAdapter();
            setupCrosshair();
            populateCrosshairData();
            setMouseWheelScroll();
        }
    }

    private HecTime convertZdtToHecTime(LineSpec line) {
        ZonedDateTime startDate = line.dateTimes[0];
        int year = startDate.getYear();
        String month = startDate.getMonth().toString();
        int day = startDate.getDayOfMonth();
        String hour = String.valueOf(startDate.getHour());
        // HecTime requires two digits for minutes
        String minutes = String.valueOf(String.format("%02d", startDate.getMinute()));
        return new HecTime(day + month + year, hour + minutes);
    }

    private void setG2dLineProperties(G2dLineProperties props, LineSpec lineSpec) {
        props.setLineWidth(lineSpec.lineWidth);
        props.setLinePattern(lineSpec.linePattern);
        props.setName(lineSpec.lineName);
        // Hide member lines from legend when there are too many ensembles
        if (memberCount > LEGEND_MEMBER_LIMIT && lineSpec.lineName.contains("Member")) {
            props.setLabel("");
        } else {
            props.setLabel(lineSpec.lineName);
        }
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
            if (!y2Label.isEmpty()) {
                view.setAxisLabel(ViewportLayout.Y2, y2Label);
            }
        }

        buildViewPortGraph();

        plotPanel.buildComponents(layout);
        setPanAdapter();
        setupCrosshair();
        populateCrosshairData();
        setMouseWheelScroll();

        return plotPanel;
    }

    private void populateCrosshairData() {
        if (crosshairAdapter == null) {
            return;
        }
        crosshairAdapter.clearSeries();
        for (int i = 0; i < crosshairSeriesList.size(); i++) {
            double[][] series = crosshairSeriesList.get(i);
            String name = crosshairSeriesNames.get(i);
            crosshairAdapter.addSeries(name, series[0], series[1]);
        }
    }

    /**
     * Highlights a specific ensemble member line by making it bold and red,
     * while dimming all other member lines to the ensemble color (matching
     * the behavior when a metric is computed in the Time Series tab).
     * @param memberIndex 0-based index of the ensemble member to highlight
     */
    private static final Color[] HIGHLIGHT_COLORS = {
            new Color(255, 0, 0),       // Red
            new Color(0, 0, 255),       // Blue
            new Color(0, 180, 0),       // Green
            new Color(255, 140, 0),     // Orange
            new Color(148, 0, 211),     // Violet
            new Color(0, 180, 180),     // Teal
            new Color(200, 0, 100),     // Magenta
            new Color(128, 128, 0),     // Olive
            new Color(255, 20, 147),    // Deep Pink
            new Color(0, 100, 200),     // Steel Blue
    };

    /**
     * Highlights specific ensemble member lines with distinct colors,
     * while dimming all other member lines.
     * @param memberIndices 0-based indices of the ensemble members to highlight, in order
     */
    public void highlightMembers(List<Integer> memberIndices) {
        if (view == null) return;

        // Map each member index to its highlight color
        java.util.Map<Integer, Color> colorMap = new java.util.LinkedHashMap<>();
        for (int i = 0; i < memberIndices.size(); i++) {
            int idx = memberIndices.get(i);
            if (!colorMap.containsKey(idx)) {
                colorMap.put(idx, HIGHLIGHT_COLORS[colorMap.size() % HIGHLIGHT_COLORS.length]);
            }
        }

        List<G2dLineProperties> list = view.getY1DataProperties(ViewportLayout.X1);
        for (G2dLineProperties lineProperties : list) {
            if (lineProperties.getName().contains("Member")) {
                String numStr = lineProperties.getName().replace("Member ", "");
                try {
                    int memberNum = Integer.parseInt(numStr);
                    Color highlight = colorMap.get(memberNum - 1);
                    if (highlight != null) {
                        lineProperties._lineColor = highlight;
                        lineProperties._lineTransparency = 0;
                        lineProperties._lineWidth = 3;
                    } else {
                        lineProperties._lineColor = ensembleColor();
                        lineProperties._lineTransparency = 80;
                        lineProperties._lineWidth = 1;
                    }
                } catch (NumberFormatException ignored) {
                    // Not a member line
                }
            }
        }
        plotPanel.buildComponents(layout);
    }

    private void buildViewPortGraph() {
        for (int i = 0; i < timeSeriesDataSetList.size(); i++) {
            String axis = rangeAxisList.get(i);
            view.addCurve(axis, timeSeriesDataSetList.get(i), g2dLinePropertiesList.get(i));
        }
    }
}
