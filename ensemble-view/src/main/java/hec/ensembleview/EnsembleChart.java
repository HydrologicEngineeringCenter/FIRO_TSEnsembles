package hec.ensembleview;

import org.jfree.chart.ChartPanel;

import java.text.ParseException;
import java.time.ZonedDateTime;

/**
 * Interface for basic ensemble charting functions.
 */
public interface EnsembleChart {
    void setTitle(String title);
    void setYLabel(String label);
    void setXLabel(String label);
    ChartPanel generateChart();
    void addLine(LineSpec line) throws ParseException;

}
