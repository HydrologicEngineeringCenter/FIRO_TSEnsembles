package hec.ensembleview.charts;

import hec.ensemble.stats.Statistics;
import hec.geometry.Axis;
import hec.geometry.Scale;
import hec.gfx2d.ViewportLayout;
import hec.gfx2d.*;
import hec.map.LocalPt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;
import java.util.Random;

public abstract class EnsembleChart implements MouseWheelListener {
    ViewportLayout view = null;
    String yLabel = "";
    String xLabel = "";
    G2dPanel plotPanel;
    PlotLayout layout = new PlotLayout();
    final ToolbarButtonProp toolbarButtonProp = new ToolbarButtonProp();
    static final String ICON = "Images/Pan.gif";
    private Point lastPoint;
    transient G2dPointerComponent pointerComponent;

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

    void setMouseWheelScroll() {
        if(plotPanel.getViewports().length > 0) {
            plotPanel.getViewports()[0].addMouseWheelListener(this);
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

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getScrollAmount() != 0) {
            mouseWheelZoom(e);
        }
    }

    private void mouseWheelZoom(MouseWheelEvent e) {
        int direction = e.getWheelRotation() < 0 ? -1 : 1;
        Point p = e.getPoint();
        double zf;
        if (!p.equals(lastPoint)) {
            lastPoint = p;
        }
        if (direction == 1) {
            zf = 1.105;
        } else {
            zf = 0.905;
        }

        LocalPt lpt = new LocalPt(p.x, p.y);
        pointerComponent = plotPanel.getViewports()[0];

        Viewport v = (Viewport) pointerComponent;
        List<Scale> scaleVector = v.getScaleVector();
        for (Scale scl : scaleVector) {
            Axis xaxis = scl.getAxis(Axis.XAXIS);
            Axis yaxis = scl.getAxis(Axis.YAXIS);
            final double x = scl.x2e(lpt.x);
            final double y = scl.y2n(lpt.y);

            xaxis.zoomByFactor(zf);
            yaxis.zoomByFactor(zf);


            //find out where our mouse point world location now is after the zoom and pan back in order to
            //have the zoom stay centered on this location
            double localNewX = scl.e2x(x);
            double localNewY = scl.n2y(y);
            //compute a drag vector
            //dxl, dyl are delta X,Y in local coordinates ( pixels )
            double dxl = localNewX - p.x;
            double dyl = localNewY - p.y;   //positive y change is a mouse move down because 0,0 is in the upper left N,N is in the lower right

            if (dxl != 0) {
                panAxis(v.getAxis("x1"), dxl);
                panAxis(v.getAxis("x2"), dxl);
            }

            if (dyl != 0) {
                panAxis(v.getAxis("y1"), dyl);
                panAxis(v.getAxis("y2"), dyl);
            }
        }
        v.getG2dPanel().repaint();
    }

    private void panAxis(Axis a, double dal) {

        if (a == null || a.getZoom() == 1) {
            return;
        }
        double vMaxw = a.getViewMax();
        double vMinw = a.getViewMin();

        double vMaxl = a.w2l(vMaxw);
        double vMinl = a.w2l(vMinw);

        double nMaxl = vMaxl + dal;  //new view maximum in local (pixel) coords
        double nMinl = vMinl + dal;  //new view minimum in local (pixel) coords

        double nMaxw = a.l2w((int) nMaxl); //new max world
        double nMinw = a.l2w((int) nMinl); //new min world

        if (nMaxw > a.getMax()) {
            //view range local
            double viewRangeLocal = Math.abs(vMaxl - vMinl);
            nMaxw = a.getMax();
            nMaxl = a.w2l(a.getMax());
            nMinw = a.l2w((int) (a.isReversed() ? nMaxl + viewRangeLocal : (nMaxl - viewRangeLocal)));
        }
        if (vMinw < a.getMin()) {
            double viewRangeLocal = vMaxl - vMinl;
            nMinl = a.w2l(a.getMin());
            nMaxw = a.l2w((int) (nMinl + viewRangeLocal));
            nMinw = a.l2w((int) (a.isReversed() ? (nMinl - viewRangeLocal) : (nMinl + viewRangeLocal)));
        }
        a.setViewLimits(nMinw, nMaxw);
    }
}
