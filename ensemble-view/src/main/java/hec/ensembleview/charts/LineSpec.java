package hec.ensembleview.charts;

import java.awt.*;
import java.time.ZonedDateTime;

public class LineSpec {
    int rangeAxis;
    float[] values;
    ZonedDateTime[] dateTimes;
    Stroke lineStroke;
    Color lineColor;
    String lineName;

    public LineSpec(int rangeAxis, float[] values, ZonedDateTime[] dateTimes, Stroke lineStroke,
                    Color lineColor, String lineName) {
        this.rangeAxis = rangeAxis;
        this.values = values;
        this.dateTimes = dateTimes;
        this.lineStroke = lineStroke;
        this.lineColor = lineColor;
        this.lineName = lineName;
    }
}
