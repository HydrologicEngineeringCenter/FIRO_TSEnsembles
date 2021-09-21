package hec.ensembleview;

import java.text.ParseException;
import java.time.ZonedDateTime;

/**
 * Interface for basic ensemble charting functions.
 */
public interface EnsembleChart {
    void setTitle(String title);
    void setYLabel(String label);
    void setXLabel(String label);
    void addLine(float[] values, ZonedDateTime[] dateTimes, String name) throws ParseException;
    void showPlot();

}
