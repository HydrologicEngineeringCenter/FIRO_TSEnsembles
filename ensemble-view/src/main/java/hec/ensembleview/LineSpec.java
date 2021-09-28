package hec.ensembleview;

import java.awt.*;
import java.time.ZonedDateTime;

public class LineSpec {
    public float[] values;
    public ZonedDateTime[] dateTimes;
    public float lineStroke;
    public Color lineColor;
    public String lineName;

    public LineSpec(float[] values, ZonedDateTime[] dateTimes, float lineStroke,
                    Color lineColor, String lineName) {
        this.values = values;
        this.dateTimes = dateTimes;
        this.lineStroke = lineStroke;
        this.lineColor = lineColor;
        this.lineName = lineName;
    }
}
