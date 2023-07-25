package hec.ensembleview.charts;

import java.text.ParseException;

public interface LinePlot {
    void addLine(LineSpec line) throws ParseException;
}
