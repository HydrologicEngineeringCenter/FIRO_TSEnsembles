package hec.ensembleview.charts;

import java.text.ParseException;

public interface ScatterPlot {
    void addPoint(PointSpec point) throws ParseException;
}
