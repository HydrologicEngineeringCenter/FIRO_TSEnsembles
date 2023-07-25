package hec.ensembleview.charts;

import java.awt.*;

public class PointSpec {
    int rangeAxis;
    float[] yValue;
    Map<Float, Float> prob;
    Stroke lineStroke;
    Color pointColor;
    String pointName;

    public PointSpec(int rangeAxis, float[] y,Stroke lineStroke,
                     Color pointColor, String pointName) {
        this.rangeAxis = rangeAxis;
        this.yValue = y;
        this.lineStroke = lineStroke;
        this.pointColor = pointColor;
        this.pointName = pointName;
    }
}
