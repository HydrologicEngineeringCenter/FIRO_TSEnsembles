/*
 * Copyright 2023  Hydrologic Engineering Center (HEC).
 * United States Army Corps of Engineers
 * All Rights Reserved.  HEC PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval
 * from HEC
 */
package hec.ensembleview.charts;

import hec.geometry.Axis;
import hec.gfx2d.G2dPointerAdapter;
import hec.gfx2d.G2dPointerComponent;
import hec.gfx2d.Viewport;
import rma.swing.RmaImage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * mouse adapter that can pan the plot and scroll using mouse wheel.
 *
 */
public class PanMouseAdapter extends G2dPointerAdapter {
    public static final String PAN_CURSOR = "PanCursor";
    private Cursor panCursor;
    transient G2dPointerComponent pointerComponent;

    public PanMouseAdapter(G2dPointerComponent c, JPanel comp) {
        super(c, comp);
        pointerComponent = c;
        Image panImage = RmaImage.loadURLImage("Images/Pan.gif");
        if(panImage != null)
        {
            panCursor = Toolkit.getDefaultToolkit().createCustomCursor(panImage, new Point(5, 5), PAN_CURSOR);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        mousePressed = null;
    }

    Point mousePressed = null;

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        mousePressed = new Point(e.getPoint());
        previousMouseDragged = mousePressed;
    }

    Point previousMouseDragged = null;

    @Override
    public void mouseDragged(MouseEvent e) {
        if (mousePressed == null) {
            return;
        }
        Point currentPoint = e.getPoint();

        //compute a drag vector
        //dxl, dyl are delta X,Y in local coordinates ( pixels )
        double dxl = previousMouseDragged.x - (double) currentPoint.x;
        double dyl = previousMouseDragged.y - (double) currentPoint.y;   //positive y change is a mouse move down because 0,0 is in the upper left N,N is in the lower right

        //make an assumption that the pointer component is a Viewport. May need to make a pan component tool interface??
        if (pointerComponent instanceof Viewport) {
            Viewport v = (Viewport) pointerComponent;
            boolean repaint = false;
            if (dxl != 0) {
                panAxis(v.getAxis("x1"), dxl);
                panAxis(v.getAxis("x2"), dxl);
                repaint = true;
            }

            if (dyl != 0) {
                panAxis(v.getAxis("y1"), dyl);
                panAxis(v.getAxis("y2"), dyl);
                repaint = true;
            }
            if (repaint) {
                previousMouseDragged = currentPoint;
                v.getG2dPanel().repaint();
            }
        }
    }

    /**
     * pan an axis by a given amount "deltaA" in local coordinates.
     * @param a
     * @param dal
     */
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

    /*
     * Method for scrolling by a block increment.
     * Added for mouse wheel scrolling support
     */

    public java.awt.Cursor getCursor()
    {
        if ( panCursor == null ) return super.getCursor();
        return panCursor;
    }
}
