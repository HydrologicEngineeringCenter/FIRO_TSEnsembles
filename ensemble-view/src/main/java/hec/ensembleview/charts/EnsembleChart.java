package hec.ensembleview.charts;
import org.jfree.chart.ChartPanel;

/**
 * Interface for basic ensemble charting functions.
 */
public interface EnsembleChart {
    void setYLabel(String label);
    void setXLabel(String label);
    ChartPanel generateChart();


}
