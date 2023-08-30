package hec.ensembleview.charts;

import java.awt.*;
import java.time.ZonedDateTime;

public class LineSpec {
    int rangeAxis;
    float[] values;
    ZonedDateTime[] dateTimes;
    int lineWidth;
    float[] linePattern;
    Color lineColor;
    String lineName;

    public LineSpec(int rangeAxis, float[] values, ZonedDateTime[] dateTimes, int lineWidth,
                    Color lineColor, float[] linePattern, String lineName) {
        this.rangeAxis = rangeAxis;
        this.values = values;
        this.dateTimes = dateTimes;
        this.lineWidth = lineWidth;
        this.lineColor = lineColor;
        this.linePattern = linePattern;
        this.lineName = lineName;
    }
}
