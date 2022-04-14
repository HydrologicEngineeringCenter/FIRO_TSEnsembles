package hec.ensembleview;

import java.text.ParseException;

public interface LinePlot {
    public void addLine(LineSpec line) throws ParseException;
}
