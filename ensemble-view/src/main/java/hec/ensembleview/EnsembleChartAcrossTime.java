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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EnsembleChartAcrossTime implements EnsembleChart, LinePlot {  //Remember, this is plotting for the compute stat!  Not for the raw timeseries

    private String chartTitle = ""; //adding chart title
    private String yLabel = "";  // adding ylabel
    private String xLabel = "";  // adding xlabel
    private final Boolean showLegend = true;
    private final Map<Integer, List<LineSpec>> lineSpecMap = new HashMap<>();  //hashmap that holds list of LineSpec
    private final Map<Integer, TimeSeriesCollection> timeSeriesCollectionMap = new HashMap<>();  //hashmap that holds timeseriescollection.  This contains the y1 and y2 axis
    private final Map<Integer, XYLineAndShapeRenderer> rendererMap = new HashMap<>(); //hashmap that holds xylineandshaperenderer class.  Can make line or shape visible

    public EnsembleChartAcrossTime() {
    }

    @Override
    public void setTitle(String title) {
        chartTitle = title;
    } //override method in EnsembleChart

    @Override
    public void setYLabel(String label) {
        yLabel = label;
    }  //override method in EnsembleChart

    @Override
    public void setXLabel(String label) {
        xLabel = label;
    }  //override method in EnsembleChart

    @Override
    public void addLine(LineSpec line) throws ParseException {  //override method in LinePlot with Linespec as parameter.  LineSpec defines the specifications of the line.  This method is creating the line to be plotted and adding to timeseriescollection
        TimeSeries newMember = new TimeSeries(line.lineName);  // instantiate new TimeSeries class passing lineName to timeseries.  pass time series values to this object. ensures timesteps are consistent
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");  // date format
        for (int i = 0; i < line.values.length; i++)  // for statement, values length is the length of the time series
        {
            Date dt = dateFormat.parse(line.dateTimes[i].toLocalDateTime().toString());  //getting the datetime and formating the time
            newMember.add(new Second(dt), line.values[i]); //attaching time to values and adding to timeseries object
        }

        if (!timeSeriesCollectionMap.containsKey(line.rangeAxis)) { //rangeAxis is the different y axis specified as 0 or 1. If neither value exists
            timeSeriesCollectionMap.put(line.rangeAxis, new TimeSeriesCollection()); //if neither value exist, put on primary y axis?  Ask Neema
            lineSpecMap.put(line.rangeAxis, new ArrayList<>());
        }

        timeSeriesCollectionMap.get(line.rangeAxis).addSeries(newMember);  //get the rangeAxis (o or 1) and add timeseries to collection map
        lineSpecMap.get(line.rangeAxis).add(line);  //get the lineSpec key and add LineSpec to list of LineSpec.  So each line gets its own line spec
    }

    @Override
    public ChartPanel generateChart() {  // method to generateChart, returns a Chart Panel, displays a JFreeChart Object
        XYPlot plot = new XYPlot(); // new instance of XYPlot object, general class for plotting x, y pairs
        plot.setDomainPannable(true); //allow the chart to zoom
        plot.setRangePannable(true);  //allow the chart to zoom

        for (int i = 0; i < timeSeriesCollectionMap.size(); i++) {  // for statement, iterates through timeseriescollection map which is for either y1 or y2
            rendererMap.put(i, new XYLineAndShapeRenderer());  //puts a new instance of XYLineandShapeRenderer object to renderer Map.
            XYLineAndShapeRenderer renderer = rendererMap.get(i); //gets the XYLineAndShapeRenderer object just created and assign to variable renderer
            plot.setDataset(i, timeSeriesCollectionMap.get(i));  // set the timeseries stat from the collection map (0 or 1) to the XYplot
            plot.setRenderer(i, renderer);  //add renderer to the plot which displays the shapes and lines
            plot.setDomainAxis(new DateAxis(xLabel)); //sets the x axis as the date and gives it label
            plot.setRangeAxis(i, new NumberAxis(yLabel));  //sets the y axis as a numberaxis and give it a label
            plot.mapDatasetToDomainAxis(i, 0);  //maps the dataset to the x axis, needed because of the y1 and y2 but x axis is always the same
            plot.mapDatasetToRangeAxis(i, i); // map dataset to y axis, y1 or y2

            List<LineSpec> linesForRange = lineSpecMap.get(i);  //get list of linespecs for y1 or y2 axis. Setting the line specifications
            for (int j = 0; j < linesForRange.size(); j++) { // for loop. Loop through list of lineSpecs
                LineSpec currentLine = linesForRange.get(j);  // get one LineSpec
                plot.getRenderer(i).setSeriesStroke(j, currentLine.lineStroke);  //set the series stroke for the line
                if (currentLine.lineColor != null) plot.getRenderer(i).setSeriesPaint(j, currentLine.lineColor);  // set line color
            }
        }

        ChartPanel chart = new ChartPanel(new JFreeChart(chartTitle, plot));  // create a new instance of the chart panel.  As a parameter, JFreeChart class is instantiated and passed with chart title and created plot
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
