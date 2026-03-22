package hec.ensembleview.charts;

import hec.geometry.Scale;
import hec.gfx2d.Viewport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Mouse listener that draws a crosshair on the chart viewport.
 * The crosshair always follows the mouse. A tooltip always displays
 * the current y-value. When the crosshair touches an ensemble line,
 * the tooltip shows the series name and value.
 * Hides when the pan tool is active.
 */
public class CrosshairMouseAdapter extends MouseAdapter {
    private static final int TOUCH_RADIUS_Y_PIXELS = 8;
    private static final Color CROSSHAIR_COLOR = new Color(100, 100, 100, 180);

    private final Viewport viewport;
    private Point currentMouse;
    private String tooltipText;
    private boolean enabled = true;
    private final List<DataSeries> seriesList = new ArrayList<>();

    public CrosshairMouseAdapter(Viewport viewport) {
        this.viewport = viewport;
    }

    public void addSeries(String name, double[] xValues, double[] yValues) {
        seriesList.add(new DataSeries(name, xValues, yValues));
    }

    public void clearSeries() {
        seriesList.clear();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!enabled) {
            return;
        }
        // Hide crosshair when pan tool is active
        Component source = e.getComponent();
        if (source != null && source.getCursor() != null
                && PanMouseAdapter.PAN_CURSOR.equals(source.getCursor().getName())) {
            if (currentMouse != null) {
                currentMouse = null;
                tooltipText = null;
                viewport.getG2dPanel().repaint();
            }
            return;
        }
        currentMouse = e.getPoint();
        updateTooltip();
        viewport.getG2dPanel().repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        currentMouse = null;
        tooltipText = null;
        viewport.getG2dPanel().repaint();
    }

    @SuppressWarnings("unchecked")
    private void updateTooltip() {
        if (currentMouse == null) {
            return;
        }

        List<Scale> scaleVector = viewport.getScaleVector();
        if (scaleVector.isEmpty()) {
            return;
        }

        Scale scl = scaleVector.get(0);
        double worldX = scl.x2e(currentMouse.x);
        double worldY = scl.y2n(currentMouse.y);

        // Check if the crosshair is touching any ensemble line.
        // For each series, find the two bracketing x-points around the mouse,
        // interpolate the y-value where the line actually is, and check proximity.
        String touchedName = null;
        double touchedYValue = 0;
        double bestYDistPx = Double.MAX_VALUE;

        for (DataSeries series : seriesList) {
            if (series.xValues.length < 2) {
                continue;
            }

            // Find the bracketing pair: the last point with x <= worldX
            int leftIdx = -1;
            for (int i = 0; i < series.xValues.length - 1; i++) {
                if (series.xValues[i] <= worldX && series.xValues[i + 1] >= worldX) {
                    leftIdx = i;
                    break;
                }
            }

            double interpolatedY;
            if (leftIdx >= 0) {
                // Interpolate y between the two bracketing points
                double x0 = series.xValues[leftIdx];
                double x1 = series.xValues[leftIdx + 1];
                double y0 = series.yValues[leftIdx];
                double y1 = series.yValues[leftIdx + 1];
                double t = (x1 != x0) ? (worldX - x0) / (x1 - x0) : 0;
                interpolatedY = y0 + t * (y1 - y0);
            } else {
                // Mouse is outside the series x-range, skip
                continue;
            }

            // Check pixel distance between interpolated line position and mouse
            int lineLocalY = scl.n2y(interpolatedY);
            double yDistPx = Math.abs(lineLocalY - currentMouse.y);

            if (yDistPx <= TOUCH_RADIUS_Y_PIXELS && yDistPx < bestYDistPx) {
                bestYDistPx = yDistPx;
                touchedName = series.name;
                touchedYValue = interpolatedY;
            }
        }

        if (touchedName != null) {
            tooltipText = String.format("%s  y: %.2f", touchedName, touchedYValue);
        } else {
            tooltipText = String.format("y: %.2f", worldY);
        }
    }

    public void paintCrosshair(Graphics2D g2, int viewportX, int viewportY, int viewportWidth, int viewportHeight) {
        if (!enabled || currentMouse == null) {
            return;
        }

        // Translate from viewport-local to panel coordinates
        int px = currentMouse.x + viewportX;
        int py = currentMouse.y + viewportY;

        // Clip drawing to the viewport area
        Shape oldClip = g2.getClip();
        g2.clipRect(viewportX, viewportY, viewportWidth, viewportHeight);

        // Draw crosshair lines
        g2.setColor(CROSSHAIR_COLOR);
        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10.0f, new float[]{4.0f, 4.0f}, 0.0f));
        g2.drawLine(px, viewportY, px, viewportY + viewportHeight);
        g2.drawLine(viewportX, py, viewportX + viewportWidth, py);

        // Always draw the tooltip label
        if (tooltipText != null) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(tooltipText);
            int textHeight = fm.getHeight();

            int labelX = px + 10;
            int labelY = py - 10;

            // Keep label within viewport bounds
            if (labelX + textWidth + 6 > viewportX + viewportWidth) {
                labelX = px - textWidth - 16;
            }
            if (labelY - textHeight - 2 < viewportY) {
                labelY = py + textHeight + 10;
            }

            g2.setColor(new Color(255, 255, 230, 220));
            g2.fillRoundRect(labelX - 3, labelY - textHeight + 2, textWidth + 6, textHeight + 2, 4, 4);
            g2.setColor(Color.DARK_GRAY);
            g2.drawRoundRect(labelX - 3, labelY - textHeight + 2, textWidth + 6, textHeight + 2, 4, 4);
            g2.drawString(tooltipText, labelX, labelY);
        }

        g2.setClip(oldClip);
    }

    private static class DataSeries {
        final String name;
        final double[] xValues;
        final double[] yValues;

        DataSeries(String name, double[] xValues, double[] yValues) {
            this.name = name;
            this.xValues = xValues;
            this.yValues = yValues;
        }
    }
}