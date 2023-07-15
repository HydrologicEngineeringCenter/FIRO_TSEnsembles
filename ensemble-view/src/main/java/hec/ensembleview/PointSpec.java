package hec.ensembleview;

import java.awt.*;
import java.util.Map;

public class PointSpec {
    int rangeAxis;
    public float[] yValue;
    public Map<Float, Float> prob;
    public Stroke lineStroke;
    public Color pointColor;
    public String pointName;

    public PointSpec(int rangeAxis, float[] y,Stroke lineStroke,
                     Color pointColor, String pointName) {
        this.rangeAxis = rangeAxis;
        this.yValue = y;
        this.lineStroke = lineStroke;
        this.pointColor = pointColor;
        this.pointName = pointName;
    }

    public PointSpec(int rangeAxis, Map<Float, Float> prob, Stroke lineStroke,
                     Color pointColor, String pointName) {
        this.rangeAxis = rangeAxis;
        this.prob = prob;
        this.lineStroke = lineStroke;
        this.pointColor = pointColor;
        this.pointName = pointName;
    }
}
