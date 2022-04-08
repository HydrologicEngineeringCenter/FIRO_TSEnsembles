package hec.ensembleview;

import java.text.ParseException;

public interface ScatterPlot {
    public void addPoint(PointSpec point) throws ParseException;
}
