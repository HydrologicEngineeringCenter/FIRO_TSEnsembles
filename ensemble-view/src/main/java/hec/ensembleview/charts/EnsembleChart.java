package hec.ensembleview.charts;

import hec.ensemble.stats.Statistics;
import hec.gfx2d.ViewportLayout;
import hec.gfx2d.*;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public abstract class EnsembleChart {
    ViewportLayout view = null;
    String yLabel = "";
    String xLabel = "";
    G2dPanel plotPanel;
    PlotLayout layout = new PlotLayout();
    final ToolbarButtonProp toolbarButtonProp = new ToolbarButtonProp();
    static final String ICON = "Images/Pan.gif";

    EnsembleChart() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
    }

    public void setYLabel(String label) {
        yLabel = label;
    }

    public void setXLabel(String label) {
        xLabel = label;
    }

    Color ensembleColor() {
        return new Color(51,204,255);
    }

    double[] floatToDoubleConversion(float[] floatArray) {
        double[] doubleArray = new double[floatArray.length];
        for (int i = 0; i < floatArray.length; i++) {
            doubleArray[i] = floatArray[i];
        }
        return doubleArray;
    }

    public static Color getColorForStatType(Statistics statType) {
        switch (statType) {
            case MIN:
            case MAX:
                return randomColor(1);
            case AVERAGE:
                return randomColor(25);
            case MEDIAN:
                return randomColor(50);
            case STANDARDDEVIATION:
                return randomColor(75);
            case CUMULATIVE:
                return randomColor(125);
            case TOTAL:
                return randomColor(150);
            case PERCENTILES:
                return randomColor(200);
            default:
                return randomColor(100);
        }
    }

    static Color randomColor(int i) {
        Random rand = new Random(i);
        float r = rand.nextFloat();
        float g = rand.nextFloat();
        float b = rand.nextFloat();

        return new Color(r, g, b);
    }

    public static int getLineWidthForStatType(Statistics statType) {
        switch (statType) {
            case MIN:
            case MAX:
            case CUMULATIVE:
            case PERCENTILES:
                return 3;
            default:
                return 1;
        }
    }

    public static float[] getLinePatternForStatType(Statistics statType) {
        switch (statType) {
            case MIN:
            case MAX:
            case CUMULATIVE:
            case PERCENTILES:
                return G2dLineProperties.DOT_STYLE_PATTERN;
            default:
                return G2dLineProperties.SOLID_STYLE_PATTERN;
        }
    }

    void setPanAdapter() {
        toolbarButtonProp.adapter = PanMouseAdapter.class.getName();
        toolbarButtonProp.up = ICON;
        toolbarButtonProp.down = ICON;
        toolbarButtonProp.over = ICON;
        toolbarButtonProp.on = ICON;


        plotPanel.addTool(toolbarButtonProp);
    }

    public G2dPanel generateChart() {
        plotPanel = new G2dPanel();
        plotPanel.setIgnorePopupPlotEvents(true);

        return plotPanel;
    }
}
