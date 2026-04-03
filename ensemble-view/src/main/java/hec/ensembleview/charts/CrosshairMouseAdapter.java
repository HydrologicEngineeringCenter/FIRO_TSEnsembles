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
    private static final int TOUCH_RADIUS_PIXELS = 12;
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
        seriesList.add(new DataSeries(name, xValues, yValues, null));
    }

    public void addPointSeries(String name, double[] xValues, double[] yValues, int[] memberIndices) {
        seriesList.add(new DataSeries(name, xValues, yValues, memberIndices));
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

        // Try point-snap first (for scatter/probability data with member indices)
        String pointResult = findNearestPoint(scl);
        if (pointResult != null) {
            tooltipText = pointResult;
            return;
        }

        // Fall back to line interpolation (for time series data)
        String lineResult = findNearestLine(scl, worldX, worldY);
        tooltipText = (lineResult != null) ? lineResult : String.format("y: %.2f", worldY);
    }

    private String findNearestPoint(Scale scl) {
        double bestDistPx = Double.MAX_VALUE;
        String bestLabel = null;

        for (DataSeries series : seriesList) {
            if (series.memberIndices == null) continue;

            for (int i = 0; i < series.xValues.length; i++) {
                int ptX = scl.e2x(series.xValues[i]);
                int ptY = scl.n2y(series.yValues[i]);
                double dist = Math.sqrt(Math.pow(ptX - currentMouse.x, 2) + Math.pow(ptY - currentMouse.y, 2));

                if (dist <= TOUCH_RADIUS_PIXELS && dist < bestDistPx) {
                    bestDistPx = dist;
                    bestLabel = String.format("Member %d  %s: %.2f",
                            series.memberIndices[i], series.name, series.yValues[i]);
                }
            }
        }
        return bestLabel;
    }

    private String findNearestLine(Scale scl, double worldX, double worldY) {
        String touchedName = null;
        double touchedYValue = 0;
        double bestYDistPx = Double.MAX_VALUE;

        for (DataSeries series : seriesList) {
            if (series.memberIndices != null) continue; // skip point series
            if (series.xValues.length < 2) continue;

            int leftIdx = -1;
            for (int i = 0; i < series.xValues.length - 1; i++) {
                if (series.xValues[i] <= worldX && series.xValues[i + 1] >= worldX) {
                    leftIdx = i;
                    break;
                }
            }

            if (leftIdx < 0) continue;

            double x0 = series.xValues[leftIdx];
            double x1 = series.xValues[leftIdx + 1];
            double y0 = series.yValues[leftIdx];
            double y1 = series.yValues[leftIdx + 1];
            double t = (x1 != x0) ? (worldX - x0) / (x1 - x0) : 0;
            double interpolatedY = y0 + t * (y1 - y0);

            int lineLocalY = scl.n2y(interpolatedY);
            double yDistPx = Math.abs(lineLocalY - currentMouse.y);

            if (yDistPx <= TOUCH_RADIUS_Y_PIXELS && yDistPx < bestYDistPx) {
                bestYDistPx = yDistPx;
                touchedName = series.name;
                touchedYValue = interpolatedY;
            }
        }

        if (touchedName != null) {
            return String.format("%s  y: %.2f", touchedName, touchedYValue);
        }
        return null;
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
        final int[] memberIndices;

        DataSeries(String name, double[] xValues, double[] yValues, int[] memberIndices) {
            this.name = name;
            this.xValues = xValues;
            this.yValues = yValues;
            this.memberIndices = memberIndices;
        }
    }
}
