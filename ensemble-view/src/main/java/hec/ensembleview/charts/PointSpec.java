package hec.ensembleview.charts;

import java.awt.*;
import java.util.Map;

public class PointSpec {
    String rangeAxis;
    float[] yValue;
    Map<Float, Float> prob;
    int lineWidth;
    Color pointColor;
    String pointName;

    public PointSpec(String rangeAxis, float[] y,int lineWidth,
                     Color pointColor, String pointName) {
        this.rangeAxis = rangeAxis;
        this.yValue = y;
        this.lineWidth = lineWidth;
        this.pointColor = pointColor;
        this.pointName = pointName;
    }

    public PointSpec(String rangeAxis, Map<Float, Float> prob, int lineWidth,
                     Color pointColor, String pointName) {
        this.rangeAxis = rangeAxis;
        this.prob = prob;
        this.lineWidth = lineWidth;
        this.pointColor = pointColor;
        this.pointName = pointName;
    }
}
