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
    int[] memberIndices;

    public PointSpec(String rangeAxis, float[] y, int lineWidth,
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

    /**
     * Sets the 1-based member indices corresponding to each data point.
     * For probability plots, this tracks which ensemble member each sorted point came from.
     */
    public void setMemberIndices(int[] memberIndices) {
        this.memberIndices = memberIndices;
    }
}
