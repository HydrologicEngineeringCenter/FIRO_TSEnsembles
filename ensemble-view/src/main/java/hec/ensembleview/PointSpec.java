package hec.ensembleview;

import hec.ensemble.Ensemble;

import java.awt.*;

public class PointSpec {
    int rangeAxis;
    public float[] yValue;
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
}
