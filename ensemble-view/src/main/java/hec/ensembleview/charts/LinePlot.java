package hec.ensembleview.charts;

import java.text.ParseException;

public interface LinePlot {
    public void addLine(LineSpec line) throws ParseException;
}
